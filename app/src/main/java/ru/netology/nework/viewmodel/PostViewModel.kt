package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.MentionUser
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.toMentionUser
import ru.netology.nework.model.ErrorReport
import ru.netology.nework.model.EventFeedModel
import ru.netology.nework.model.FeedErrorMassage
import ru.netology.nework.model.PostFeedModel
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.EventRepository
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.supportingFunctions.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val creatingPost = Post(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    published = "",
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val eventRepository: EventRepository,
    appAuth: AppAuth
) : ViewModel() {


    @OptIn(ExperimentalCoroutinesApi::class)
    val postData: LiveData<PostFeedModel> = appAuth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            postRepository.data.map { posts ->
                PostFeedModel(
                    posts.map { it.copy(ownedByMe = myId == it.authorId) }
                )
            }
        }
        .catch { it.printStackTrace() }
        .asLiveData(Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventData: LiveData<EventFeedModel> = appAuth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            eventRepository.events.map { events ->
                EventFeedModel(
                    events.map { it.copy(ownedByMe = myId == it.authorId) }
                )
            }
        }
        .catch { it.printStackTrace() }
        .asLiveData(Dispatchers.Default)


    /*
    Поскольку из за BottomNavigationView вся вёрстка снизу плывёт,
    будем выставлять паддинг снизу для каждого Barr или View вручную
    padding берём из MainActivity системных настроек
     */
    var padding: Int = 0
    private val _mentionUsers = MutableStateFlow<List<MentionUser>>(emptyList())
    val mentionUsersFlow: StateFlow<List<MentionUser>>
        get() = _mentionUsers.asStateFlow()
    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _photo = MutableLiveData<PhotoModel?>()
    val photo: LiveData<PhotoModel?>
        get() = _photo

    private val _edited = MutableLiveData(creatingPost)
    val edited: LiveData<Post>
        get() = _edited

    private val _locationState = MutableLiveData<Coordinates?>()
    val locationState: LiveData<Coordinates?>
        get() = _locationState

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated


    init {
        loadPosts()
        loadUsers()
        loadEvents()
    }

    fun postRefresh() {
        viewModelScope.launch {
            _dataState.value = FeedModelState(refreshing = true)
            try {
                postRepository.getAllPosts()
                _dataState.value = FeedModelState()
            } catch (_: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun eventsRefresh() {
        viewModelScope.launch {
            _dataState.value = FeedModelState(refreshing = true)
            try {
                eventRepository.getAllEvents()
                _dataState.value = FeedModelState()
            } catch (_: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }


    fun loadPosts() {
        viewModelScope.launch {
            _dataState.value = FeedModelState(refreshing = true)
            try {
                postRepository.getAllPosts()
                _dataState.value = FeedModelState()
            } catch (_: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun loadEvents(){
        viewModelScope.launch {
            _dataState.value = FeedModelState(refreshing = true)
            try {
                eventRepository.getAllEvents()
                _dataState.value = FeedModelState()
            }catch (_ : RuntimeException){
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            try {
                val users = postRepository.getAllUsers()
                val mention = users.map {
                    it.toMentionUser()
                }
                _mentionUsers.value = mention
            } catch (_: RuntimeException) {

            }
        }
    }

    fun savePost() {
        val newPost = edited.value ?: return
        _postCreated.value = Unit
        viewModelScope.launch {
            _dataState.value = FeedModelState(refreshing = true)
            try {
                if (newPost.id != 0L) {
                    postRepository.editPost(newPost)
                } else {
                    postRepository.savePost(newPost, _photo.value?.file)
                }
                _dataState.value = FeedModelState(errorReport = null)
                clearEditingState()
            } catch (_: RuntimeException) {
                _dataState.value = FeedModelState(
                    errorReport = ErrorReport(
                        0,
                        FeedErrorMassage.SAVE_ERROR
                    )
                )
            }
        }
    }

    fun likePostById(post: Post) {
        val isLiked = post.likedByMe
        viewModelScope.launch {
            try {
                postRepository.likePostById(post.id)
                _dataState.value = FeedModelState(errorReport = null)
            } catch (_: RuntimeException) {
                if (!isLiked) {
                    _dataState.value = FeedModelState(
                        errorReport = ErrorReport(
                            post.id,
                            FeedErrorMassage.LIKE_ERROR
                        )
                    )
                } else {
                    _dataState.value = FeedModelState(
                        errorReport = ErrorReport(
                            post.id,
                            FeedErrorMassage.DISLIKE_ERROR
                        )
                    )
                }
            }
        }
    }

    fun likeEventById(event: Event) {
        val isLiked = event.likedByMe
        viewModelScope.launch {
            try {
                eventRepository.likeEventById(event.id)
                _dataState.value = FeedModelState(errorReport = null)
            } catch (_: RuntimeException) {
                if (!isLiked) {
                    _dataState.value = FeedModelState(
                        errorReport = ErrorReport(
                            event.id,
                            FeedErrorMassage.LIKE_ERROR
                        )
                    )
                } else {
                    _dataState.value = FeedModelState(
                        errorReport = ErrorReport(
                            event.id,
                            FeedErrorMassage.DISLIKE_ERROR
                        )
                    )
                }
            }
        }
    }

    fun removePostById(id: Long) {
        viewModelScope.launch {
            try {
                postRepository.removePostsBiId(id)
                _dataState.value = FeedModelState(errorReport = null)
            } catch (_: RuntimeException) {
                _dataState.value = FeedModelState(
                    errorReport = ErrorReport(
                        id,
                        FeedErrorMassage.REMOVE_ERROR
                    )
                )
            }
        }
    }

    fun removeEventById(id: Long) {
        viewModelScope.launch {
            try {
                eventRepository.removeEventBiId(id)
                _dataState.value = FeedModelState(errorReport = null)
            } catch (_: RuntimeException) {
                _dataState.value = FeedModelState(
                    errorReport = ErrorReport(
                        id,
                        FeedErrorMassage.REMOVE_ERROR
                    )
                )
            }
        }
    }

    fun updatePhoto(uri: Uri, file: File) {
        _photo.value = PhotoModel(uri, file)
    }

    fun removePhoto() {
        _photo.value = null
    }

    fun setEditPost(post: Post) {
        _edited.value = post
        mentionUsersIsTransferred = false
    }

    fun setContentAndLink(content: String, link: String) {
        val linkText = if (link.isBlank()) {
            null
        } else {
            link.trim()
        }
        if (edited.value?.content == content.trim()) {
            _edited.value = edited.value?.copy(link = linkText)
        } else {
            _edited.value = edited.value?.copy(content = content.trim(), link = linkText)
        }
    }


    fun toggleMentionSelection(userId: Long) {
        val currentList = _mentionUsers.value
        val updatedList = currentList.map { user ->
            if (user.id == userId) user.copy(isSelected = !user.isSelected) else user
        }
        _mentionUsers.value = updatedList
    }

    // флаг для загрузки списка упомянутых пользователей
    var mentionUsersIsTransferred: Boolean = false

    fun setMentionUsers(users: List<Long>) {
        val currentList = _mentionUsers.value
        val updatedList = currentList.map { user ->
            if (users.find { it == user.id } != null) user.copy(isSelected = true) else user
        }
        _mentionUsers.value = updatedList
        mentionUsersIsTransferred = true
    }

    fun setSelectedMentionIds() {
        val mentionsIdList = _mentionUsers.value.filter { it.isSelected }.map { it.id }
        _edited.value = edited.value?.copy(mentionIds = mentionsIdList)
    }

    fun setLocation(lat: Double, long: Double) {
        _edited.value = edited.value?.copy(coords = Coordinates(lat, long))
    }

    fun removeLocation() {
        _edited.value = edited.value?.copy(coords = null)
    }

    fun clearMentionsList() {
        _mentionUsers.value = _mentionUsers.value.map { it.copy(isSelected = false) }
    }

    fun clearEditingState() {
        _edited.value = creatingPost
        _photo.value = null
        _mentionUsers.value = _mentionUsers.value.map { it.copy(isSelected = false) }
        mentionUsersIsTransferred = false
    }


}