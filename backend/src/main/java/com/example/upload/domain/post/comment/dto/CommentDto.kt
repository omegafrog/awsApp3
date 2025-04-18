package com.example.upload.domain.post.comment.dto

import com.example.upload.domain.post.comment.entity.Comment
import java.time.LocalDateTime

class CommentDto(
    val id: Long,
    val content: String,
    val postId: Long,
    val authorId: Long,
    val authorName: String,
    val createdTime: LocalDateTime,
    val modifiedTime: LocalDateTime,
) {

    constructor(comment: Comment): this(
        id = comment.id!!,
        content = comment.content,
        postId = comment.post.id!!,
        authorId = comment.author.id!!,
        authorName = comment.author.nickname,
        createdTime = comment.createdDate,
        modifiedTime = comment.modifiedDate,
    )
}
