package ru.netology.nework.enumeration

enum class EventType {
    ONLINE, OFFLINE,
}

fun String.toEventType(): EventType{
    return when(this){
        "ONLINE" -> EventType.ONLINE
        "OFFLINE" -> EventType.OFFLINE
        else -> EventType.ONLINE
    }
}