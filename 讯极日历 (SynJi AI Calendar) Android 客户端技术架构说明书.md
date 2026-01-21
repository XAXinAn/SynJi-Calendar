# 讯极日历 (SynJi AI Calendar) Android 客户端技术架构说明书

文档版本： V1.1

适用平台： Android / HarmonyOS

核心理念： 隐私优先 (Privacy First)、通用适配 (Universal)、端云协同 (Cloud-Edge Synergy)

------

## 1. 总体架构概述 (Overview)

讯极日历客户端采用 **MVVM (Model-View-ViewModel)** 架构设计，确保 UI 与业务逻辑解耦。

不同于传统日历应用，本客户端集成了**“双引擎 AI 核心”**：

1. **视觉引擎 (Vision Engine):** 基于 **PaddleOCR** 的通用离线文字识别模块，不依赖任何特定厂商（如 Google GMS 或 Huawei HMS）的服务，实现了在所有 Android 设备上的即时可用性。
2. **推理引擎 (Reasoning Engine):** 基于 **Qwen (通义千问)** 的端侧大模型模块，利用 **Int4 量化**技术在手机本地完成语义分析与日程提取。

------

## 2. 技术选型详情 (Tech Stack)

### 2.1 基础开发环境

- **开发语言:** Java 17 / Kotlin
- **最低支持版本:** Android 8.0 (API Level 26) +
- **UI 框架:** Jetpack Compose 或 XML/ViewBinding (根据项目现状)
- **异步处理:** Kotlin Coroutines (协程) / RxJava

### 2.2 核心 AI 技术栈 (关键亮点)

| **模块**     | **技术选型**                | **详细配置/说明**                                            | **选型理由**                                                 |
| ------------ | --------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **通用 OCR** | **RapidOCR (PaddleOCR-v4)** | • 推理后端：OnnxRuntime / NCNN • 模型：PP-OCRv4-tiny (检测+方向+识别) • 精度：FP16 | • **全机型通用** (无视 GMS/HMS 差异) • 中文识别率业界领先 • 支持离线，保护隐私 |
| **端侧 LLM** | **MNN / MLC-LLM**           | • 模型：Qwen2.5-1.5B-Instruct • 量化：**Int4** (4-bit quantization) • 硬件加速：OpenCL / Vulkan (GPU) | • 模型体积小 (~1.2GB) • 针对 ARM 架构深度优化 • 兼容高通/麒麟/联发科芯片 |
| **网络层**   | **Retrofit + OkHttp**       | • 协议：RESTful API / SSE (流式响应) • 数据格式：JSON        | • 标准化网络请求库 • 支持连接服务端大模型                    |

------

## 3. 客户端逻辑架构图

代码段

```
graph TD
    UI[用户交互层 (Activity/Fragment)] --> VM[ViewModel (业务逻辑)]
    
    subgraph "AI Core Layer (本地智能核心)"
        VM -->|1. Bitmap| OCR[通用 OCR 引擎 (RapidOCR)]
        OCR -->|调用 .onnx| ONNX[OnnxRuntime]
        ONNX -->|2. 原始文本| Prompt[Prompt 构造器]
        Prompt -->|3. 提示词| LLM[端侧 LLM 引擎 (MNN/MLC)]
        LLM -->|加载| ModelFile[Qwen-1.5B-Int4.gguf]
        LLM -->|4. JSON| Parser[结构化解析器]
    end
    
    subgraph "Data Layer (数据层)"
        Parser -->|日程对象| Repo[Repository]
        Repo -->|插入| CalendarProvider[Android 系统日历]
        Repo -->|存储| RoomDB[本地 SQLite 数据库]
    end
    
    subgraph "Cloud Service (云端兜底)"
        VM -.->|复杂任务/低算力模式| CloudAPI[Retrofit 网络请求]
    end
```

------

## 4. 核心功能模块详细设计

### 4.1 模块一：通用离线 OCR (The Vision)

为了保证软件的通用性（Write Once, Run Anywhere），弃用厂商 SDK，采用嵌入式推理引擎。

- **实现方案:** 引入 `RapidOCR-Android-OnnxRuntime` 库。
- **模型文件管理:** 将 `ch_PP-OCRv4_det_infer.onnx` (检测) 和 `ch_PP-OCRv4_rec_infer.onnx` (识别) 内置于 App `assets` 目录。首次启动时解压至私有存储空间。
- **流程:**
  1. **预处理:** 图片压缩至长边 960px，灰度化处理。
  2. **文本检测 (DBNet):** 定位图片中的文字框坐标。
  3. **文本识别 (CRNN):** 将裁剪后的文字框转换为字符串。
  4. **后处理:** 按坐标从上到下、从左到右排序拼接文本，最大程度还原段落结构。

### 4.2 模块二：端侧语义推理 (The Brain)

- **模型选择:** Alibaba Qwen2.5-1.5B-Instruct-Int4。

- **内存管理:**

  - App 启动时不加载模型（节省内存）。
  - 进入“解析模式”时，动态加载模型至 RAM（需约 1.5GB 可用内存）。
  - 任务完成后 5 分钟无操作自动释放模型。

- **Prompt 工程:** 针对 1.5B 小模型设计精简版 Prompt，减少 token 消耗并提高遵循指令的稳定性。

  Plaintext

  ```
  系统: 你是日程助手。提取文本中的: 标题, 开始时间(yyyy-MM-dd HH:mm), 地点。输出JSON。
  用户: [OCR识别出的文本内容]
  ```

### 4.3 模块三：端云协同路由 (The Switch)

客户端内置**智能路由算法**，根据设备状态动态决定在哪里处理数据：

- **本地优先 (Local):**
  - 判断：`设备内存 > 6GB` && `电量 > 20%` && `文本长度 < 500字`。
  - 执行：调用本地 Qwen 模型。
  - 优势：保护隐私，无网络延迟。
- **云端增强 (Cloud):**
  - 判断：`低端机型` || `用户手动开启“高精度模式”` || `文本极长`。
  - 执行：调用服务端 API (Qwen-7B)。
  - 优势：识别更精准，能处理复杂逻辑。

------

## 5. 性能指标预估 (Benchmark)

基于 **Huawei P60 (Snapdragon 8+ Gen 1)** 的参考数据：

| **指标**         | **目标值**   | **说明**                         |
| ---------------- | ------------ | -------------------------------- |
| **OCR 耗时**     | **< 600ms**  | 1080P 图片，RapidOCR 纯 CPU 推理 |
| **OCR 准确率**   | **> 98%**    | 针对标准印刷字体（海报、截图）   |
| **LLM 首字延迟** | **< 1000ms** | 从输入                           |