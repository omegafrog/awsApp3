package com.example.upload.domain.base.genFile.genFile.entity;

import com.example.upload.global.app.AppConfig
import com.example.upload.global.entity.BaseTime
import jakarta.persistence.MappedSuperclass
import java.util.*

@MappedSuperclass
abstract class GenFile(
    var fileNo: Int,
    var originalFileName: String,
    var metadata: String,
    var fileDateDir: String,
    var fileExt: String,
    var fileExtTypeCode: String,
    var fileExtType2Code: String,
    var fileName: String,
    var fileSize: Long
) : BaseTime() {

    val filePath: String
        get() = AppConfig.getGenFileDirPath() + "/" + modelName + "/" + getTypeCodeAsStr() + "/" + fileDateDir + "/" + fileName

    override fun equals(other: Any?): Boolean {
        if (id != null) return super.equals(other);

        if (other == null || javaClass != other.javaClass) return false;
        if (!super.equals(other)) return false;
        val that = other as GenFile
        return fileNo == that.fileNo && Objects.equals(getTypeCodeAsStr(), that.getTypeCodeAsStr());
    }

    override fun hashCode(): Int {
        if (id != null) return super.hashCode();
        return Objects.hash(super.hashCode(), getTypeCodeAsStr(), fileNo);
    }

    private val ownerModelName
        get() = modelName.replace("GenFile", "")


    val downloadUrl: String
        get() = AppConfig.getSiteBackUrl() + "/" + ownerModelName + "/genFile/download/" + getOwnerModelId() + "/" + fileName;

    val publicUrl: String
        get() = AppConfig.getSiteBackUrl() + "/gen/" + modelName + "/" + getTypeCodeAsStr() + "/" + fileDateDir + "/" + fileName;

    protected abstract fun getOwnerModelId(): Long
    protected abstract fun getTypeCodeAsStr(): String
}