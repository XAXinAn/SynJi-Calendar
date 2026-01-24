# 讯极日历 (SynJi AI Calendar) 全面接口技术文档

## 1. 基础约定

### 1.1 服务地址
- **局域网调试地址**: `http://192.168.0.102:8080`
- **模拟器访问地址**: `http://10.0.2.2:8080`

### 1.2 通用响应格式
所有接口均返回 JSON 格式，标准结构如下：
```json
{
  "code": 200,       // 状态码：200-成功，400-参数错误，401-未授权，500-服务器异常
  "message": "描述", // 状态描述信息
  "data": null       // 业务数据，可为对象、数组或空
}
```

### 1.3 数据格式规范
- **日期**: `yyyy-MM-dd` (例如: `2024-01-23`)
- **时间**: `HH:mm:ss` (例如: `10:30:00`)
- **字符编码**: `UTF-8`

### 1.4 安全鉴权
- 除“发送验证码”和“登录”接口外，其余所有接口均需在 **HTTP Header** 中携带：
  - `Authorization`: `登录返回的Token字符串`

---

## 2. 认证与用户模块 (Auth & User)

### 2.1 发送短信验证码
用于验证码登录/注册前的身份验证。
- **路径**: `/api/auth/send-code`
- **方法**: `POST`
- **请求体**:
```json
{ "phoneNumber": "13800138000" }
```
- **后端逻辑**: 生成6位随机码，存入数据库 (过期时间5分钟)，并将验证码打印在后端控制台 (Console) 供调试。

### 2.2 手机号验证码登录/注册
- **路径**: `/api/auth/login`
- **方法**: `POST`
- **请求体**:
```json
{
  "phoneNumber": "13800138000",
  "verifyCode": "123456"
}
```
- **响应数据 (`data`)**:
```json
{
  "token": "mock-token-10001-1706000000000",
  "user": {
    "userId": "10001",
    "phoneNumber": "13800138000",
    "nickname": "新用户8000",
    "isNewUser": true
  }
}
```

### 2.3 获取用户信息
用于自动登录检查或刷新资料。
- **路径**: `/api/user/info`
- **方法**: `GET`
- **Header**: `Authorization: <Token>`
- **响应数据 (`data`)**:
```json
{
  "userId": "10001",
  "phoneNumber": "13800138000",
  "nickname": "新用户8000",
  "isNewUser": false
}
```

---

## 3. 日程管理模块 (Schedule)

### 3.1 获取日程列表
获取当前用户的所有日程。
- **路径**: `/api/schedule/list`
- **方法**: `GET`
- **Header**: `Authorization: <Token>`
- **响应数据 (`data`)**:
```json
[
  {
    "id": 1,
    "title": "项目启动会",
    "date": "2024-01-23",
    "time": "09:00:00",
    "isAllDay": false,
    "location": "会议室3",
    "belonging": "工作",
    "important": true,
    "notes": "带上笔记本电脑"
  }
]
```

### 3.2 新增日程
- **路径**: `/api/schedule/add`
- **方法**: `POST`
- **Header**: `Authorization: <Token>`
- **请求体**:
```json
{
  "title": "买菜",           // 必填
  "date": "2024-01-24",      // 必填，格式 yyyy-MM-dd
  "time": "18:00:00",        // 选填，格式 HH:mm:ss
  "isAllDay": false,         // 选填
  "location": "超市",        // 选填
  "belonging": "生活",       // 选填
  "important": false,        // 选填，布尔值：是否重要
  "notes": "记得带购物袋"      // 选填，长文本备注
}
```
- **说明**: `id` 由后端生成，添加成功后会在响应的 `data` 中返回完整的对象（包含新生成的 `id`）。

### 3.3 修改日程
- **路径**: `/api/schedule/update`
- **方法**: `PUT`
- **Header**: `Authorization: <Token>`
- **请求体**:
```json
{
  "id": 1,                   // 必填，要修改的日程ID
  "title": "去超市买菜",      // 必填
  "date": "2024-01-24",      // 必填
  "time": "18:30:00",        // 选填
  "isAllDay": false,
  "location": "沃尔玛",
  "belonging": "生活",
  "important": true,
  "notes": "买点新鲜的水果"
}
```

### 3.4 删除日程
- **路径**: `/api/schedule/delete/{id}`
- **方法**: `DELETE`
- **Header**: `Authorization: <Token>`
- **URL参数**: `id` 为日程的长整型唯一标识。

---

## 4. 系统工具

### 4.1 服务连通性测试 (Ping)
- **路径**: `/api/ping`
- **方法**: `GET`
- **响应示例**:
```json
{
  "code": 200,
  "message": "Server is running!",
  "data": "Pong"
}
```
