package ru.netology.nework.enumeration

enum class AttachmentType {
    IMAGE, VIDEO, AUDIO, EMPTY
}

fun String.toAttachmentType(): AttachmentType{
    return when(this){
        "IMAGE" -> AttachmentType.IMAGE
        "VIDEO" -> AttachmentType.VIDEO
        "AUDIO" -> AttachmentType.AUDIO
        else ->  AttachmentType.EMPTY
    }
}