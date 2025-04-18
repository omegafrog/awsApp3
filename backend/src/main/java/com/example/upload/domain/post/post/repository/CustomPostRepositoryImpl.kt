package com.example.upload.domain.post.post.repository

import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.post.post.dto.PostListParamDto
import com.example.upload.domain.post.post.entity.Post
import com.example.upload.domain.post.post.entity.QPost
import com.example.upload.standard.search.SearchKeywordType
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.support.PageableExecutionUtils

class CustomPostRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CustomPostRepository {

    override fun findByParam(postListParamDto: PostListParamDto, author: Member, pageable: Pageable): Page<Post> {
        val builder = getParamBuilder(postListParamDto)
        builder.and(QPost.post.author.eq(author))

        return getPagedItems(pageable, builder)
    }

    override fun findByParam(postListParamDto: PostListParamDto, pageable: Pageable): Page<Post> {
        val builder = getParamBuilder(postListParamDto)
        return getPagedItems(pageable, builder)
    }

    private fun getPagedItems(pageable: Pageable, builder: BooleanBuilder?): Page<Post> {
        val postJPAQuery = queryFactory.select(QPost.post)
            .from(QPost.post)
            .where(builder)

        postJPAQuery.offset(pageable.offset).limit(pageable.pageSize.toLong())
        val totalQuery = queryFactory.select(QPost.post.count())
            .from(QPost.post)
            .where(builder)

        applySorting(pageable, postJPAQuery)

        return PageableExecutionUtils.getPage(postJPAQuery.fetch(), pageable) { totalQuery.fetchOne()!! }
    }

    private fun getParamBuilder(postListParamDto: PostListParamDto): BooleanBuilder {
        val keyword = postListParamDto.keyword
        val keywordType = postListParamDto.keywordType

        val builder = BooleanBuilder()
        if (postListParamDto.listed != null) {
            builder.and(QPost.post.listed.eq(postListParamDto.listed))
        }
        if (postListParamDto.published != null) {
            builder.and(QPost.post.published.eq(postListParamDto.published))
        }

        when (keywordType) {
            SearchKeywordType.title -> builder.and(QPost.post.title.containsIgnoreCase(keyword))
            SearchKeywordType.content -> builder.and(QPost.post.content.containsIgnoreCase(keyword))
            SearchKeywordType.author -> builder.and(QPost.post.author.nickname.containsIgnoreCase(keyword))
            else -> builder.and(
                QPost.post.title.containsIgnoreCase(keyword)
                    .or(QPost.post.content.containsIgnoreCase(keyword))
            )
        }

        return builder
    }

    private fun applySorting(pageable: Pageable, postJPAQuery: JPAQuery<Post>) {
        pageable.sort.stream()
            .forEach { order: Sort.Order ->
                val pathBuilder: PathBuilder<*> = PathBuilder(Post::class.java, "post")
                postJPAQuery.orderBy(
                    OrderSpecifier(
                        if (order.direction.isAscending) Order.ASC else Order.DESC,
                        pathBuilder[order.property]  as Expression<Comparable<*>>
                    )
                )
            }
    }
}
