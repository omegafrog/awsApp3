package com.example.upload.global.app

import com.fasterxml.jackson.databind.ObjectMapper
import lombok.SneakyThrows
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.io.ClassPathResource

@Configuration
class AppConfig {

    companion object {
        private lateinit var objectMapper: ObjectMapper
        private lateinit var genFileDirPath: String
        private lateinit var environment: Environment
        private lateinit var siteBackUrl: String
        private lateinit var siteFrontUrl: String
        private lateinit var springServletMultipartMaxFileSize: String
        private lateinit var springServletMultipartMaxRequestSize: String
        private lateinit var tika: Tika
        private var resourcesSampleDirPath: String? = null
        
        fun getObjectMapper(): ObjectMapper = objectMapper
        
        fun getGenFileDirPath(): String = genFileDirPath
        
        fun getSiteBackUrl(): String = siteBackUrl
        
        fun getSiteFrontUrl(): String = siteFrontUrl
        
        fun getSpringServletMultipartMaxFileSize(): String = springServletMultipartMaxFileSize
        
        fun getSpringServletMultipartMaxRequestSize(): String = springServletMultipartMaxRequestSize

        
        fun getTempDirPath(): String = System.getProperty("java.io.tmpdir")

        
        fun getTika(): Tika = tika


        fun getResourcesSampleDirPath(): String {
            if (resourcesSampleDirPath == null) {
                val resource = ClassPathResource("sample")

                resourcesSampleDirPath = if (resource.exists()) {
                    resource.file.absolutePath
                } else {
                    "src/main/resources/sample"
                }
            }

            return resourcesSampleDirPath!!
        }
        
        val isNotProd: Boolean
            get() = !isProd

        
        val isProd: Boolean
            get() = environment.matchesProfiles("prod")

        
        val isDev: Boolean
            get() = environment.matchesProfiles("dev")

        
        val isTest: Boolean
            get() = environment.matchesProfiles("test")

    }

    @Value("\${custom.site.backUrl}")
    fun setSiteBackUrl(siteBackUrl: String) {
        Companion.siteBackUrl = siteBackUrl
    }

    @Value("\${custom.site.frontUrl}")
    fun setSiteFrontUrl(siteFrontUrl: String) {
        Companion.siteFrontUrl = siteFrontUrl
    }

    @Autowired
    fun setEnvironment(environment: Environment) {
        Companion.environment = environment
    }

    @Value("\${custom.genFile.dirPath}")
    fun setGenFileDirPath(genFileDirPath: String) {
        Companion.genFileDirPath = genFileDirPath
    }

    @Autowired
    fun setObjectMapper(objectMapper: ObjectMapper) {
        Companion.objectMapper = objectMapper
    }

    @Autowired
    fun setTika(tika: Tika) {
        Companion.tika = tika
    }

    @Value("\${spring.servlet.multipart.max-file-size}")
    fun setSpringServletMultipartMaxFileSize(springServletMultipartMaxFileSize: String) {
        Companion.springServletMultipartMaxFileSize = springServletMultipartMaxFileSize
    }

    @Value("\${spring.servlet.multipart.max-request-size}")
    fun setSpringServletMultipartMaxRequestSize(springServletMultipartMaxRequestSize: String) {
        Companion.springServletMultipartMaxRequestSize = springServletMultipartMaxRequestSize
    }
}
