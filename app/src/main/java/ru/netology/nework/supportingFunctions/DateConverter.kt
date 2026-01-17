package ru.netology.nework.supportingFunctions

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date


fun convertResponseToCardPost(date: String): String {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    val dateTime = LocalDateTime.parse(date.replace("Z", "+00:00"), formatter)
    val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    return dateTime.format(outputFormatter)
}

fun String.toDate(): Date?{
    return try {
        val instant = Instant.parse(this)
        Date.from(instant.toJavaInstant())
    } catch (e: Exception) {
        null
    }
}