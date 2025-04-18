package com.example.upload.domain.member.member.repository

import com.example.upload.domain.member.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByUsername(username: String): Optional<Member>
    fun findByApiKey(apiKey: String): Optional<Member>
}