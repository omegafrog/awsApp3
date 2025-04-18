package com.example.upload.domain.post.genFile.controller

import com.example.upload.domain.member.member.service.MemberService
import com.example.upload.domain.post.genFile.entity.PostGenFile
import com.example.upload.domain.post.post.service.PostService
import com.example.upload.global.app.AppConfig.Companion.getTempDirPath
import com.example.upload.standard.util.SampleResource
import com.example.upload.standard.util.Ut.file.downloadByHttp
import com.example.upload.standard.util.Ut.file.rm
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.io.FileInputStream

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1PostGenFileControllerTest {
    @Autowired
    private lateinit var postService: PostService

    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    @DisplayName("다건 조회")
    fun t1() {
        // given
        val postId = 1L

        // when
        val resultActions = mvc
            .perform(MockMvcRequestBuilders.get("/api/v1/posts/$postId/genFiles"))
            .andDo { MockMvcResultHandlers.print() }

        // then
        val post = postService.getItem(postId).get()
        val postGenFiles = post.genFiles

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostGenFileController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("items"))
            .andExpect(MockMvcResultMatchers.status().isOk())

        postGenFiles.forEachIndexed { i, postGenFile ->
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].id").value(postGenFile.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[$i].createdDate")
                        .value(Matchers.startsWith(postGenFile.createdDate.toString().substring(0, 20))))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[$i].modifiedDate")
                        .value(Matchers.startsWith(postGenFile.modifiedDate.toString().substring(0, 20))))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].postId").value(postGenFile.post.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].typeCode").value(postGenFile.typeCode.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].fileExtTypeCode").value(postGenFile.fileExtTypeCode))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].fileExtType2Code").value(postGenFile.fileExtType2Code))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].fileSize").value(postGenFile.fileSize))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].fileNo").value(postGenFile.fileNo))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].fileExt").value(postGenFile.fileExt))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].fileDateDir").value(postGenFile.fileDateDir))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].originalFileName").value(postGenFile.originalFileName))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].downloadUrl").value(postGenFile.downloadUrl))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].publicUrl").value(postGenFile.publicUrl))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].fileName").value(postGenFile.fileName))
        }
    }

    @Test
    @DisplayName("새 파일 등록")
    @WithUserDetails("user2")
    fun t2() {
        // given
        val postId = 9L
        val typeCode = PostGenFile.TypeCode.attachment
        val newFilePath = downloadByHttp("https://picsum.photos/id/237/200/300", getTempDirPath())

        // when
        val file = MockMultipartFile(
            "files",
            SampleResource.IMG_JPG_SAMPLE1.originalFileName,
            SampleResource.IMG_JPG_SAMPLE1.contentType,
            FileInputStream(newFilePath)
        )

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.multipart("/api/v1/posts/$postId/genFiles/$typeCode")
                    .file(file)
            )
            .andDo { MockMvcResultHandlers.print() }

        // then
        val post = postService.getItem(postId).get()
        println("생성된 파일 수: ${post.genFiles.size}")

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostGenFileController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("makeNewItems"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("1개의 파일이 생성되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].createdDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].modifiedDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].postId").value(postId.toInt()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].typeCode").value(typeCode.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileExtTypeCode").value(SampleResource.IMG_JPG_SAMPLE1.fileExtTypeCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileExtType2Code").value(SampleResource.IMG_JPG_SAMPLE1.fileExtType2Code))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileSize").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileNo").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileExt").value(SampleResource.IMG_JPG_SAMPLE1.fileExt))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileDateDir").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].originalFileName").value(SampleResource.IMG_JPG_SAMPLE1.originalFileName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].downloadUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].publicUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileName").isString())

        // cleanup
        rm(newFilePath)
    }

    @Test
    @DisplayName("새 파일 등록(다건)")
    @WithUserDetails("user2")
    fun t4() {
        // given
        val postId = 9L
        val typeCode = PostGenFile.TypeCode.attachment
        val newFilePath1 = SampleResource.IMG_JPG_SAMPLE1.makeCopy()
        val newFilePath2 = SampleResource.IMG_JPG_SAMPLE2.makeCopy()

        // when
        val file1 = MockMultipartFile(
            "files",
            SampleResource.IMG_JPG_SAMPLE1.originalFileName,
            SampleResource.IMG_JPG_SAMPLE1.contentType,
            FileInputStream(newFilePath1)
        )

        val file2 = MockMultipartFile(
            "files",
            SampleResource.IMG_JPG_SAMPLE2.originalFileName,
            SampleResource.IMG_JPG_SAMPLE2.contentType,
            FileInputStream(newFilePath2)
        )

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.multipart("/api/v1/posts/$postId/genFiles/$typeCode")
                    .file(file1)
                    .file(file2)
            )
            .andDo { MockMvcResultHandlers.print() }

        // then
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostGenFileController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("makeNewItems"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("2개의 파일이 생성되었습니다."))

            // 첫 번째 파일 검증
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].createdDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].modifiedDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].postId").value(postId.toInt()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].typeCode").value(typeCode.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileExtTypeCode").value(SampleResource.IMG_JPG_SAMPLE1.fileExtTypeCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileExtType2Code").value(SampleResource.IMG_JPG_SAMPLE1.fileExtType2Code))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileSize").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileNo").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileExt").value(SampleResource.IMG_JPG_SAMPLE1.fileExt))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileDateDir").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].originalFileName").value(SampleResource.IMG_JPG_SAMPLE1.originalFileName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].downloadUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].publicUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileName").isString())

            // 두 번째 파일 검증
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].createdDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].modifiedDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].postId").value(postId.toInt()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].typeCode").value(typeCode.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].fileExtTypeCode").value(SampleResource.IMG_JPG_SAMPLE2.fileExtTypeCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].fileExtType2Code").value(SampleResource.IMG_JPG_SAMPLE2.fileExtType2Code))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].fileSize").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].fileNo").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].fileExt").value(SampleResource.IMG_JPG_SAMPLE2.fileExt))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].fileDateDir").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].originalFileName").value(SampleResource.IMG_JPG_SAMPLE2.originalFileName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].downloadUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].publicUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].fileName").isString())

        // cleanup
        rm(newFilePath1)
        rm(newFilePath2)
    }
}