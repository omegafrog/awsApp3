package com.example.upload.domain.post.post.entity

import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.post.comment.entity.Comment
import com.example.upload.domain.post.genFile.entity.PostGenFile
import com.example.upload.global.dto.Empty
import com.example.upload.global.dto.RsData
import com.example.upload.global.entity.BaseTime
import com.example.upload.global.exception.ServiceException
import com.example.upload.standard.util.Ut
import jakarta.persistence.*
import java.util.*

@Entity
class Post() :
    BaseTime() {

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var author: Member
    lateinit var title: String
    lateinit var content: String
    var published: Boolean = false
    var listed: Boolean = false

    @OneToMany(mappedBy = "post", cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    val comments: MutableList<Comment> = mutableListOf()

    @OneToMany(mappedBy = "post", cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    val genFiles: MutableList<PostGenFile> = mutableListOf()

    constructor(author: Member,
                title: String,
                content: String,
                published: Boolean,
                listed: Boolean) : this() {
        this.author = author
        this.title = title
        this.content = content
        this.published = published
        this.listed = listed
    }

    fun addGenFile(typeCode: PostGenFile.TypeCode, filePath: String): PostGenFile {
        return addGenFile(typeCode, 0, filePath)
    }

    private fun addGenFile(typeCode: PostGenFile.TypeCode, fileNo: Int, filePath: String): PostGenFile {
        var fileNo = fileNo
        val originalFileName = Ut.file.getOriginalFileName(filePath)
        val fileExt = Ut.file.getFileExt(filePath)
        val fileExtTypeCode = Ut.file.getFileExtTypeCodeFromFileExt(fileExt)
        val fileExtType2Code = Ut.file.getFileExtType2CodeFromFileExt(fileExt)

        val metadata = Ut.file.getMetadata(filePath)

        val metadataStr = metadata
            .entries
            .map { entry: Map.Entry<String, Any> -> entry.key + "-" + entry.value }
            .joinToString(";") { it }

        val fileName = "${UUID.randomUUID()}.$fileExt"
        val fileSize = Ut.file.getFileSize(filePath)
        fileNo = if (fileNo == 0) getNextGenFileNo(typeCode) else fileNo

        val genFile =
            PostGenFile(
                this,
                typeCode,
                fileNo,
                originalFileName,
                metadataStr,
                Ut.date.getCurrentDateFormatted("yyyy_MM_dd"),
                fileExtTypeCode,
                fileExtType2Code,
                fileExt,
                fileName,
                fileSize
            )

        genFiles.add(genFile)
        Ut.file.mv(filePath, genFile.filePath)

        return genFile
    }

    private fun getNextGenFileNo(typeCode: PostGenFile.TypeCode): Int {
        return genFiles
            .filter { it.typeCode == typeCode }
            .maxOfOrNull { it.fileNo }
            ?.plus(1) ?: 1
    }

    fun getGenFileByTypeCodeAndFileNo(typeCode: PostGenFile.TypeCode, fileNo: Int): PostGenFile? {
        return genFiles
            .filter { genFile: PostGenFile -> genFile.typeCode == typeCode }
            .firstOrNull { it.fileNo == fileNo }
    }

    fun deleteGenFile(typeCode: PostGenFile.TypeCode, fileNo: Int) {
        getGenFileByTypeCodeAndFileNo(typeCode, fileNo)?.let {
            genFiles.remove(it)
            Ut.file.rm(it.filePath)
        }
    }

    fun modifyGenFile(typeCode: PostGenFile.TypeCode, fileNo: Int, filePath: String) {
        getGenFileByTypeCodeAndFileNo(
            typeCode,
            fileNo
        )
            ?.let { genFile: PostGenFile ->
                Ut.file.rm(genFile.filePath)
                val originalFileName = Ut.file.getOriginalFileName(filePath)
                val fileExt = Ut.file.getFileExt(filePath)
                val fileExtTypeCode = Ut.file.getFileExtTypeCodeFromFileExt(fileExt)
                val fileExtType2Code = Ut.file.getFileExtType2CodeFromFileExt(fileExt)

                val metadata = Ut.file.getMetadata(filePath)

                val metadataStr = metadata
                    .entries
                    .map { entry: Map.Entry<String, Any> -> entry.key + "-" + entry.value }
                    .joinToString(";") { it }

                val fileName = "${UUID.randomUUID()}.$fileExt"
                val fileSize = Ut.file.getFileSize(filePath)

                genFile.originalFileName = originalFileName
                genFile.metadata = metadataStr
                genFile.fileDateDir = Ut.date.getCurrentDateFormatted("yyyy_MM_dd")
                genFile.fileExt = fileExt
                genFile.fileExtTypeCode = fileExtTypeCode
                genFile.fileExtType2Code = fileExtType2Code
                genFile.fileName = fileName
                genFile.fileSize = fileSize
                Ut.file.mv(filePath, genFile.filePath)
            }
    }

    fun putGenFile(typeCode: PostGenFile.TypeCode, fileNo: Int, filePath: String) {
        getGenFileByTypeCodeAndFileNo(
            typeCode,
            fileNo
        )?.let {
            modifyGenFile(typeCode, fileNo, filePath)
        } ?: addGenFile(typeCode, fileNo, filePath)
    }

    fun addComment(author: Member, content: String): Comment {

        val comment = Comment().apply {
            this.post = this@Post
            this.author = author
            this.content = content
        }

        comments.add(comment)

        return comment
    }

    fun getCommentById(id: Long): Comment {
        return comments.firstOrNull { it.id == id }?.let {
            return it
        } ?: throw ServiceException("404-2", "존재하지 않는 댓글입니다.")

    }

    fun deleteComment(comment: Comment) {
        comments.remove(comment)
    }

    fun canModify(actor: Member) {
        if (actor.isAdmin) return
        if (actor == this.author) return
        throw ServiceException("403-1", "자신이 작성한 글만 수정 가능합니다.")
    }

    fun canDelete(actor: Member) {
        if (actor.isAdmin) return
        if (actor == this.author) return
        throw ServiceException("403-1", "자신이 작성한 글만 삭제 가능합니다.")
    }

    fun canRead(actor: Member) {
        if (actor == this.author) return
        if (actor.isAdmin) return

        throw ServiceException("403-1", "비공개 설정된 글입니다.")
    }

    val latestComment: Comment
        get() = comments.maxByOrNull { it.id!! } ?: throw ServiceException("404-2", "존재하지 않는 댓글입니다.")

    fun getHandleAuthority(actor: Member?): Boolean {
        if (actor == null) return false
        if (actor.isAdmin) return true

        return actor == this.author
    }

    fun checkActorCanMakeNewGenFile(actor: Member) {
        getCheckActorCanMakeNewGenFileRs(actor)
            .takeIf { it.isFail }
            ?.let {
                throw ServiceException(it.code, it.msg)
            }
    }

    fun getCheckActorCanMakeNewGenFileRs(actor: Member): RsData<Empty> {
//        if (actor == null) return RsData("401-1", "로그인 후 이용해주세요.")
        if (actor == author) return RsData.OK
        return RsData("403-1", "작성자만 파일을 업로드할 수 있습니다.")
    }

    val isTemp: Boolean
        get() = !published && "임시글" == title
}