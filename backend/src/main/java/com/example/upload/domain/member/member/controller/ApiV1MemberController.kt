package com.example.upload.domain.member.member.controller

import com.example.upload.domain.member.member.dto.MemberDto
import com.example.upload.domain.member.member.service.MemberService
import com.example.upload.domain.post.post.service.PostService
import com.example.upload.global.Rq
import com.example.upload.global.dto.Empty
import com.example.upload.global.dto.RsData
import com.example.upload.global.exception.ServiceException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.lang.NonNull
import org.springframework.web.bind.annotation.*

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "ApiV1MemberController", description = "회원 관련 API")
@RestController
@RequestMapping("/api/v1/members")
class ApiV1MemberController(
    private val memberService: MemberService,
    private val rq: Rq,
    private val postService: PostService
) {

    data class JoinReqBody(
        @field:NotBlank val username: String,
        @field:NotBlank val password: String,
        @field:NotBlank val nickname: String
    )

    @Operation(summary = "회원 가입")
    @PostMapping(value = ["/join"], produces = ["application/json;charset=UTF-8"])
    fun join(@RequestBody @Valid reqBody: JoinReqBody): RsData<MemberDto> {
        memberService.findByUsername(reqBody.username)
            .ifPresent {
                throw ServiceException("409-1", "이미 사용중인 아이디입니다.")
            }


        val member = memberService.join(reqBody.username, reqBody.password, reqBody.nickname, "")
        return RsData(
            "201-1",
            "회원 가입이 완료되었습니다.",
            MemberDto(member)
        )
    }


    data class LoginReqBody(@field:NotBlank val username: String, @field:NotBlank val password: String)

    data class LoginResBody(
        @field:NonNull @param:NonNull val item: MemberDto,
        @field:NonNull @param:NonNull val apiKey: String,
        @field:NonNull @param:NonNull val accessToken: String
    )

    @Operation(summary = "로그인", description = "로그인 성공 시 ApiKey와 AccessToken 반환. 쿠키로도 반환")
    @PostMapping("/login")
    fun login(@RequestBody  @Valid reqBody: LoginReqBody, response: HttpServletResponse?): RsData<LoginResBody> {
        val member = memberService.findByUsername(reqBody.username).orElseThrow {
            ServiceException(
                "401-1",
                "잘못된 아이디입니다."
            )
        }

        if (member.password != reqBody.password) {
            throw ServiceException("401-2", "비밀번호가 일치하지 않습니다.")
        }

        val accessToken = memberService.genAccessToken(member)

        rq!!.addCookie("accessToken", accessToken)
        rq.addCookie("apiKey", member.apiKey)

        return RsData(
            "200-1",
            "${member.nickname}님 환영합니다.",
            LoginResBody(
                MemberDto(member),
                member.apiKey,
                accessToken
            )
        )
    }

    @Operation(summary = "로그아웃", description = "로그아웃 시 쿠키 삭제")
    @DeleteMapping("/logout")
    fun logout(session: HttpSession?): RsData<Empty> {
        rq!!.removeCookie("accessToken")
        rq.removeCookie("apiKey")

        return RsData("200-1", "로그아웃 되었습니다.")
    }

    @Operation(summary = "내 정보 조회12")
    @GetMapping("/me")
    fun me(): RsData<MemberDto> {
        val actor = rq.actor
        val realActor = rq.getRealActor(actor)

        return RsData(
            "200-1",
            "내 정보 조회가 완료되었습니다.",
            MemberDto(realActor)
        )
    }
}
