package com.example.upload.domain.post.post.service;

import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.post.post.dto.PostListParamDto
import com.example.upload.domain.post.post.entity.Post
import com.example.upload.domain.post.post.repository.PostRepository
import com.example.upload.global.dto.RsData
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@Service
class PostService(
    private val postRepository: PostRepository
) {

    fun write(author: Member, title: String, content: String, published: Boolean, listed: Boolean): Post {

        return Post(
            author = author,
            title = title,
            content = content,
            published = published,
            listed = listed
        ).also {
            postRepository.save(it)
        }
    }

    fun getItems(): List<Post> {
        return postRepository.findAll()
    }

    fun getItem(id: Long): Optional<Post> {
        return postRepository.findById(id)
    }

    fun count(): Long {
        return postRepository.count()
    }

    fun delete(post: Post) {
        postRepository.delete(post);
    }

    @Transactional
    fun modify(post: Post, title: String, content: String, published: Boolean, listed: Boolean) {
        val wasTemp = post.isTemp


        post.title = title
        post.content = content
        post.published = published
        post.listed = listed

        if (wasTemp && !post.isTemp) {
            post.setCreateDateNow()
        }
    }

    fun flush() {
        postRepository.flush()
    }

    fun getLatestItem(): Optional<Post> {
        return postRepository.findTopByOrderByIdDesc()
    }

    fun getItems(postListParamDto: PostListParamDto): Page<Post> {
        val pageable = PageRequest.of(
            postListParamDto.getPage() - 1,
            postListParamDto.getPageSize(),
            Sort.by(Sort.Direction.DESC, "id")
        )
        return postRepository.findByParam(postListParamDto, pageable)
    }

    fun getMines(postListParamDto: PostListParamDto, author: Member): Page<Post> {
        val pageable = PageRequest.of(
            postListParamDto.getPage() - 1,
            postListParamDto.getPageSize(),
            Sort.by(Sort.Direction.DESC, "id")
        )
        return postRepository.findByParam(postListParamDto, author, pageable)
    }

    fun findTempOrMake(author: Member): RsData<Post> {
        val isNew = AtomicBoolean(false)

        val post = postRepository.findTop1ByAuthorAndPublishedAndTitleOrderByIdDesc(
            author,
            false,
            "임시글"
        ).orElseGet {
            isNew.set(true)
            write(author, "임시글", "", false, false)
        }

        if (isNew.get()) {
            return RsData(
                "201-1",
                "${post.id}번 임시글이 생성되었습니다.",
                post
            )
        }

        return RsData(
                "200-1",
        "${post.id}번 임시글을 불러옵니다.",
        post
        );
    }
}
