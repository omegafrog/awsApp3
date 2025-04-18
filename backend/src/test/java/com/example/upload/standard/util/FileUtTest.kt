package com.example.upload.standard.util

import com.example.upload.global.app.AppConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class FileUtTest {

    @Test
    @DisplayName("downloadByHttp")
    fun t1() {
        // given
        val imageUrl = "https://picsum.photos/id/237/200/300"
        val tempDir = AppConfig.getTempDirPath()

        // when
        val newFilePath = Ut.file.downloadByHttp(imageUrl, tempDir)

        // then
        assertThat(newFilePath).endsWith(".jpg")

        // cleanup
        Ut.file.delete(newFilePath)
    }

    @Test
    @DisplayName("getExtensionByTika")
    fun tika() {
        // given
        val imageUrl = "https://picsum.photos/id/237/200/300"
        val tempDir = AppConfig.getTempDirPath()
        val newFilePath = Ut.file.downloadByHttp(imageUrl, tempDir)

        // when
        val ext = Ut.file.getExtensionByTika(newFilePath)

        // then
        assertThat(ext).isEqualTo("jpg")

        // cleanup
        Ut.file.delete(newFilePath)
    }
}