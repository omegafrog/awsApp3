package com.example.upload.domain.post.comment.entity

import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.post.post.entity.Post
import com.example.upload.global.entity.BaseTime
import com.example.upload.global.exception.ServiceException
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class Comment : BaseTime {

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var author: Member

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var post: Post

    lateinit var content: String

    constructor()

    constructor(post: Post, author: Member, content: String) {
        this.author = author
        this.post = post
        this.content = content
    }

    fun modify(content: String) {
        this.content = content
    }

    fun canModify(actor: Member) {
        if (actor.isAdmin) return
        if (actor == this.author) return
        throw ServiceException("403-1", "자신이 작성한 댓글만 수정 가능합니다.")
    }

    fun canDelete(actor: Member) = when {
        actor.isAdmin -> true
        actor == this.author -> true
        else ->
        throw ServiceException("403-1", "자신이 작성한 댓글만 삭제 가능합니다.")
    }
}