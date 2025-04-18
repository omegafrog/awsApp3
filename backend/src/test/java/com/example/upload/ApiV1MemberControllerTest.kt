package com.example.upload

import com.example.upload.domain.member.member.controller.ApiV1MemberController
import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.member.member.service.MemberService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1MemberControllerTest @Autowired constructor(
    private val mvc: MockMvc,
    private val memberService: MemberService
) {
    private lateinit var loginedMember: Member
    private lateinit var token: String

    @BeforeEach
    fun login() {
        loginedMember = memberService.findByUsername("user1").get()
        token = memberService.getAuthToken(loginedMember)
    }

    private fun checkMember(resultActions: ResultActions, member: Member) {
        resultActions
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.id").value(member.id))
            .andExpect(jsonPath("$.data.nickname").value(member.nickname))
            .andExpect(jsonPath("$.data.profileImgUrl").value(member.profileImgUrlOrDefault))
    }

    private fun joinRequest(username: String, password: String, nickname: String): ResultActions {
        return mvc
            .perform(
                post("/api/v1/members/join")
                    .content("""
                        {
                            "username": "$username",
                            "password": "$password",
                            "nickname": "$nickname"
                        }
                    """.trimIndent())
                    .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
            )
            .andDo(print())
    }

    @Test
    @DisplayName("회원 가입1")
    fun join1() {
        // given
        val username = "userNew"
        val password = "1234"
        val nickname = "무명"

        // when
        val resultActions = joinRequest(username, password, nickname)
        val member = memberService.findByUsername("userNew").get()

        // then
        assertThat(member.nickname).isEqualTo(nickname)

        resultActions
            .andExpect(status().isCreated())
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("join"))
            .andExpect(jsonPath("$.code").value("201-1"))
            .andExpect(jsonPath("$.msg").value("회원 가입이 완료되었습니다."))

        checkMember(resultActions, member)
    }

    @Test
    @DisplayName("회원 가입2 - username이 이미 존재하는 케이스")
    fun join2() {
        // given
        val username = "user1"
        val password = "1234"
        val nickname = "무명"

        // when
        val resultActions = joinRequest(username, password, nickname)

        // then
        resultActions
            .andExpect(status().isConflict())
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("join"))
            .andExpect(jsonPath("$.code").value("409-1"))
            .andExpect(jsonPath("$.msg").value("이미 사용중인 아이디입니다."))
    }

    @Test
    @DisplayName("회원 가입3 - 입력 데이터 누락")
    fun join3() {
        // given
        val username = ""
        val password = ""
        val nickname = ""

        // when
        val resultActions = joinRequest(username, password, nickname)

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("join"))
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(jsonPath("$.msg").value("""
                nickname : NotBlank : must not be blank
                password : NotBlank : must not be blank
                username : NotBlank : must not be blank
            """.trimIndent()))
    }

    private fun loginRequest(username: String, password: String): ResultActions {
        return mvc
            .perform(
                post("/api/v1/members/login")
                    .content("""
                        {
                            "username": "$username",
                            "password": "$password"
                        }
                    """.trimIndent())
                    .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
            )
            .andDo(print())
    }

    @Test
    @DisplayName("로그인 - 성공")
    fun login1() {
        // given
        val username = "user1"
        val password = "user11234"

        // when
        val resultActions = loginRequest(username, password)
        val member = memberService.findByUsername(username).get()

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("login"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${member.nickname}님 환영합니다."))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.item.id").value(member.id))
            .andExpect(jsonPath("$.data.item.nickname").value(member.nickname))
            .andExpect(jsonPath("$.data.apiKey").value(member.apiKey))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.item.profileImgUrl").value(member.profileImgUrlOrDefault))

        resultActions
            .andExpect { mvcResult ->
                val apiKey = mvcResult.response.getCookie("apiKey")

                assertThat(apiKey).isNotNull
                assertThat(apiKey!!.name).isEqualTo("apiKey")
                assertThat(apiKey.value).isNotBlank()
                assertThat(apiKey.domain).isEqualTo("localhost")
                assertThat(apiKey.path).isEqualTo("/")
                assertThat(apiKey.isHttpOnly).isTrue
                assertThat(apiKey.secure).isTrue

                val accessToken = mvcResult.response.getCookie("accessToken")

                assertThat(accessToken).isNotNull
                assertThat(accessToken!!.name).isEqualTo("accessToken")
                assertThat(accessToken.value).isNotBlank()
                assertThat(accessToken.domain).isEqualTo("localhost")
                assertThat(accessToken.path).isEqualTo("/")
                assertThat(accessToken.isHttpOnly).isTrue
                assertThat(accessToken.secure).isTrue
            }
    }

    @Test
    @DisplayName("로그인 - 실패 - 비밀번호 틀림")
    fun login2() {
        // given
        val username = "user1"
        val password = "1234"

        // when
        val resultActions = loginRequest(username, password)

        // then
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("login"))
            .andExpect(jsonPath("$.code").value("401-2"))
            .andExpect(jsonPath("$.msg").value("비밀번호가 일치하지 않습니다."))
    }

    @Test
    @DisplayName("로그인 - 실패 - 존재하지 않는 username")
    fun login3() {
        // given
        val username = "aaaaa"
        val password = "1234"

        // when
        val resultActions = loginRequest(username, password)

        // then
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("login"))
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.msg").value("잘못된 아이디입니다."))
    }

    @Test
    @DisplayName("로그인 - 실패 - username 누락")
    fun login4() {
        // given
        val username = ""
        val password = "123123"

        // when
        val resultActions = loginRequest(username, password)

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("login"))
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(jsonPath("$.msg").value("username : NotBlank : must not be blank"))
    }

    @Test
    @DisplayName("로그인 - 실패 - password 누락")
    fun login5() {
        // given
        val username = "123123"
        val password = ""

        // when
        val resultActions = loginRequest(username, password)

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("login"))
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(jsonPath("$.msg").value("password : NotBlank : must not be blank"))
    }

    private fun meRequest(apiKey: String): ResultActions {
        return mvc
            .perform(
                get("/api/v1/members/me")
                    .header("Authorization", "Bearer $apiKey")
            )
            .andDo(print())
    }

    @Test
    @DisplayName("내 정보 조회")
    fun me1() {
        // given
        val apiKey = loginedMember.apiKey
        val token = memberService.getAuthToken(loginedMember)

        // when
        val resultActions = meRequest(token)

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("me"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("내 정보 조회가 완료되었습니다."))

        checkMember(resultActions, loginedMember)
    }

    @Test
    @DisplayName("내 정보 조회 - 실패 - 잘못된 api key")
    fun me2() {
        // given
        val apiKey = ""

        // when
        val resultActions = meRequest(apiKey)

        // then
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."))
    }

    @Test
    @DisplayName("내 정보 조회 - 만료된 accessToken 사용")
    fun me3() {
        // given
        val apiKey = loginedMember.apiKey
        val expiredToken = "$apiKey eyJhbGciOiJIUzUxMiJ9.eyJpZCI6MywidXNlcm5hbWUiOiJ1c2VyMSIsImlhdCI6MTczOTI0MDc0NiwiZXhwIjoxNzM5MjQwNzUxfQ.tm-lhZpkazdOtshyrdtq0ioJCampFzx8KBf-alfVS4JUp7zJJchYdYtjMfKtW7c3t4Fg5fEY12pPt6naJjhV-Q"

        // when
        val resultActions = meRequest(expiredToken)

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("me"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("내 정보 조회가 완료되었습니다."))

        checkMember(resultActions, loginedMember)
    }

    @Test
    @DisplayName("로그아웃")
    fun logout() {
        // when
        val resultActions = mvc.perform(
            delete("/api/v1/members/logout")
        )

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("logout"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("로그아웃 되었습니다."))

        resultActions.andExpect { mvcResult ->
            val apiKey = mvcResult.response.getCookie("apiKey")
            assertThat(apiKey).isNotNull
            assertThat(apiKey!!.maxAge).isZero()

            val accessToken = mvcResult.response.getCookie("accessToken")
            assertThat(accessToken).isNotNull
            assertThat(accessToken!!.maxAge).isZero()
        }
    }
}