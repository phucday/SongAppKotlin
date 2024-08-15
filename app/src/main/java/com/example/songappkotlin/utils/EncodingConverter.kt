package com.example.songappkotlin.utils

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object EncodingConverter {
    fun convertToUTF8(input: String, originalEncoding: String): String? {
        return try {
            // Chuyển đổi chuỗi đầu vào sang mảng byte sử dụng mã hóa gốc
            val bytes = input.toByteArray(Charset.forName(originalEncoding))

            // Tạo chuỗi mới với mã hóa UTF-8
            String(bytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null // Hoặc ném ngoại lệ tùy thuộc vào yêu cầu
        }
    }
}