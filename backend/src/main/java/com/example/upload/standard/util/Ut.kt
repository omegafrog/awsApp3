package com.example.upload.standard.util

import com.example.upload.global.app.AppConfig
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import lombok.SneakyThrows
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO

class Ut {
    object file {
        private const val ORIGINAL_FILE_NAME_SEPARATOR = "--originalFileName_"
        private val MIME_TYPE_MAP = linkedMapOf(
            "application/json" to "json",
            "text/plain" to "txt",
            "text/html" to "html",
            "text/css" to "css",
            "application/javascript" to "js",
            "image/jpeg" to "jpg",
            "image/png" to "png",
            "image/gif" to "gif",
            "image/webp" to "webp",
            "image/svg+xml" to "svg",
            "application/pdf" to "pdf",
            "application/xml" to "xml",
            "application/zip" to "zip",
            "application/gzip" to "gz",
            "application/x-tar" to "tar",
            "application/x-7z-compressed" to "7z",
            "application/vnd.rar" to "rar",
            "audio/mpeg" to "mp3",
            "audio/x-m4a" to "m4a",
            "audio/mp4" to "m4a",
            "audio/wav" to "wav",
            "video/quicktime" to "mov",
            "video/mp4" to "mp4",
            "video/webm" to "webm",
            "video/x-msvideo" to "avi"
        )


        fun getFileExtTypeCodeFromFileExt(ext: String): String {
            return when (ext) {
                "jpeg", "jpg", "gif", "png", "svg", "webp" -> "img"
                "mp4", "avi", "mov" -> "video"
                "mp3", "m4a" -> "audio"
                else -> "etc"
            }
        }

        fun getFileExtType2CodeFromFileExt(ext: String): String {
            return when (ext) {
                "jpeg", "jpg" -> "jpg"
                else -> ext
            }
        }

        fun getMetadata(filePath: String): Map<String, Any> {
            val ext = getFileExt(filePath)
            val fileExtTypeCode = getFileExtTypeCodeFromFileExt(ext)

            if (fileExtTypeCode == "img") return getImgMetadata(filePath)

            return java.util.Map.of()
        }

        private fun getImgMetadata(filePath: String): Map<String, Any> {
            val metadata: MutableMap<String, Any> = LinkedHashMap()

            try {
                ImageIO.createImageInputStream(File(filePath)).use { input ->
                    val readers = ImageIO.getImageReaders(input)
                    if (!readers.hasNext()) {
                        throw IOException("지원되지 않는 이미지 형식: $filePath")
                    }

                    val reader = readers.next()
                    reader.input = input

                    val width = reader.getWidth(0)
                    val height = reader.getHeight(0)

                    metadata["width"] = width
                    metadata["height"] = height
                    reader.dispose()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return metadata
        }
        
        @SneakyThrows
        fun downloadByHttp(url: String, dirPath: String): String {
            return downloadByHttp(url, dirPath, true)
        }

        @SneakyThrows
        fun downloadByHttp(url: String, dirPath: String, uniqueFilename: Boolean): String {            // HttpClient 생성
            val client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()

            // HTTP 요청 생성
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build()

            val tempFilePath = dirPath + "/" + UUID.randomUUID() + ".tmp"
            mkdir(dirPath)


            // 요청 보내고 응답 받기
            val response = client.send(
                request, HttpResponse.BodyHandlers.ofFile(Path.of(tempFilePath))
            )

            var extension = getExtensionFromResponse(response)

            if (extension == "tmp") {
                extension = getExtensionByTika(tempFilePath)
            }

            // 파일명 추출
            var filename = getFilenameWithoutExtFromUrl(url)

            filename = if (uniqueFilename)
                UUID.randomUUID().toString() + ORIGINAL_FILE_NAME_SEPARATOR + filename
            else
                filename

            val newFilePath = "$dirPath/$filename.$extension"

            mv(tempFilePath, newFilePath)

            return newFilePath
        }

        
        fun getExtensionByTika(filePath: String?): String {
            val mineType = AppConfig.getTika().detect(filePath)

            return MIME_TYPE_MAP.getOrDefault(mineType, "tmp")
        }

        fun getOriginalFileName(filePath: String): String {
            val originalFileName = Path.of(filePath).fileName.toString()

            return if (originalFileName.contains(ORIGINAL_FILE_NAME_SEPARATOR))
                originalFileName.substring(originalFileName.indexOf(ORIGINAL_FILE_NAME_SEPARATOR) + ORIGINAL_FILE_NAME_SEPARATOR.length)
            else
                originalFileName
        }

        fun getFileExt(filePath: String): String {
            val filename = getOriginalFileName(filePath)

            return if (filename.contains("."))
                filename.substring(filename.lastIndexOf('.') + 1)
            else
                ""
        }

        @SneakyThrows
        fun getFileSize(filePath: String): Long {
            return Files.size(Path.of(filePath))
        }

        @SneakyThrows
        fun mv(oldFilePath: String, newFilePath: String) {
            mkdir(Paths.get(newFilePath).parent.toString())

            Files.move(
                Path.of(oldFilePath),
                Path.of(newFilePath),
                StandardCopyOption.REPLACE_EXISTING
            )
        }

        private fun getExtensionFromResponse(response: HttpResponse<*>): String {
            return response.headers()
                .firstValue("Content-Type")
                .map { contentType: String -> MIME_TYPE_MAP.getOrDefault(contentType, "tmp") }
                .orElse("tmp")
        }

        private fun getFilenameWithoutExtFromUrl(url: String): String {
            try {
                val path = URI(url).path
                val filename = Path.of(path).fileName.toString()
                // 확장자 제거
                return if (filename.contains("."))
                    filename.substring(0, filename.lastIndexOf('.'))
                else
                    filename
            } catch (e: URISyntaxException) {
                // URL에서 파일명을 추출할 수 없는 경우 타임스탬프 사용
                return "download_" + System.currentTimeMillis()
            }
        }

        
        @SneakyThrows
        fun toFile(multipartFile: MultipartFile?, dirPath: String): String {
            if (multipartFile == null) return ""
            if (multipartFile.isEmpty) return ""

            val filePath =
                dirPath + "/" + UUID.randomUUID() + ORIGINAL_FILE_NAME_SEPARATOR + multipartFile.originalFilename

            mkdir(dirPath)
            multipartFile.transferTo(File(filePath))

            return filePath
        }

        @SneakyThrows
        private fun mkdir(dirPath: String) {
            val path = Path.of(dirPath)

            if (Files.exists(path)) return

            Files.createDirectories(path)
        }

        
        @SneakyThrows
        fun rm(filePath: String) {
            val path = Path.of(filePath)

            if (!Files.exists(path)) return

            if (Files.isRegularFile(path)) {
                // 파일이면 바로 삭제
                Files.delete(path)
            } else {
                // 디렉터리면 내부 파일들 삭제 후 디렉터리 삭제
                Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
                    @Throws(IOException::class)
                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        Files.delete(file)
                        return FileVisitResult.CONTINUE
                    }

                    @Throws(IOException::class)
                    override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                        Files.delete(dir)
                        return FileVisitResult.CONTINUE
                    }
                })
            }
        }

        
        @SneakyThrows
        fun delete(filePath: String) {
            Files.deleteIfExists(Path.of(filePath))
        }

        @SneakyThrows
        fun copy(filePath: String, newFilePath: String) {
            mkdir(Paths.get(newFilePath).parent.toString())

            Files.copy(
                Path.of(filePath),
                Path.of(newFilePath),
                StandardCopyOption.REPLACE_EXISTING
            )
        }

        fun getContentType(fileExt: String): String {
            return MIME_TYPE_MAP.entries
                .filter { entry: Map.Entry<String, String> -> entry.value == fileExt }
                .map { it }
                .firstOrNull()?.key ?: ""
        }
    }

    object str {
        fun lcfirst(str: String): String {
            return str[0].lowercaseChar().toString() + str.substring(1)
        }
    }

    object date {
        fun getCurrentDateFormatted(pattern: String): String {
            val simpleDateFormat = SimpleDateFormat(pattern)
            return simpleDateFormat.format(Date())
        }
    }

    object url {
        
        fun encode(str: String): String {
            return try {
                URLEncoder.encode(str, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                str
            }
        }


        
        fun removeDomain(url: String): String {
            return url.replaceFirst("https?://[^/]+".toRegex(), "")
        }
    }

    object json {
        private val objectMapper: ObjectMapper = AppConfig.getObjectMapper()

        
        fun toString(obj: Any?): String {
            try {
                return objectMapper.writeValueAsString(obj)
            } catch (e: JsonProcessingException) {
                throw RuntimeException(e)
            }
        }
    }

    object jwt {

        
        fun createToken(keyString: String, expireSeconds: Int, claims: Map<String, Any>): String {
            val secretKey = Keys.hmacShaKeyFor(keyString.toByteArray())

            val issuedAt = Date()
            val expiration = Date(issuedAt.time + 1000L * expireSeconds)

            val jwt = Jwts.builder()
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact()

            return jwt
        }

        
        fun isValidToken(keyString: String, token: String?): Boolean {
            try {
                val secretKey = Keys.hmacShaKeyFor(keyString.toByteArray())

                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(token)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }

            return true
        }


        
        fun getPayload(keyString: String, jwtStr: String?): Map<String, Any> {
            val secretKey = Keys.hmacShaKeyFor(keyString.toByteArray())

            return Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parse(jwtStr)
                .payload as Map<String, Any>
        }
    }
}
