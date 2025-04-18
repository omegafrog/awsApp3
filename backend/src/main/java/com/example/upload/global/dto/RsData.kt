package com.example.upload.global.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
@JsonInclude(JsonInclude.Include.NON_NULL)
class RsData<T>(
    val code: String,
    val msg: String,
    val data: T
) {
    constructor(code: String, msg: String) : this(code, msg, Empty() as T)

    @get:JsonIgnore
    val statusCode: Int
        get() {
            val statusCodeStr = code.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            return statusCodeStr.toInt()
        }

    @get:JsonIgnore
    val isSuccess: Boolean
        get() = statusCode < 400

    @get:JsonIgnore
    val isFail: Boolean
        get() = !isSuccess

    fun <T> newDataOf(data: T): RsData<T> {
        return RsData(code, msg, data)
    }

    companion object {
        val OK: RsData<Empty> = RsData("200-1", "OK", Empty())
    }
}