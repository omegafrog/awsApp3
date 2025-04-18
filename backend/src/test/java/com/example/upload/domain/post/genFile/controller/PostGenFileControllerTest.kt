package com.example.upload.domain.post.genFile.controller

import com.example.upload.domain.member.member.service.MemberService
import com.example.upload.domain.post.post.service.PostService
import com.example.upload.standard.util.Ut
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PostGenFileControllerTest {

    @Autowired
    private lateinit var postService: PostService

    @Autowired
    private lateinit var  memberService: MemberService

    @Autowired
    private lateinit var  mvc: MockMvc

    @Test
    @DisplayName("다운로드 테스트")
    fun t1() {
        // given
        val post1 = postService.getItem(1).get()
        val postGenFile1 = post1.genFiles.first()
        val downloadUrl = Ut.url.removeDomain(postGenFile1.downloadUrl)

        // when
        val resultActions = mvc
            .perform(get(downloadUrl))
            .andDo(print())

        // then
        resultActions
            .andExpect(handler().handlerType(PostGenFileController::class.java))
            .andExpect(handler().methodName("download"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${postGenFile1.originalFileName}\""))
            .andExpect(content().contentType(MediaType.IMAGE_GIF))
    }
}