package com.example.upload.domain.post.genFile.entity

import com.example.upload.domain.base.genFile.genFile.entity.GenFile
import com.example.upload.domain.post.post.entity.Post
import jakarta.persistence.*

@Entity
class PostGenFile : GenFile {

    enum class TypeCode {
        attachment,
        thumbnail
    }

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var post: Post

    @Enumerated(EnumType.STRING)
    lateinit var typeCode: TypeCode

    constructor(
        post: Post, typeCode: TypeCode, fileNo: Int, originalFileName: String, metadataStr: String,
        yyyyMmDd: String, fileExtTypeCode: String, fileExtType2Code: String, fileExt: String,
        fileName: String, fileSize: Long
    ) : super(
        fileNo, originalFileName, metadataStr, yyyyMmDd, fileExt, fileExtTypeCode, fileExtType2Code,
        fileName, fileSize
    ) {
        this.post = post
        this.typeCode = typeCode
    }

    override fun getOwnerModelId(): Long {
        return post.id!!
    }

    override fun getTypeCodeAsStr(): String {
        return typeCode.name
    }
}