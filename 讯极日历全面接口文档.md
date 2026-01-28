# 讯极日历 (SynJi AI Calendar) 全面接口技术文档 (v2.0 - Search Integrated)

> **版本更新说明 (v2.0)**: 
> 1. **搜索功能增强**: 新增 `/api/schedule/search` 接口，支持后端模糊匹配。
> 2. **内测验证码策略**: 内测期间发码接口仅做模拟，验证码**随机生成**并直接在响应中返回，前端需弹窗提示。
> 3. **管理权限下放**: 允许 **群主 (OWNER)** 和 **管理员 (ADMIN)** 查看群成员列表。
> 4. **成员列表规范**: 规定列表需按昵称首字母进行字母表排序。

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

### 2.1 发送验证码 (内测调整)
- **路径**: `POST /api/auth/send-code`
- **请求**: `{ "phoneNumber": "..." }`
- **后端配合**: 内测期间**不需要**真实接入短信服务商。后端生成随机验证码后，直接在响应的 `data` 字段中返回。
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "验证码发送成功",
    "data": "582913"  // 前端拿到此验证码后，弹窗提示用户
  }
  ```

### 2.2 登录
- **接口**: `POST /api/auth/login`
- **请求体**: `{ "phoneNumber": "...", "code": "..." }` (注意：字段是 code)
- **响应**: 返回 `token` 和 `user` 对象。

### 2.3 用户资料
- **获取**: `GET /api/user/info`
- **更新**: `PUT /api/user/update` -> `{ "nickname": "..." }`

---

## 3. 群组功能 (Group) - 深度管理

### 3.1 基础功能
- **获取已加入群组**: `GET /api/group/list`
- **创建群组**: `POST /api/group/create`
- **加入群组**: `POST /api/group/join` (通过 `inviteCode`)

### 3.2 群成员管理
- **获取成员列表**:
  - **路径**: `/api/group/members`
  - **方法**: `GET`
  - **参数**: `groupId` (Query String)
  - **访问权限**: 仅 **OWNER** 或 **ADMIN** 可查询全量列表。
  - **排序规则**: 后端已按 `nickname` 字母升序排列返回。
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

## 4. 日程管理模块 (Schedule) - 共享与状态

### 4.1 获取日程列表
- **路径**: `/api/schedule/list`
- **并集查询**: 返回 `belonging = "个人"` OR `belonging IN (用户所属群组名称)` 的记录。

### 4.2 搜索日程 (v2.0 新增)
- **路径**: `/api/schedule/search`
- **方法**: `GET`
- **参数**: `keyword` (Query String, 搜索关键词)
- **后端实现要求**:
  1. **身份校验**: 必须从 Header 的 `Authorization` 中解析出 `userId`。
  2. **权限范围**: 仅搜索该用户有权查看的日程（即：`creator_id = userId` OR `belonging IN (该用户加入的群组)`）。
  3. **模糊匹配**: 对 `title` 字段进行模糊查询（SQL: `LIKE %keyword%`）。
  4. **结果排序**: 按日程日期 `date` 降序排列。
- **响应示例**:
  ```json
  {
    "code": 200,
    "data": [
      {
        "id": 101,
        "title": "会议：产品内测讨论",
        "date": "2024-06-01",
        "time": "14:30:00",
        "isImportant": true,
        "belonging": "核心开发组"
      }
    ]
  }
  ```

### 4.3 AI 流程规范
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
