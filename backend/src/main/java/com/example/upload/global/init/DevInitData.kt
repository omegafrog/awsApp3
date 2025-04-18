package com.example.upload.global.init;

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@Profile("dev")
@Configuration
class DevInitData {

    @Bean
    fun devApplicationRunner(): ApplicationRunner {
        return ApplicationRunner {
            genApiJsonFile("http://localhost:8080/v3/api-docs/apiV1", "apiV1.json")
            runCmd(
                listOf(
                    "cmd.exe",
                    "/c",
                    "npx --package typescript --package openapi-typescript --package punycode openapi-typescript apiV1.json -o ../frontend/src/lib/backend/apiV1/schema.d.ts"
                )
            );
        };
    }

    fun runCmd(command: List<String>) {
        val processBuilder = ProcessBuilder(command)
        processBuilder.redirectErrorStream(true) // 표준 에러를 표준 출력과 합침

        val process = processBuilder.start()

        process.inputStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                println(line)
            }
        }

        val exitCode = process.waitFor()
        println("프로세스 종료 코드: $exitCode")
    }

    fun genApiJsonFile(url: String, filename: String) {
        val filePath = Path.of(filename) // 저장할 파일명

        val client = HttpClient.newHttpClient()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 200) {
            Files.writeString(
                filePath,
                response.body(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            println("JSON 데이터가 ${filePath.toAbsolutePath()}에 저장되었습니다.");
        } else {
            println("오류: HTTP 상태 코드 ${response.statusCode()}")
        }
    }
}
