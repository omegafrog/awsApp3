package com.example.upload.domain.member.member.service

import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.member.member.repository.MemberRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val authTokenService: AuthTokenService

) {

    fun join(username: String, password: String, nickname: String, profileImgUrl: String): Member {
        val member = Member(
            username,
            password,
            username,
            nickname,
            profileImgUrl
        )

        return memberRepository.save(member)
    }

    fun count(): Long {
        return memberRepository.count()
    }

    fun findByUsername(username: String): Optional<Member> {
        return memberRepository.findByUsername(username)
    }

    fun findById(id: Long): Optional<Member> {
        return memberRepository.findById(id)
    }

    fun findByApiKey(apiKey: String): Optional<Member> {
        return memberRepository.findByApiKey(apiKey)
    }

    fun getAuthToken(member: Member): String {
        return member.apiKey + " " + authTokenService.genAccessToken(member)
    }

    fun getMemberByAccessToken(accessToken: String?): Optional<Member> {
        val payload = authTokenService.getPayload(accessToken)
            ?: return Optional.empty()

        val id = payload["id"] as Long
        val username = payload["username"] as String?
        val nickname = payload["nickname"] as String?

        return Optional.of(
            Member(
                id,
                username!!,
                nickname!!
            )
        )
    }

    fun genAccessToken(member: Member): String {
        return authTokenService.genAccessToken(member)
    }
}