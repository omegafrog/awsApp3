package com.example.upload.domain.post.comment.controller

import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.post.comment.dto.CommentDto
import com.example.upload.domain.post.comment.entity.Comment
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
import lombok.RequiredArgsConstructor
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "ApiV1CommentController", description = "댓글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/comments")
class ApiV1CommentController(
    private val postService: PostService,
    private val rq: Rq
) {

    @Operation(summary = "댓글 목록", description = "게시글의 댓글 목록을 가져옵니다.")
    @GetMapping
    @Transactional(readOnly = true)
    fun getItems(@PathVariable postId: Long): List<CommentDto> {
        val post = postService.getItem(postId).orElseThrow {
            ServiceException(
                "404-1",
                "존재하지 않는 게시글입니다."
            )
        }

        return post.comments
            .map { comment: Comment? -> CommentDto(comment!!) }
    }

    @Operation(summary = "댓글 상세", description = "게시글의 댓글 상세 정보를 가져옵니다.")
    @GetMapping("{id}")
    @Transactional(readOnly = true)
    fun getItem(@PathVariable postId: Long, @PathVariable id: Long): CommentDto {
        val post = postService.getItem(postId).orElseThrow {
            ServiceException(
                "404-1",
                "존재하지 않는 게시글입니다."
            )
        }

        val comment = post.getCommentById(id)

        return CommentDto(comment)
    }


    data class WriteReqBody(val content: String)

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    @PostMapping
    @Transactional
    fun write(@PathVariable postId: Long, @RequestBody reqBody: WriteReqBody): RsData<Empty> {
        val actor = rq.actor
        val comment = _write(postId, actor, reqBody.content)

        postService.flush()

        return RsData(
            "201-1",
            "${comment.id}번 댓글 작성이 완료되었습니다."
        )
    }


    data class CommentModifyReqBody(@field:NotBlank val content: String)

    @Operation(summary = "댓글 수정", description = "게시글의 댓글을 수정합니다.")
    @PutMapping("{id}")
    @Transactional
    fun modify(
        @PathVariable postId: Long,
        @PathVariable id: Long,
        @RequestBody @Valid reqBody: CommentModifyReqBody
    ): RsData<Empty> {
        val actor = rq.actor

        val post = postService.getItem(postId).orElseThrow {
            ServiceException(
                "404-1",
                "존재하지 않는 게시글입니다."
            )
        }

        val comment = post.getCommentById(id)

        comment.canModify(actor)
        comment.modify(reqBody.content)

        return RsData(
            "200-1",
            "${id}번 댓글 수정이 완료되었습니다."
        )
    }


    @DeleteMapping("{id}")
    @Transactional
    fun delete(@PathVariable postId: Long, @PathVariable id: Long): RsData<Empty> {
        val actor = rq.actor
        val post = postService.getItem(postId).orElseThrow {
            ServiceException(
                "404-1",
                "존재하지 않는 게시글입니다."
            )
        }

        val comment = post.getCommentById(id)

        comment.canDelete(actor)
        post.deleteComment(comment)

        return RsData(
            "200-1",
            "${id}번 댓글 삭제가 완료되었습니다."
        )
    }


    fun _write(postId: Long, actor: Member, content: String): Comment {
        val post = postService.getItem(postId).orElseThrow {
            ServiceException(
                "404-1",
                "존재하지 않는 게시글입니다."
            )
        }

        val comment = post.addComment(actor, content)

        return comment
    }
}
