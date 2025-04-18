package com.example.upload.global.security

import com.example.upload.domain.member.member.entity.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.core.user.OAuth2User

class SecurityUser(
    val id: Long,
    username: String?,
    password: String?,
    val nickname: String,
    authorities: Collection<GrantedAuthority?>
) :
    User(username, password, authorities), OAuth2User {
    constructor(member: Member) : this(
        member.id,
        member.username,
        member.password,
        member.nickname,
        member.authorities
    )

    override fun getAttributes(): Map<String, Any> {
        return emptyMap()
    }

    override fun getName(): String {
        return this.username
    }
}