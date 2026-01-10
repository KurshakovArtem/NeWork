package ru.netology.nework.supportingFunctions

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun convertResponseToCardPost(date: String): String {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    val dateTime = LocalDateTime.parse(date.replace("Z", "+00:00"), formatter)
    val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    return dateTime.format(outputFormatter)
}