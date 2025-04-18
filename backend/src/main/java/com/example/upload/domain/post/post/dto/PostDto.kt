package com.example.upload.domain.post.post.dto

import com.example.upload.domain.post.post.entity.Post
import java.time.LocalDateTime

open class PostDto(
    val id: Long,
    val createdDate: LocalDateTime,
    val modifiedDate: LocalDateTime,
    val title: String,
    val authorId: Long,
    val authorName: String,
    val published: Boolean,
    val listed: Boolean
) {
    constructor(post: Post) : this(
        id = post.id!!,
        createdDate = post.createdDate,
        modifiedDate = post.modifiedDate,
        authorId = post.author.id!!,
        authorName = post.author.nickname,
        title = post.title,
        published = post.published,
        listed = post.listed
    )

}