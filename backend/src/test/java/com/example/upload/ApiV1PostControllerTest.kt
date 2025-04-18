package com.example.upload
import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.member.member.service.MemberService
import com.example.upload.domain.post.post.controller.ApiV1PostController
import com.example.upload.domain.post.post.dto.PostListParamDto
import com.example.upload.domain.post.post.entity.Post
import com.example.upload.domain.post.post.service.PostService
import com.example.upload.standard.search.SearchKeywordType
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1PostControllerTest @Autowired constructor(
    private val mvc: MockMvc,
    private val postService: PostService,
    private val memberService: MemberService
) {
    private lateinit var loginedMember: Member
    private lateinit var token: String

    @BeforeEach
    fun login() {
        loginedMember = memberService.findByUsername("user1").get()
        token = memberService.getAuthToken(loginedMember)
    }

    private fun checkPost(resultActions: ResultActions, post: Post) {
        resultActions
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.id").value(post.id))
            .andExpect(jsonPath("$.data.title").value(post.title))
            .andExpect(jsonPath("$.data.content").value(post.content))
            .andExpect(jsonPath("$.data.authorId").value(post.author.id))
            .andExpect(jsonPath("$.data.authorName").value(post.author.nickname))
            .andExpect(jsonPath("$.data.published").value(post.published))
            .andExpect(jsonPath("$.data.listed").value(post.listed))
            .andExpect(jsonPath("$.data.createdDate").value(matchesPattern(post.createdDate.toString().replace(Regex("0+$"), "") + ".*")))
            .andExpect(jsonPath("$.data.modifiedDate").value(matchesPattern(post.modifiedDate.toString().replace(Regex("0+$"), "") + ".*")))
    }

    private fun checkPosts(posts: List<Post>, resultActions: ResultActions) {
        posts.forEachIndexed { i, post ->
            resultActions
                .andExpect(jsonPath("$.data.items[$i]").exists())
                .andExpect(jsonPath("$.data.items[$i].id").value(post.id))
                .andExpect(jsonPath("$.data.items[$i].title").value(post.title))
                .andExpect(jsonPath("$.data.items[$i].content").doesNotExist())
                .andExpect(jsonPath("$.data.items[$i].authorId").value(post.author.id))
                .andExpect(jsonPath("$.data.items[$i].authorName").value(post.author.nickname))
                .andExpect(jsonPath("$.data.items[$i].published").value(post.published))
                .andExpect(jsonPath("$.data.items[$i].listed").value(post.listed))
                .andExpect(jsonPath("$.data.items[$i].createdDate").value(matchesPattern(post.createdDate.toString().replace(Regex("0+$"), "") + ".*")))
                .andExpect(jsonPath("$.data.items[$i].modifiedDate").value(matchesPattern(post.modifiedDate.toString().replace(Regex("0+$"), "") + ".*")))
        }
    }

    @Test
    @DisplayName("글 다건 조회 - 공개 글 목록")
    fun items1() {
        // given
        val pageSize = 10
        val page = 1
        val keyword = ""
        val keywordType = SearchKeywordType.title
        val listed = true

        // when
        val resultActions = mvc
            .perform(
                get("/api/v1/posts?page=$page&pageSize=$pageSize&keywordType=$keywordType&keyword=$keyword&listed=$listed")
            )
            .andDo(print())

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("getItems"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
            .andExpect(jsonPath("$.data.items.length()").value(pageSize)) // 한페이지당 보여줄 글 개수
            .andExpect(jsonPath("$.data.currentPageNo").isNumber()) // 현재 페이지
            .andExpect(jsonPath("$.data.totalPages").isNumber()) // 전체 페이지 개수

        val postListParamDto = PostListParamDto(
            keywordType = keywordType,
            keyword = keyword,
            listed = listed,
            published = null,
            page = page,
            pageSize = pageSize
        )

        val postPage = postService.getItems(postListParamDto)
        val posts = postPage.content
        checkPosts(posts, resultActions)
    }

    @Test
    @DisplayName("글 다건 조회 - 검색 - 제목, 페이징이 되어야 함.")
    fun items2() {
        // given
        val page = 1
        val pageSize = 3
        val keywordType = SearchKeywordType.title
        val keyword = "title"
        val listed = true

        // when
        val resultActions = mvc
            .perform(
                get("/api/v1/posts?page=$page&pageSize=$pageSize&keywordType=$keywordType&keyword=$keyword&listed=$listed")
            )
            .andDo(print())

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("getItems"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
            .andExpect(jsonPath("$.data.items.length()").value(pageSize)) // 한페이지당 보여줄 글 개수
            .andExpect(jsonPath("$.data.currentPageNo").value(page)) // 현재 페이지
            .andExpect(jsonPath("$.data.totalPages").value(50))
            .andExpect(jsonPath("$.data.totalItems").value(148))

        val postListParamDto = PostListParamDto(
            keywordType = keywordType,
            keyword = keyword,
            listed = listed,
            published = null,
            page = page,
            pageSize = pageSize
        )

        val postPage = postService.getItems(postListParamDto)
        val posts = postPage.content
        checkPosts(posts, resultActions)
    }

    @Test
    @DisplayName("글 다건 조회 - 검색 - 내용, 페이징이 되어야 함.")
    fun items3() {
        // given
        val page = 1
        val pageSize = 3
        val keywordType = SearchKeywordType.content
        val keyword = "content"
        val listed = true

        // when
        val resultActions = mvc
            .perform(
                get("/api/v1/posts?page=$page&pageSize=$pageSize&keywordType=$keywordType&keyword=$keyword&listed=$listed")
            )
            .andDo(print())

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("getItems"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
            .andExpect(jsonPath("$.data.items.length()").value(pageSize)) // 한페이지당 보여줄 글 개수
            .andExpect(jsonPath("$.data.currentPageNo").value(page)) // 현재 페이지
            .andExpect(jsonPath("$.data.totalPages").value(50))
            .andExpect(jsonPath("$.data.totalItems").value(148))

        val postListParamDto = PostListParamDto(
            keywordType = keywordType,
            keyword = keyword,
            listed = listed,
            published = null,
            page = page,
            pageSize = pageSize
        )

        val postPage = postService.getItems(postListParamDto)
        val posts = postPage.content
        checkPosts(posts, resultActions)
    }

    @Test
    @DisplayName("내가 작성한 글 조회 (user1) - 검색, 페이징 되어야 함.")
    fun mines() {
        // given
        val page = 1
        val pageSize = 3
        val keywordType = SearchKeywordType.title
        val keyword = ""

        // when
        val resultActions = mvc
            .perform(
                get("/api/v1/posts/mine?page=$page&pageSize=$pageSize&keywordType=$keywordType&keyword=$keyword")
                    .header("Authorization", "Bearer $token")
            )
            .andDo(print())

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("getMines"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("내 글 목록 조회가 완료되었습니다."))
            .andExpect(jsonPath("$.data.items.length()").value(pageSize)) // 한페이지당 보여줄 글 개수
            .andExpect(jsonPath("$.data.currentPageNo").value(page)) // 현재 페이지
            .andExpect(jsonPath("$.data.totalPages").value(32))
            .andExpect(jsonPath("$.data.totalItems").value(95))

        val postListParamDto = PostListParamDto(
            keywordType = keywordType,
            keyword = keyword,
            listed = null,
            published = null,
            page = page,
            pageSize = pageSize
        )

        val postPage = postService.getMines(postListParamDto, loginedMember)
        val posts = postPage.content
        checkPosts(posts, resultActions)
    }

    private fun itemRequest(postId: Long, apiKey: String): ResultActions {
        return mvc
            .perform(
                get("/api/v1/posts/$postId")
                    .header("Authorization", "Bearer $apiKey")
            )
            .andDo(print())
    }

    @Test
    @DisplayName("글 단건 조회 1 - 다른 유저의 공개글 조회")
    fun item1() {
        // given
        val postId = 1L

        // when
        val resultActions = itemRequest(postId, token)

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("getItem"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${postId}번 글을 조회하였습니다."))

        val post = postService.getItem(postId).get()
        checkPost(resultActions, post)
    }

    @Test
    @DisplayName("글 단건 조회 2 - 없는 글 조회")
    fun item2() {
        // given
        val postId = 100000L

        // when
        val resultActions = itemRequest(postId, token)

        // then
        resultActions
            .andExpect(status().isNotFound())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("getItem"))
            .andExpect(jsonPath("$.code").value("404-1"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 글입니다."))
    }

    @Test
    @DisplayName("글 단건 조회 3 - 다른 유저의 비공개 글 조회")
    fun item3() {
        // given
        val postId = 3L

        // when
        val resultActions = itemRequest(postId, token)

        // then
        resultActions
            .andExpect(status().isForbidden())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("getItem"))
            .andExpect(jsonPath("$.code").value("403-1"))
            .andExpect(jsonPath("$.msg").value("비공개 설정된 글입니다."))
    }

    private fun writeRequest(apiKey: String, title: String, content: String): ResultActions {
        return mvc
            .perform(
                post("/api/v1/posts")
                    .header("Authorization", "Bearer $apiKey")
                    .content("""
                        {
                            "title": "$title",
                            "content": "$content",
                            "published": true,
                            "listed": true
                        }
                    """.trimIndent())
                    .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
            )
            .andDo(print())
    }

    @Test
    @DisplayName("글 작성")
    @WithUserDetails("user3")
    fun write1() {
        // given
        val title = "새로운 글 제목"
        val content = "새로운 글 내용"

        // when
        val resultActions = writeRequest(token, title, content)
        val post = postService.getLatestItem().get()

        // then
        resultActions
            .andExpect(status().isCreated())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("write"))
            .andExpect(jsonPath("$.code").value("201-1"))
            .andExpect(jsonPath("$.msg").value("${post.id}번 글 작성이 완료되었습니다."))

        checkPost(resultActions, post)
    }

    @Test
    @DisplayName("글 작성2 - no apiKey")
    fun write2() {
        // given
        val invalidToken = "212123"
        val title = "새로운 글 제목"
        val content = "새로운 글 내용"

        // when
        val resultActions = writeRequest(invalidToken, title, content)

        // then
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."))
    }

    @Test
    @DisplayName("글 작성3 - no input data")
    fun write3() {
        // given
        val title = ""
        val content = ""

        // when
        val resultActions = writeRequest(token, title, content)

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("write"))
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(jsonPath("$.msg").value("""
                content : NotBlank : must not be blank
                title : NotBlank : must not be blank
            """.trimIndent()))
    }

    private fun modifyRequest(postId: Long, apiKey: String, title: String, content: String): ResultActions {
        return mvc
            .perform(
                put("/api/v1/posts/$postId")
                    .header("Authorization", "Bearer $apiKey")
                    .content("""
                        {
                            "title": "$title",
                            "content": "$content",
                            "published": true,
                            "listed": true
                        }
                    """.trimIndent())
                    .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
            )
            .andDo(print())
    }

    @Test
    @DisplayName("글 수정")
    @Rollback(false)
    fun modify1() {
        // given
        val postId = 1L
        val title = "수정된 글 제목"
        val content = "수정된 글 내용"

        // when
        val resultActions = modifyRequest(postId, token, title, content)

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("modify"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${postId}번 글 수정이 완료되었습니다."))

        val post = postService.getItem(postId).get()
        checkPost(resultActions, post)
    }

    @Test
    @DisplayName("글 수정 2 - no apiKey")
    fun modify2() {
        // given
        val postId = 1L
        val emptyToken = ""
        val title = "수정된 글 제목"
        val content = "수정된 글 내용"

        // when
        val resultActions = modifyRequest(postId, emptyToken, title, content)

        // then
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."))
    }

    @Test
    @DisplayName("글 수정 3 - no input data")
    fun modify3() {
        // given
        val postId = 1L
        val title = ""
        val content = ""

        // when
        val resultActions = modifyRequest(postId, token, title, content)

        // then
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("modify"))
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(jsonPath("$.msg").value("""
                content : NotBlank : must not be blank
                title : NotBlank : must not be blank
            """.trimIndent()))
    }

    @Test
    @DisplayName("글 수정 4 - no permission")
    fun modify4() {
        // given
        val postId = 3L
        val title = "다른 유저의 글 제목 수정"
        val content = "다른 유저의 글 내용 수정"

        // when
        val resultActions = modifyRequest(postId, token, title, content)

        // then
        resultActions
            .andExpect(status().isForbidden())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("modify"))
            .andExpect(jsonPath("$.code").value("403-1"))
            .andExpect(jsonPath("$.msg").value("자신이 작성한 글만 수정 가능합니다."))
    }

    private fun deleteRequest(postId: Long, apiKey: String): ResultActions {
        return mvc
            .perform(
                delete("/api/v1/posts/$postId")
                    .header("Authorization", "Bearer $apiKey")
            )
            .andDo(print())
    }

    @Test
    @DisplayName("글 삭제")
    fun delete1() {
        // given
        val postId = 1L

        // when
        val resultActions = deleteRequest(postId, token)

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("delete"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${postId}번 글 삭제가 완료되었습니다."))
    }

    @Test
    @DisplayName("글 삭제2 - no apiKey")
    fun delete2() {
        // given
        val postId = 1L
        val emptyToken = ""

        // when
        val resultActions = deleteRequest(postId, emptyToken)

        // then
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."))
    }

    @Test
    @DisplayName("글 삭제3 - no permission")
    fun delete3() {
        // given
        val postId = 3L

        // when
        val resultActions = deleteRequest(postId, token)

        // then
        resultActions
            .andExpect(status().isForbidden())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("delete"))
            .andExpect(jsonPath("$.code").value("403-1"))
            .andExpect(jsonPath("$.msg").value("자신이 작성한 글만 삭제 가능합니다."))
    }

    @Test
    @DisplayName("통계 - 관리자 기능 - 관리자 접근")
    @WithUserDetails("admin")
    fun statisticsAdmin() {
        // when
        val resultActions = mvc.perform(
            get("/api/v1/posts/statistics")
        )
            .andDo(print())

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("getStatistics"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("통계 조회가 완료되었습니다."))
            .andExpect(jsonPath("$.data.postCount").value(10))
            .andExpect(jsonPath("$.data.postPublishedCount").value(10))
            .andExpect(jsonPath("$.data.postListedCount").value(10))
    }

    @Test
    @DisplayName("통계 - 관리자 기능 - user1 접근")
    @WithUserDetails("user1")
    fun statisticsUser() {
        // when
        val resultActions = mvc.perform(
            get("/api/v1/posts/statistics")
        )
            .andDo(print())

        // then
        resultActions
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("403-1"))
            .andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."))
    }

    @Test
    @DisplayName("임시글 생성")
    @WithUserDetails("user1")
    fun writeTemp() {
        // when
        val resultActions = mvc
            .perform(
                post("/api/v1/posts/temp")
            )
            .andDo(print())

        // then
        resultActions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("201-1"))
    }

    @Test
    @DisplayName("임시글 생성, 이미 임시글이 있다면 생성하지 않음")
    @WithUserDetails("user1")
    fun AlreadyExistsTemp() {
        // given
        val resultActions1 = mvc
            .perform(
                post("/api/v1/posts/temp")
            )
            .andDo(print())

        // when
        val resultActions2 = mvc
            .perform(
                post("/api/v1/posts/temp")
            )
            .andDo(print())

        // then
        resultActions2
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200-1"))
    }
}