package com.example.synji_calendar.ui.group

/**
 * 群组基础信息
 */
data class GroupInfo(
    val groupId: String,
    val name: String,
    val ownerId: String,
    val inviteCode: String,
    val memberCount: Int = 0
)

/**
 * 群组成员信息
 */
data class GroupMember(
    val userId: String,
    val nickname: String,
    val phoneNumber: String,
    val role: String, // "OWNER", "ADMIN", "MEMBER"
    val joinedAt: String? = null
)

/**
 * 设置/取消管理员请求
 */
data class SetAdminRequest(
    val groupId: String,
    val userId: String,
    val isAdmin: Boolean
)

/**
 * 创建群组请求体
 */
data class CreateGroupRequest(
    val name: String
)

/**
 * 加入群组请求体
 */
data class JoinGroupRequest(
    val inviteCode: String
)
