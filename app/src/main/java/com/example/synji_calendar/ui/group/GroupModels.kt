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
