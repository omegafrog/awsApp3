package com.example.upload.domain.post.genFile.dto

import com.example.upload.domain.post.genFile.entity.PostGenFile
import java.time.LocalDateTime

class PostGenFileDto(
    val id: Long,
    val createdDate: LocalDateTime,
    val modifiedDate: LocalDateTime,
    val postId: Long,
    val fileName: String,
    val typeCode: String,
    val fileExtTypeCode: String,
    val fileExtType2Code: String,
    val fileSize: Long,
    val fileNo: Long,
    val fileExt: String,
    val fileDateDir: String,
    val originalFileName: String,
    val downloadUrl: String,
    val publicUrl: String
){
    constructor(postGenFile: PostGenFile): this(
        id = postGenFile.id!!,
        createdDate = postGenFile.createdDate,
        modifiedDate = postGenFile.modifiedDate,
        postId = postGenFile.post.id!!,
        fileName = postGenFile.fileName,
        typeCode = postGenFile.typeCode.name,
        fileExtTypeCode = postGenFile.fileExtTypeCode,
        fileExtType2Code = postGenFile.fileExtType2Code,
        fileSize = postGenFile.fileSize,
        fileNo = postGenFile.fileNo.toLong(),
        fileExt = postGenFile.fileExt,
        fileDateDir = postGenFile.fileDateDir,
        originalFileName = postGenFile.originalFileName,
        downloadUrl = postGenFile.downloadUrl,
        publicUrl = postGenFile.publicUrl
    )

}
