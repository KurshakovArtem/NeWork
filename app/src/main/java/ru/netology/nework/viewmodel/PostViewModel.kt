package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ru.netology.nework.dto.Post
import ru.netology.nework.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(private val repository: PostRepository) : ViewModel() {


    val data: LiveData<List<Post>> = repository.data
        .catch { it.printStackTrace() }  // реализовать обработку ошибок(snackbar)
        .asLiveData(Dispatchers.Default)

    fun loadPosts(){
        viewModelScope.launch {
            repository.getAllPosts()
        }
    }

}