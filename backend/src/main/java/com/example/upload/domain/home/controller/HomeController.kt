package com.example.upload.domain.home.controller;

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Tag(name = "HomeController", description = "API 서버 홈")
@Controller
class HomeController {

    @Operation(summary = "API 서버 시작페이지", description = "API 서버 시작페이지입니다. api 호출은 인증을 해주세요")
    @GetMapping(value = ["/"], produces = ["text/plain;charset=UTF-8"])
    @ResponseBody
    fun home(): String {
        return "API 서버에 오신 걸 환영합니다.";
    }

    @GetMapping("/session")
    @ResponseBody
    fun session(session: HttpSession): MutableMap<String, Any> {
        val sessionMap = mutableMapOf<String, Any>()

        val names = session.attributeNames

        while(names.hasMoreElements()) {
            val name = names.nextElement()
            session.getAttribute(name)?.let {
                sessionMap[name] = it
            }
        }

        return sessionMap
    }

}
