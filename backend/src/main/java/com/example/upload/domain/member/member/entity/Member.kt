package com.example.upload.domain.member.member.entity

import com.example.upload.global.entity.BaseTime
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
class Member() : BaseTime() {

    @Column(length = 100, unique = true)
    lateinit var username: String

    @Column(length = 100)
    lateinit var password: String

    @Column(length = 100, unique = true)
    lateinit var apiKey: String

    @Column(length = 100)
    lateinit var nickname: String
    lateinit var profileImgUrl: String

    constructor(id: Long, username: String, nickname: String): this() {
        this.id = id
        this.username = username
        this.nickname = nickname
        this.profileImgUrl = ""
    }

    constructor(username: String, password:String, apiKey: String, nickname: String, profileImgUrl: String): this() {
        this.username = username
        this.password = password
        this.apiKey = apiKey
        this.nickname = nickname
        this.profileImgUrl = profileImgUrl
    }

    val isAdmin: Boolean
    get() = username == "admin"

    val authoritiesAsString:List<String>
    get() = if (isAdmin) listOf("ROLE_ADMIN") else emptyList()

    val authorities: List<GrantedAuthority>
    get() = authoritiesAsString.map { SimpleGrantedAuthority(it) }

    val profileImgUrlOrDefault: String
    get() = profileImgUrl.ifBlank{ "https://placehold.co/640x640?text=O_O" }

    fun update(nickname: String) {
        this.nickname = nickname
    }


}