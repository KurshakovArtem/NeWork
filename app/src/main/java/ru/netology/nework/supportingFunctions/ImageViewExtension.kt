package ru.netology.nework.supportingFunctions

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.nework.R

fun ImageView.loadAvatar(url: String){
    Glide.with(this)
        .load(url)
        .circleCrop()
        .placeholder(R.drawable.ic_loading_100dp)
        .error(R.drawable.ic_error_100dp)
        .timeout(10_000)
        .into(this)
}

fun ImageView.loadAttachmentImage(url: String){
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.ic_loading_100dp)
        .error(R.drawable.ic_error_100dp)
        .timeout(10_000)
        .into(this)
}

fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}