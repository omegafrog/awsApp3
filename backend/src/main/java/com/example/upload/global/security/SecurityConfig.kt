package com.example.upload.global.security
import com.example.upload.global.app.AppConfig
import com.example.upload.global.dto.Empty
import com.example.upload.global.dto.RsData
import com.example.upload.standard.util.Ut
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customAuthenticationFilter: CustomAuthenticationFilter,
    private val customAuthorizationRequestResolver: CustomAuthorizationRequestResolver,
    private val customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler
) {

    @Bean
    fun securityFilterChain(http: org.springframework.security.config.annotation.web.builders.HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/h2-console/**", permitAll)
                authorize(HttpMethod.GET, "/api/*/posts/{id:\\d+}", permitAll)
                authorize(HttpMethod.GET, "/api/*/posts", permitAll)
                authorize(HttpMethod.GET, "/api/*/posts/{postId:\\d+}/comments", permitAll)
                authorize(HttpMethod.GET, "/api/*/posts/{postId:\\d+}/genFiles", permitAll)
                authorize("/api/*/members/login", permitAll)
                authorize("/api/*/members/join", permitAll)
                authorize("/api/*/members/logout", permitAll)
                authorize("/api/v1/posts/statistics", hasRole("ADMIN"))
                authorize("/api/*/**", authenticated)
                authorize(anyRequest, permitAll)
            }

            headers {
                frameOptions {
                    sameOrigin = true
                }
            }

            csrf { disable() }

            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }

            oauth2Login {
                authenticationSuccessHandler = customAuthenticationSuccessHandler
                authorizationEndpoint {
                    authorizationRequestResolver = customAuthorizationRequestResolver
                }
            }

            addFilterBefore<UsernamePasswordAuthenticationFilter>(customAuthenticationFilter)

            exceptionHandling {
                authenticationEntryPoint = AuthenticationEntryPoint { request, response, authException ->
                    response.contentType = "application/json;charset=UTF-8"
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.writer.write(
                        Ut.json.toString(RsData<Empty>("401-1", "잘못된 인증키입니다."))
                    )
                }

                accessDeniedHandler = AccessDeniedHandler { request, response, accessDeniedException ->
                    response.contentType = "application/json;charset=UTF-8"
                    response.status = HttpServletResponse.SC_FORBIDDEN
                    response.writer.write(
                        Ut.json.toString(RsData<Empty>("403-1", "접근 권한이 없습니다."))
                    )
                }
            }
        }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("https://cdpn.io", AppConfig.getSiteFrontUrl())
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
            allowCredentials = true
            allowedHeaders = listOf("*")
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/api/**", configuration)
        }
    }
}