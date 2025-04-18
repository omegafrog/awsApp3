package com.example.upload.global

import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.member.member.service.MemberService
import com.example.upload.global.exception.ServiceException
import com.example.upload.global.security.SecurityUser
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class Rq(
    private val request: HttpServletRequest,
    private val response: HttpServletResponse,
    private val memberService: MemberService
) {

    fun setLogin(actor: Member) {
        // 유저 정보 생성

        val user: UserDetails = SecurityUser(actor.id!!, actor.username, "", actor.nickname, actor.authorities)

        // 인증 정보 저장소
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(user, null, user.authorities)
    }

    val actor: Member
        get() {
            val authentication = SecurityContextHolder.getContext().authentication
                ?: throw ServiceException("401-2", "로그인이 필요합니다.")

            val principal = authentication.principal as? SecurityUser
                ?: throw ServiceException("401-3", "잘못된 인증 정보입니다")

            val user = principal

            return Member(
                user.id,
                user.username,
                user.nickname
            )
        }

    fun getHeader(name: String): String? {
        return request.getHeader(name)
    }

    fun getValueFromCookie(name: String): String? {
        return request.cookies?.firstOrNull { it.name == name }?.value
    }

    fun addCookie(name: String?, value: String?) {
        Cookie(name, value)
            .apply {
                domain = "localhost"
                path = "/"
                isHttpOnly = true
                secure = true
                setAttribute("SameSite", "Strict")
            }
            .also {
                response.addCookie(it)
            }
    }

    fun getRealActor(actor: Member): Member {
        return memberService.findById(actor.id!!).get()
    }

    fun removeCookie(name: String?) {

        Cookie(name, null)
            .apply {
                domain = "localhost"
                path = "/"
                isHttpOnly = true
                secure = true
                setAttribute("SameSite", "Strict")
                maxAge = 0
            }
            .also {
                response.addCookie(it)
            }

    }

    val isLogin: Boolean
        get() = runCatching { actor }.isSuccess
}