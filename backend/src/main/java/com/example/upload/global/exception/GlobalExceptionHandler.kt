package com.example.upload.global.exception

import com.example.upload.global.app.AppConfig.Companion.getSpringServletMultipartMaxFileSize
import com.example.upload.global.app.AppConfig.Companion.isNotProd
import com.example.upload.global.dto.Empty
import com.example.upload.global.dto.RsData
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<RsData<Void>> {
        val message = e.bindingResult.fieldErrors
            .map { fe: FieldError -> fe.field + " : " + fe.code + " : " + fe.defaultMessage }
            .sorted()
            .joinToString("\n") { it }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                RsData(
                    "400-1",
                    message
                )
            )
    }


    @ResponseStatus
    @ExceptionHandler(
        ServiceException::class
    )
    fun ServiceExceptionHandle(ex: ServiceException): ResponseEntity<RsData<Void>> {
        // 개발 모드에서만 작동되도록.

        if (isNotProd) ex.printStackTrace()

        return ResponseEntity
            .status(ex.statusCode)
            .body(
                RsData(
                    ex.code,
                    ex.msg
                )
            )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun MethodArgumentTypeMismatchExceptionHandle(ex: MethodArgumentTypeMismatchException): ResponseEntity<RsData<Void>> {
        if (isNotProd) ex.printStackTrace()

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                RsData(
                    "400-1",
                    "잘못된 요청입니다."
                )
            )
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handle(ex: MaxUploadSizeExceededException): ResponseEntity<RsData<Empty>> {
        if (isNotProd) ex.printStackTrace()

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                RsData(
                    "413-1",
                    "업로드되는 개별 파일의 용량은 ${getSpringServletMultipartMaxFileSize()}(을)를 초과할 수 없습니다."
                )
            )
    }

    @ExceptionHandler(RuntimeException::class)
    fun RuntimeException(ex: RuntimeException): ResponseEntity<RsData<Void>> {
        if (isNotProd) ex.printStackTrace()

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                RsData(
                    "500",
                    "에러 발생"
                )
            )
    }
}
