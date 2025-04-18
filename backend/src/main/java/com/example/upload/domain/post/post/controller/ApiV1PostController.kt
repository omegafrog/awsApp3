package com.example.upload.domain.post.post.controller

import com.example.upload.domain.post.post.dto.PageDto
import com.example.upload.domain.post.post.dto.PostDto
import com.example.upload.domain.post.post.dto.PostListParamDto
import com.example.upload.domain.post.post.dto.PostWithContentDto
import com.example.upload.domain.post.post.service.PostService
import com.example.upload.global.Rq
import com.example.upload.global.dto.Empty
import com.example.upload.global.dto.RsData
import com.example.upload.global.exception.ServiceException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springdoc.core.annotations.ParameterObject
import org.springframework.lang.NonNull
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "ApiV1PostController", description = "글 API")
@RestController
@RequestMapping("/api/v1/posts")
class ApiV1PostController(
    private val postService: PostService,
    private val rq: Rq
) {

    data class StatisticsResBody(
        @field:NonNull @param:NonNull val postCount: Long,
        @field:NonNull @param:NonNull val postPublishedCount: Long,
        @field:NonNull @param:NonNull val postListedCount: Long
    )

    @get:GetMapping("/statistics")
    @get:Operation(summary = "통계 조회12")
    val statistics: RsData<StatisticsResBody>
        get() = RsData(
            "200-1",
            "통계 조회가 완료되었습니다.",
            StatisticsResBody(
                10,
                10,
                10
            )
        )

    @Operation(summary = "글 목록 조회", description = "페이징 처리와 검색 가능")
    @GetMapping
    @Transactional(readOnly = true)
    fun getItems(@ParameterObject postListParamDto: PostListParamDto): RsData<PageDto> {
        val postPage = postService.getItems(postListParamDto)

        return RsData(
            "200-1",
            "글 목록 조회가 완료되었습니다.",
            PageDto(postPage)
        )
    }


    @Operation(summary = "내 글 목록 조회", description = "페이징 처리와 검색 가능")
    @GetMapping("/mine")
    @Transactional(readOnly = true)
    fun getMines(@ParameterObject postListParamDto: PostListParamDto): RsData<PageDto> {
        val actor = rq.actor
        val pagePost = postService.getMines(postListParamDto, actor)

        return RsData(
            "200-1",
            "내 글 목록 조회가 완료되었습니다.",
            PageDto(pagePost)
        )
    }

    @Operation(summary = "글 단건 조회", description = "비밀글은 작성자만 조회 가능")
    @GetMapping("{id}")
    @Transactional(readOnly = true)
    fun getItem(@PathVariable id: Long): RsData<PostWithContentDto> {
        val post = postService.getItem(id).orElseThrow {
            ServiceException(
                "404-1",
                "존재하지 않는 글입니다."
            )
        }

        if (!post.published) {
            val actor = rq.actor
            post.canRead(actor)
        }

        val postWithContentDto = PostWithContentDto(post)
        if (rq.isLogin) {
            postWithContentDto.canActorHandle = post.getHandleAuthority(rq.actor)
        }

        return RsData(
            "200-1",
            "${id}번 글을 조회하였습니다.",
            postWithContentDto
        )
    }

    data class WriteReqBody(
        @field:NotBlank val title: String,
        @field:NotBlank val content: String,
        val published: Boolean,
        val listed: Boolean
    )

    @Operation(summary = "글 작성", description = "로그인 한 사용자만 글 작성 가능")
    @PostMapping
    @Transactional
    fun write(@RequestBody @Valid reqBody: WriteReqBody): RsData<PostWithContentDto> {
        val actor = rq.actor
        val realActor = rq.getRealActor(actor)

        val post = postService.write(realActor, reqBody.title, reqBody.content, reqBody.published, reqBody.listed)

        return RsData(
            "201-1",
            "${post.id}번 글 작성이 완료되었습니다.",
            PostWithContentDto(post)
        )
    }

    data class PostModifyReqBody(
        @field:NotBlank val title: String,
        @field:NotBlank val content: String,
        val published: Boolean,
        val listed: Boolean
    )

    @Operation(summary = "글 수정", description = "작성자와 관리자만 글 수정 가능")
    @PutMapping("/{id}")
    @Transactional
    fun modify(@PathVariable id: Long, @RequestBody @Valid reqBody: PostModifyReqBody): RsData<PostWithContentDto> {
        val actor = rq.actor // 야매

        val post = postService.getItem(id).orElseThrow {
            ServiceException(
                "404-1",
                "존재하지 않는 글입니다."
            )
        }

        post.canModify(actor)

        postService.modify(post, reqBody.title, reqBody.content, reqBody.published, reqBody.listed)

        return RsData(
            "200-1",
            "${id}번 글 수정이 완료되었습니다.",
            PostWithContentDto(post)
        )
    }

    @Operation(summary = "글 삭제", description = "작성자와 관리자만 글 삭제 가능")
    @DeleteMapping("{id}")
    @Transactional
    fun delete(@PathVariable id: Long): RsData<Empty> {
        val actor = rq.actor

        val post = postService.getItem(id).orElseThrow {
            ServiceException(
                "404-1",
                "존재하지 않는 글입니다."
            )
        }

        post.canDelete(actor)
        postService.delete(post)

        return RsData(
            "200-1",
            "${id}번 글 삭제가 완료되었습니다."
        )
    }

    data class PostMakeTempResponseBody(
        @field:NonNull @param:NonNull val post: PostDto
    )

    @Transactional
    @PostMapping("/temp")
    @Operation(summary = "임시 글 생성")
    fun makeTemp(): RsData<PostMakeTempResponseBody> {
        val findTempOrMakeRsData = postService.findTempOrMake(rq.actor)

        return findTempOrMakeRsData.newDataOf(
            PostMakeTempResponseBody(
                PostDto(findTempOrMakeRsData.data)
            )
        )
    }
}
