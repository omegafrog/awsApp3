package com.example.upload.domain.member.member.dto

import com.example.upload.domain.member.member.entity.Member

class MemberDto(
    val id: Long,
    val nickname: String,
    val profileImgUrl: String
) {
    constructor(member: Member): this(
        id = member.id,
        nickname = member.nickname,
        profileImgUrl = member.profileImgUrlOrDefault
    )
}
