package ru.netology.nework.adapter

import ru.netology.nework.dto.Post


interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onVideo(post: Post) {}
    fun onMoveToSinglePost(post: Post) {}
    fun onSaveRefresh(post: Post) {}
    fun onMoveToSinglePhoto(post: Post) {}
}




class PostAdapter {
}