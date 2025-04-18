package com.example.upload.domain.post.post.dto

import com.example.upload.domain.post.post.entity.Post

class PostWithContentDto(post: Post) {

    val id = post.id!!
    val createdDate = post.createdDate
    val modifiedDate = post.modifiedDate
    val title = post.title
    val authorId = post.author.id!!
    val content = post.content
    val authorName = post.author.nickname
    val authorProfileImgUrl = post.author.profileImgUrl
    val published = post.published
    val listed: Boolean = post.listed
    var canActorHandle = false

}