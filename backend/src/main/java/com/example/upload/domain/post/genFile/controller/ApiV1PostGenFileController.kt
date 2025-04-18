package com.example.upload.domain.post.genFile.controller

import com.example.upload.domain.post.genFile.dto.PostGenFileDto
import com.example.upload.domain.post.genFile.entity.PostGenFile
import com.example.upload.domain.post.post.service.PostService
import com.example.upload.global.Rq
import com.example.upload.global.app.AppConfig.Companion.getTempDirPath
import com.example.upload.global.dto.RsData
import com.example.upload.global.exception.ServiceException
import com.example.upload.standard.util.Ut.file.toFile
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import lombok.RequiredArgsConstructor
import org.springframework.http.MediaType
import org.springframework.lang.NonNull
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/posts/{postId}/genFiles")
@RequiredArgsConstructor
@Tag(name = "ApiV1PostGenFileController", description = "API 글 파일 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
class ApiV1PostGenFileController(
    private val postService: PostService,
    private val rq: Rq
) {

    @PostMapping(value = ["/{typeCode}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "다건등록")
    @Transactional
    fun makeNewItems(
        @PathVariable postId: Long,
        @PathVariable typeCode: PostGenFile.TypeCode,
        @Parameter(
            description = "업로드할 파일 목록",
            content = [Content(mediaType = "multipart/form-data")]
        ) @NonNull @RequestPart("files") files: Array<MultipartFile>
    ): RsData<List<PostGenFileDto>> {
        val actor = rq.actor

        val post = postService.getItem(postId).orElseThrow {
            ServiceException(
                "404-1",
                "${postId}번 글은 존재하지 않습니다."
            )
        }

        post.checkActorCanMakeNewGenFile(actor)

        val postGenFiles: MutableList<PostGenFile> = ArrayList()

        for (file in files) {
            if (file.isEmpty) continue

            val filePath = toFile(file, getTempDirPath())

            postGenFiles.add(
                post.addGenFile(
                    typeCode,
                    filePath
                )
            )
        }

        postService.flush()

        return RsData(
            "201-1",
            "${postGenFiles.size}개의 파일이 생성되었습니다.",
            postGenFiles.map { postGenFile: PostGenFile? -> PostGenFileDto(postGenFile!!) }
        )
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "다건조회")
    fun items(
        @PathVariable postId: Long
    ): List<PostGenFileDto> {
        val post = postService.getItem(postId).orElseThrow {
            ServiceException(
                "404-1",
                "${postId}번 글은 존재하지 않습니다."
            )
        }

        return post
            .genFiles
            .map { postGenFile: PostGenFile? -> PostGenFileDto(postGenFile!!) }

    }
}