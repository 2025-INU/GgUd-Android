package com.capstone.ggud.ui.components

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun formatIsoToDotTime(iso: String): String {
    return runCatching {
        val ldt = LocalDateTime.parse(iso)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd • HH:mm")
        ldt.format(formatter)
    }.getOrElse { "-" }
}