# 讯极日历 (SynJi AI Calendar) 全面接口技术文档 (v1.9)

> **版本更新说明 (v1.9)**: 
> 1. **管理权限下放**：允许 **群主 (OWNER)** 和 **管理员 (ADMIN)** 查看群成员列表。
> 2. **成员列表规范**：规定列表需按昵称首字母进行字母表排序。
> 3. **详情穿透**：明确点击成员项可查看个人资料详情的交互逻辑。
> 4. **后台安全性增强**：明确后端需在 API 层面对管理员限额（最多2人）和操作权限进行二次校验。

---

## 1. 基础约定

### 1.1 服务地址
- **局域网调试地址**: `http://192.168.43.227:8080`
- **模拟器访问地址**: `http://10.0.2.2:8080`

### 1.2 通用响应格式 (ApiResponse)
```json
{
  "code": 200,       // 200-成功，400-参数错误，401-未授权，500-服务器异常
  "message": "success", 
  "data": null       // 业务数据
}
```

---

## 2. 认证与用户模块 (Auth & User)

### 2.1 发送验证码 & 登录
- **发码**: `POST /api/auth/send-code` -> `{ "phoneNumber": "..." }`
- **登录**: `POST /api/auth/login` -> 返回 `token` 和 `user` 对象。

### 2.2 用户资料
- **获取**: `GET /api/user/info`
- **更新**: `PUT /api/user/update` -> `{ "nickname": "..." }`

---

## 3. 群组功能 (Group) - **深度管理**

### 3.1 基础功能
- **获取已加入群组**: `GET /api/group/list`
- **创建群组**: `POST /api/group/create`
- **加入群组**: `POST /api/group/join` (通过 `inviteCode`)

### 3.2 群成员管理 (v1.9 强化)
- **获取成员列表**:
  - **路径**: `/api/group/members`
  - **方法**: `GET`
  - **参数**: `groupId` (Query String)
  - **访问权限**: 仅 **OWNER** 或 **ADMIN** 可查询全量列表。
  - **排序规则**: 后端/前端需支持按 `nickname` 字母升序排列。
  - **响应数据**:
    ```json
    [
      {
        "userId": "10001",
        "nickname": "张三",
        "phoneNumber": "13800138000",
        "role": "OWNER", // OWNER, ADMIN, MEMBER
        "joinedAt": "2024-05-20 10:00:00"
      }
    ]
    ```

- **角色晋升/降级**:
  - **路径**: `/api/group/set-admin`
  - **方法**: `POST`
  - **请求体**: `{ "groupId": "10", "userId": "10002", "isAdmin": true }`
  - **核心约束**: 
    1. 仅 **OWNER** 有权操作。
    2. 单个群组 **ADMIN** 数量不得超过 **2**。
    3. 后端必须在执行前校验当前 ADMIN 总数。

---

## 4. 日程管理模块 (Schedule) - **共享与状态**

### 4.1 获取日程列表
- **路径**: `/api/schedule/list`
- **并集查询**: 返回 `belonging = "个人"` OR `belonging IN (用户所属群组名称)` 的记录。

### 4.2 AI 流程规范
1. **解析 (ai-parse)**: 纯文本识别，不产生数据库记录。
2. **确认入库 (add)**: 
   - **AI 路径**: `isAiGenerated: true`, `isViewed: false` (产生红点)。
   - **手动路径**: `isAiGenerated: false`, `isViewed: true` (无红点)。

---

## 5. 核心逻辑与测试要点

### 5.1 数据库映射参考
| 逻辑字段 | 数据库列名 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| role | role | VARCHAR | 角色标识 |
| isAiGenerated | is_ai_generated | TINYINT | 1/0 映射 |
| isViewed | is_viewed | TINYINT | 1/0 映射 |

### 5.2 安全性要求
1. **防越权**: 成员不得修改/删除非自己创建的日程。
2. **防超限**: 严格执行 2 名管理员的硬性限制，前端拦截+后端拒签。
3. **字母排序**: 成员列表需从 A-Z 排布，提升在大群组中的检索效率。
