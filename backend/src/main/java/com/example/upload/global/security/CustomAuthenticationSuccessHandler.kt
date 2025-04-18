package com.example.upload.global.security;

import com.example.upload.domain.member.member.service.MemberService
import com.example.upload.global.Rq
import com.example.upload.global.app.AppConfig
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationSuccessHandler(
    private val rq: Rq,
    private val memberService: MemberService

): AuthenticationSuccessHandler {


    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication) {
        val session = request.session

        var redirectUrl = session.getAttribute("redirectUrl") as String


        if (redirectUrl.isBlank()) {
            redirectUrl = AppConfig.getSiteFrontUrl()
        }

        session.removeAttribute("redirectUrl")

        val actor = rq.getRealActor(rq.actor)

        val accessToken = memberService.genAccessToken(actor)

        rq.addCookie("accessToken", accessToken)
        rq.addCookie("apiKey", actor.apiKey)

        response.sendRedirect(redirectUrl)
    }
}
