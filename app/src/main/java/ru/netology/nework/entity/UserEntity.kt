package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.User

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String? = null,
) {
    fun toUser() = User(
        id = id,
        login = login,
        name = name,
        avatar = avatar,
    )

    companion object {
        fun fromUser(dto: User) = UserEntity(
            id = dto.id,
            login = dto.login,
            name = dto.name,
            avatar = dto.avatar,
        )
    }
}

fun List<UserEntity>.toUser(): List<User> = map(UserEntity::toUser)
fun List<User>.toUserEntity(): List<UserEntity> = map(UserEntity::fromUser)