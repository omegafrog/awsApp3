package com.example.upload.domain.post.post.repository

import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.post.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PostRepository : JpaRepository<Post, Long>, CustomPostRepository {
    fun findTopByOrderByIdDesc(): Optional<Post>
    fun findTop1ByAuthorAndPublishedAndTitleOrderByIdDesc(
        author: Member,
        published: Boolean,
        title: String
    ): Optional<Post>
}
