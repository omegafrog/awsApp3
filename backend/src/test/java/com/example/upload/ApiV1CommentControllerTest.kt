package com.example.upload

import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.member.member.service.MemberService
import com.example.upload.domain.post.comment.controller.ApiV1CommentController
import com.example.upload.domain.post.post.service.PostService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1CommentControllerTest @Autowired constructor(
    private val mvc: MockMvc,
    private val postService: PostService,
    private val memberService: MemberService
) {
    private lateinit var loginedMember: Member
    private lateinit var token: String

    @BeforeEach
    fun login() {
        loginedMember = memberService.findByUsername("user1").get()
        token = memberService.getAuthToken(loginedMember)
    }

    @Test
    @DisplayName("댓글 작성")
    fun write() {
        // given
        val postId = 1L
        val content = "댓글 내용"

        // when
        val resultActions = mvc
            .perform(
                post("/api/v1/posts/$postId/comments")
                    .header("Authorization", "Bearer $token")
                    .content("""
                        {
                            "content": "$content"
                        }
                    """.trimIndent())
                    .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
            )
            .andDo(print())

        // then
        val post = postService.getItem(postId).get()
        val comment = post.latestComment

        resultActions
            .andExpect(status().isCreated())
            .andExpect(handler().handlerType(ApiV1CommentController::class.java))
            .andExpect(handler().methodName("write"))
            .andExpect(jsonPath("$.code").value("201-1"))
            .andExpect(jsonPath("$.msg").value("${comment.id}번 댓글 작성이 완료되었습니다."))
    }

    @Test
    @DisplayName("댓글 수정")
    fun modify() {
        // given
        val postId = 1L
        val commentId = 1L
        val content = "댓글 내용"

        // when
        val resultActions = mvc
            .perform(
                put("/api/v1/posts/$postId/comments/$commentId")
                    .header("Authorization", "Bearer $token")
                    .content("""
                        {
                            "content": "$content"
                        }
                    """.trimIndent())
                    .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
            )
            .andDo(print())

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1CommentController::class.java))
            .andExpect(handler().methodName("modify"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${commentId}번 댓글 수정이 완료되었습니다."))
    }

    @Test
    @DisplayName("댓글 삭제")
    fun delete1() {
        // given
        val postId = 1L
        val commentId = 1L

        // when
        val resultActions = mvc
            .perform(
                delete("/api/v1/posts/$postId/comments/$commentId")
                    .header("Authorization", "Bearer $token")
            )
            .andDo(print())

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1CommentController::class.java))
            .andExpect(handler().methodName("delete"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${commentId}번 댓글 삭제가 완료되었습니다."))
    }

    @Test
    @DisplayName("댓글 다건 조회")
    fun items() {
        // given
        val postId = 1L

        // when
        val resultActions = mvc
            .perform(get("/api/v1/posts/$postId/comments"))
            .andDo(print())

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1CommentController::class.java))
            .andExpect(handler().methodName("getItems"))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2))
    }
}
