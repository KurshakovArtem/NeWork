package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.dto.Post
import ru.netology.nework.model.FeedModel
import ru.netology.nework.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(private val repository: PostRepository) : ViewModel() {


    val data: LiveData<FeedModel> = repository.data
        .map(::FeedModel)
        .catch { it.printStackTrace() }
        .asLiveData(Dispatchers.Default)

    fun loadPosts(){
        viewModelScope.launch {
            repository.getAllPosts()
        }
    }

}