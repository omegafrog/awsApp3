package com.example.upload.global.security;

import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.member.member.service.MemberService
import com.example.upload.global.Rq
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class CustomAuthenticationFilter(
    private val rq: Rq,
    private val memberService: MemberService

): OncePerRequestFilter() {


    private fun isAuthorizationHeader(): Boolean {
        val authorizationHeader = rq.getHeader("Authorization")

        if (authorizationHeader == null) {
            return false
        }

        return authorizationHeader.startsWith("Bearer ");
    }

    data class AuthToken(val apiKey: String , val accessToken: String) {}

    private fun getAuthTokenFromRequest(): AuthToken? {

        if (isAuthorizationHeader()) {

            val authorizationHeader = rq.getHeader("Authorization")
            val authToken = authorizationHeader?.removePrefix("Bearer ")
            val tokenBits = authToken?.split(" ", limit = 2)

            if (tokenBits?.size != 2) {
                return null
            }

            return AuthToken(tokenBits[0], tokenBits[1]);
        }

        val accessToken = rq.getValueFromCookie("accessToken")
        val apiKey = rq.getValueFromCookie("apiKey")

        if (accessToken == null || apiKey == null) {
            return null
        }

        return AuthToken(apiKey, accessToken)

    }

    private fun getMemberByAccessToken(accessToken: String, apiKey: String): Member? {

        val opAccMember = memberService.getMemberByAccessToken(accessToken);

        if (opAccMember.isPresent) {
            return opAccMember.get()
        }

        val opRefMember = memberService.findByApiKey(apiKey)

        if(opRefMember.isEmpty) {
            return null
        }

        val newAccessToken = memberService.genAccessToken(opRefMember.get());
        rq.addCookie("accessToken", newAccessToken);
        rq.addCookie("apiKey", apiKey);

        return opRefMember.get()
    }


    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        val url = request.requestURI

        if(url in listOf("/api/v1/members/login", "/api/v1/members/join", "/api/*/members/logout")) {
            filterChain.doFilter(request, response)
            return
        }

        val tokens = getAuthTokenFromRequest()

        if (tokens == null) {
            filterChain.doFilter(request, response)
            return
        }

        val (apiKey, accessToken) = tokens

        val actor = getMemberByAccessToken(accessToken, apiKey)

        if (actor == null) {
            filterChain.doFilter(request, response);
            return
        }

        rq.setLogin(actor)
        filterChain.doFilter(request, response)
    }
}
