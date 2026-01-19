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
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.model.ErrorReport
import ru.netology.nework.model.EventFeedModel
import ru.netology.nework.model.FeedErrorMassage
import ru.netology.nework.model.PostFeedModel
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.UserFeedModel
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

private val creatingEvent = Event(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    published = "",
    datetime = "",
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
        .catch {
            _dataState.value = FeedModelState(error = true)
            it.printStackTrace()
        }
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
        .catch {
            _dataState.value = FeedModelState(error = true)
            it.printStackTrace()
        }
        .asLiveData(Dispatchers.Default)

    val userData: LiveData<UserFeedModel> = postRepository.usersData
        .map(::UserFeedModel)
        .catch {
            _dataState.value = FeedModelState(error = true)
            it.printStackTrace()
        }
        .asLiveData()


    /*
    Поскольку из за BottomNavigationView вся вёрстка снизу плывёт,
    будем выставлять паддинг снизу для каждого Barr или View вручную
    padding берём из MainActivity системных настроек
     */
    var padding: Int = 0

    var singlePostUse: Post? = null
    var singleEventUse: Event? = null
    private val _mentionUsers = MutableStateFlow<List<MentionUser>>(emptyList())
    val mentionUsersFlow: StateFlow<List<MentionUser>>
        get() = _mentionUsers.asStateFlow()

    private val _speakerUsers = MutableStateFlow<List<MentionUser>>(emptyList())
    val speakerUsersFlow: StateFlow<List<MentionUser>>
        get() = _speakerUsers.asStateFlow()
    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _postPhoto = MutableLiveData<PhotoModel?>()
    val postPhoto: LiveData<PhotoModel?>
        get() = _postPhoto

    private val _eventPhoto = MutableLiveData<PhotoModel?>()
    val eventPhoto: LiveData<PhotoModel?>
        get() = _eventPhoto

    private val _postEdited = MutableLiveData(creatingPost)
    val postEdited: LiveData<Post>
        get() = _postEdited

    private val _eventEdited = MutableLiveData(creatingEvent)
    val eventEdited: LiveData<Event>
        get() = _eventEdited

//    private val _locationState = MutableLiveData<Coordinates?>()
//    val locationState: LiveData<Coordinates?>
//        get() = _locationState

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    val saveBeforeBack = MutableLiveData(false)


    init {
        loadPosts()
        loadUsers()
        loadEvents()
    }

    fun clearDataState(){
        _dataState.value = FeedModelState()
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

    fun usersRefresh() {
        viewModelScope.launch {
            _dataState.value = FeedModelState(refreshing = true)
            try {
                postRepository.getAllUsers()
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

    fun loadEvents() {
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

    fun loadUsers() {
        viewModelScope.launch {
            try {
                val users = postRepository.getAllUsers()
                val mention = users.map {
                    it.toMentionUser()
                }
                _mentionUsers.value = mention
                _speakerUsers.value = mention
            } catch (_: RuntimeException) {

            }
        }
    }

    fun savePost() {
        val newPost = postEdited.value ?: return
        _postCreated.value = Unit
        viewModelScope.launch {
            _dataState.value = FeedModelState(refreshing = true)
            try {
                if (newPost.id != 0L) {
                    postRepository.editPost(newPost)
                } else {
                    postRepository.savePost(newPost, _postPhoto.value?.file)
                }
                _dataState.value = FeedModelState(errorReport = null)
                clearPostEditingState()
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

    fun saveEvent() {
        val newEvent = eventEdited.value ?: return
        _eventCreated.value = Unit
        viewModelScope.launch {
            _dataState.value = FeedModelState(refreshing = true)
            try {
                if (newEvent.id != 0L) {
                    eventRepository.editEvent(newEvent)
                } else {
                    eventRepository.saveEvent(newEvent, _eventPhoto.value?.file)
                }
                _dataState.value = FeedModelState(errorReport = null)
                clearEventEditingState()
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

    fun addParticipantsById(event: Event) {
        viewModelScope.launch {
            try {
                eventRepository.participantsById(event.id)
                _dataState.value = FeedModelState(errorReport = null)
            } catch (_: RuntimeException) {
                _dataState.value = FeedModelState(
                    errorReport = ErrorReport(
                        event.id,
                        FeedErrorMassage.PARTICIPANTS_ERROR
                    )
                )
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

    fun updatePostPhoto(uri: Uri, file: File) {
        _postPhoto.value = PhotoModel(uri, file)
    }

    fun removePostPhoto() {
        _postPhoto.value = null
    }

    fun updateEventPhoto(uri: Uri, file: File) {
        _eventPhoto.value = PhotoModel(uri, file)
    }

    fun removeEventPhoto() {
        _eventPhoto.value = null
    }

    fun setEditPost(post: Post) {
        _postEdited.value = post
        mentionUsersIsTransferred = false
    }

    fun setEditEvent(event: Event) {
        _eventEdited.value = event
        speakerUsersIsTransferred = false
    }

    fun setPostContentAndLink(content: String, link: String) {
        val linkText = if (link.isBlank()) {
            null
        } else {
            link.trim()
        }
        if (postEdited.value?.content == content.trim()) {
            _postEdited.value = postEdited.value?.copy(link = linkText)
        } else {
            _postEdited.value = postEdited.value?.copy(content = content.trim(), link = linkText)
        }
    }

    fun setEventContentAndLink(content: String, link: String) {
        val linkText = if (link.isBlank()) {
            null
        } else {
            link.trim()
        }
        if (eventEdited.value?.content == content.trim()) {
            _eventEdited.value = eventEdited.value?.copy(link = linkText)
        } else {
            _eventEdited.value = eventEdited.value?.copy(content = content.trim(), link = linkText)
        }
    }

    fun toggleMentionSelection(userId: Long) {
        val currentList = _mentionUsers.value
        val updatedList = currentList.map { user ->
            if (user.id == userId) user.copy(isSelected = !user.isSelected) else user
        }
        _mentionUsers.value = updatedList
    }

    fun toggleSpeakerSelection(userId: Long) {
        val currentList = _speakerUsers.value
        val updatedList = currentList.map { user ->
            if (user.id == userId) user.copy(isSelected = !user.isSelected) else user
        }
        _speakerUsers.value = updatedList
    }

    // флаг для загрузки списка упомянутых пользователей
    var mentionUsersIsTransferred: Boolean = false
    var speakerUsersIsTransferred: Boolean = false

    fun setMentionUsers(users: List<Long>) {
        val currentList = _mentionUsers.value
        val updatedList = currentList.map { user ->
            if (users.find { it == user.id } != null) user.copy(isSelected = true) else user
        }
        _mentionUsers.value = updatedList
        mentionUsersIsTransferred = true
    }

    fun setSpeakerUsers(users: List<Long>) {
        val currentList = _speakerUsers.value
        val updatedList = currentList.map { user ->
            if (users.find { it == user.id } != null) user.copy(isSelected = true) else user
        }
        _speakerUsers.value = updatedList
        speakerUsersIsTransferred = true
    }

    fun setSelectedMentionIds() {
        val mentionsIdList = _mentionUsers.value.filter { it.isSelected }.map { it.id }
        _postEdited.value = postEdited.value?.copy(mentionIds = mentionsIdList)
    }

    fun setSelectedSpeakerIds() {
        val speakerIdList = _speakerUsers.value.filter { it.isSelected }.map { it.id }
        _eventEdited.value = eventEdited.value?.copy(speakerIds = speakerIdList)
    }

    fun setPostLocation(lat: Double, long: Double) {
        _postEdited.value = postEdited.value?.copy(coords = Coordinates(lat, long))
    }

    fun removePostLocation() {
        _postEdited.value = postEdited.value?.copy(coords = null)
    }

    fun setEventLocation(lat: Double, long: Double) {
        _eventEdited.value = eventEdited.value?.copy(coords = Coordinates(lat, long))
    }

    fun removeEventLocation() {
        _eventEdited.value = eventEdited.value?.copy(coords = null)
    }

    fun clearMentionsList() {
        _mentionUsers.value = _mentionUsers.value.map { it.copy(isSelected = false) }
    }

    fun clearSpeakerList() {
        _speakerUsers.value = _speakerUsers.value.map { it.copy(isSelected = false) }
    }

    fun clearPostEditingState() {
        _postEdited.value = creatingPost
        _postPhoto.value = null
        _mentionUsers.value = _mentionUsers.value.map { it.copy(isSelected = false) }
        mentionUsersIsTransferred = false
    }

    fun clearEventEditingState() {
        _eventEdited.value = creatingEvent
        _eventPhoto.value = null
        _speakerUsers.value = _speakerUsers.value.map { it.copy(isSelected = false) }
        speakerUsersIsTransferred = false
    }

    fun setEventType(eventType: EventType) {
        _eventEdited.value = _eventEdited.value?.copy(type = eventType)
    }

    fun setEventDateTime(date: String) {
        _eventEdited.value = _eventEdited.value?.copy(datetime = date)
    }

}