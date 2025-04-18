package com.example.upload.global.exception;

import com.example.upload.global.dto.Empty
import com.example.upload.global.dto.RsData

class ServiceException(
    private val _code: String,
    override val message: String
): RuntimeException(message) {

    private var rsData: RsData<Empty> = RsData(_code, message)

    val code: String
        get() = rsData.code

    val msg: String
        get() = rsData.msg

    val statusCode: Int
        get() = rsData.statusCode

}
