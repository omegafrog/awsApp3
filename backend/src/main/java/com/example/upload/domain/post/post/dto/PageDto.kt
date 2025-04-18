package com.example.upload.domain.post.post.dto

import com.example.upload.domain.post.post.entity.Post
import org.springframework.data.domain.Page

class PageDto(
    val items: List<PostDto>,
    val totalPages: Int,
    val totalItems: Int,
    val currentPageNo: Int,
    val pageSize: Int
) {

    constructor(postPage: Page<Post>): this(
        items = postPage.content.map { PostDto(it) },
        totalPages = postPage.totalPages,
        totalItems = postPage.totalElements.toInt(),
        currentPageNo = postPage.number + 1,
        pageSize = postPage.size
    )
}
