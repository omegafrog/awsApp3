package com.example.upload.domain.post.post.repository

import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.post.post.dto.PostListParamDto
import com.example.upload.domain.post.post.entity.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CustomPostRepository {
    fun findByParam(postListParamDto: PostListParamDto, pageable: Pageable): Page<Post>
    fun findByParam(postListParamDto: PostListParamDto, author: Member, pageable: Pageable): Page<Post>
}
