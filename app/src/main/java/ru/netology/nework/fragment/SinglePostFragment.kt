package ru.netology.nework.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import androidx.appcompat.widget.PopupMenu
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.ui_view.ViewProvider
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentSinglePostBinding
import ru.netology.nework.databinding.LocationBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.model.FeedErrorMassage
import ru.netology.nework.supportingFunctions.convertResponseToCardPost
import ru.netology.nework.supportingFunctions.converterNumToString
import ru.netology.nework.supportingFunctions.dpToPx
import ru.netology.nework.supportingFunctions.loadAttachmentImage
import ru.netology.nework.supportingFunctions.loadAvatar
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SinglePostFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    private var mapView: MapView? = null

    private lateinit var userLocation: UserLocationLayer

    @Inject
    lateinit var appAuth: AppAuth
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentSinglePostBinding.inflate(
            inflater,
            container,
            false
        )

        binding.root.setPadding(0, 0, 0, viewModel.padding)

        val singlePost: Post = viewModel.singlePostUse ?: {
            findNavController().navigateUp()
        } as Post

        viewModel.postData.observe(viewLifecycleOwner) { state ->
            val post = state.posts.find { it.id == singlePost.id } ?: singlePost

            with(binding) {
                cardAuthor.text = post.author
                cardPublished.text = convertResponseToCardPost(post.published)
                cardContent.text = post.content

                if (!post.link.isNullOrBlank()) {
                    cardLink.visibility = View.VISIBLE
                    cardLink.text = post.link
                } else {
                    cardLink.visibility = View.GONE
                }

                if (post.authorAvatar.isNullOrBlank()) {
                    cardAvatar.setImageResource(R.drawable.ic_empty_avatar_24)
                } else {
                    cardAvatar.loadAvatar(post.authorAvatar)
                }

                if (post.likedByMe) {
                    cardLikeButton.setIconResource(R.drawable.ic_liked_24)
                } else {
                    cardLikeButton.setIconResource(R.drawable.ic_like_24)
                }
                cardLikeButton.text = converterNumToString(post.likeOwnerIds?.size ?: 0)
                cardMentionsButton.text = converterNumToString(post.mentionIds?.size ?: 0)

                cardLikeButton.setOnClickListener {
                    if (appAuth.authStateFlow.value.id != 0L) {
                        viewModel.likePostById(post)
                    } else {
                        showLoginDialog()
                    }
                }

                cardMenu.isVisible = post.ownedByMe
                cardMenu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.option_post)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    viewModel.removePostById(post.id)
                                    findNavController().navigateUp()
                                    true
                                }

                                R.id.edit -> {
                                    viewModel.setEditPost(post)
                                    findNavController().navigate(R.id.action_singlePostFragment_to_newPostFragment)
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }

                val likedUsers = post.users?.filter { user ->
                    user.key == post.likeOwnerIds?.find { it == user.key }
                }?.map { it.value } ?: emptyList()

                likersPreviewContainer.removeAllViews()
                val visibleUsers = likedUsers.take(5)
                visibleUsers.forEachIndexed { index, user ->
                    val avatarView = ImageView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            requireContext().dpToPx(32),
                            requireContext().dpToPx(32),
                        ).apply {
                            if (index > 0) {
                                marginStart = requireContext().dpToPx(-12)
                            }
                        }

                        clipToOutline = true
                        outlineProvider = ViewOutlineProvider.BACKGROUND

                        if (user.avatar.isNullOrBlank()) {
                            setImageResource(R.drawable.ic_empty_avatar_24)
                            setBackgroundResource(R.drawable.background_shape)
                        } else {
                            loadAvatar(user.avatar)
                        }
                    }
                    likersPreviewContainer.addView(avatarView)
                }

                if (likedUsers.size > 5) {
                    val plusView = ImageView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            requireContext().dpToPx(32),
                            requireContext().dpToPx(32),
                        ).apply {
                            marginStart = requireContext().dpToPx(-12)
                        }
                        setImageResource(R.drawable.ic_add_24)
                        setBackgroundResource(R.drawable.background_shape)
                        clipToOutline = true
                        outlineProvider = ViewOutlineProvider.BACKGROUND


                    }
                    likersPreviewContainer.addView(plusView)
                }

                val mentionUsers = post.users?.filter { user ->
                    user.key == post.mentionIds?.find { it == user.key }
                }?.map { it.value } ?: emptyList()

                if (mentionUsers.isEmpty()) {
                    mentionsPreview.visibility = View.GONE
                    cardMentioned.visibility = View.GONE
                } else {
                    mentionsPreview.visibility = View.VISIBLE
                    cardMentioned.visibility = View.VISIBLE
                }
                mentionsPreviewContainer.removeAllViews()
                val visibleMentions = mentionUsers.take(5)
                visibleMentions.forEachIndexed { index, user ->
                    val avatarView = ImageView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            requireContext().dpToPx(32),
                            requireContext().dpToPx(32),
                        ).apply {
                            if (index > 0) {
                                marginStart = requireContext().dpToPx(-12)
                            }
                        }

                        clipToOutline = true
                        outlineProvider = ViewOutlineProvider.BACKGROUND

                        if (user.avatar.isNullOrBlank()) {
                            setImageResource(R.drawable.ic_empty_avatar_24)
                            setBackgroundResource(R.drawable.background_shape)
                        } else {
                            loadAvatar(user.avatar)
                        }
                    }
                    mentionsPreviewContainer.addView(avatarView)
                }

                if (mentionUsers.size > 5) {
                    val plusView = ImageView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            requireContext().dpToPx(32),
                            requireContext().dpToPx(32),
                        ).apply {
                            marginStart = requireContext().dpToPx(-12)
                        }
                        setImageResource(R.drawable.ic_add_24)
                        setBackgroundResource(R.drawable.background_shape)
                        clipToOutline = true
                        outlineProvider = ViewOutlineProvider.BACKGROUND


                    }
                    mentionsPreviewContainer.addView(plusView)
                }

                when (post.attachment?.type) {

                    AttachmentType.EMPTY, null -> {
                        cardAttachment.visibility = View.GONE
                    }

                    AttachmentType.IMAGE -> {
                        cardAttachment.visibility = View.VISIBLE
                        cardAttachmentImage.visibility = View.VISIBLE
                        cardAttachmentVideo.visibility = View.GONE
                        cardAttachmentImage.loadAttachmentImage(post.attachment.url)
                    }

                    AttachmentType.VIDEO -> {
                        cardAttachment.visibility = View.VISIBLE
                        cardAttachmentImage.visibility = View.GONE
                        cardAttachmentVideo.apply {
                            visibility = View.VISIBLE
                            setMediaController(MediaController(binding.root.context))
                            setVideoURI(post.attachment.url.toUri())
                            setOnPreparedListener {
                                animate().alpha(1F)
                                seekTo(0)
                                setZOrderOnTop(false)
                            }
                            setOnCompletionListener {
                                stopPlayback()
                            }
                        }
                    }

                    AttachmentType.AUDIO -> {
                        cardAttachment.visibility = View.VISIBLE
                        cardAttachmentImage.visibility = View.GONE
                        cardAttachmentVideo.apply {
                            visibility = View.VISIBLE
                            setMediaController(MediaController(binding.root.context))
                            setVideoURI(post.attachment.url.toUri())
                            setBackgroundResource(R.drawable.audio)
                            setOnPreparedListener {
                                setZOrderOnTop(true)
                            }
                            setOnCompletionListener {
                                stopPlayback()
                            }
                        }
                    }

                }
            }
        }

        if (singlePost.coords == null) {
            binding.map.visibility = View.GONE
        } else {
            binding.map.visibility = View.VISIBLE
            mapView = binding.map.apply {
                userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
                userLocation.isVisible = true
                userLocation.isHeadingModeActive = false

                val collection = mapWindow.map.mapObjects.addCollection()
                collection.clear()
                val placeBinding = LocationBinding.inflate(layoutInflater)
                val point = Point(singlePost.coords.lat, singlePost.coords.long)
                @Suppress("DEPRECATION")
                collection.addPlacemark(
                    point,
                    ViewProvider(placeBinding.root)
                )
                val cameraPosition = mapWindow.map.cameraPosition
                mapWindow.map.move(
                    CameraPosition(
                        Point(singlePost.coords.lat, singlePost.coords.long),
                        10F,
                        cameraPosition.azimuth,
                        cameraPosition.tilt,
                    )
                )
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.loadPosts()
                    }
                    .show()
                viewModel.clearDataState()
            }

            when (state.errorReport?.feedErrorMassage) {
                FeedErrorMassage.LIKE_ERROR -> {
                    Snackbar.make(binding.root, R.string.like_error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) {
                            val postId = state.errorReport.postIdError
                            val post = viewModel.postData.value?.posts?.find { it.id == postId }
                                ?: return@setAction
                            viewModel.likePostById(post)
                        }
                        .show()
                    viewModel.clearDataState()
                }

                FeedErrorMassage.DISLIKE_ERROR -> {
                    Snackbar.make(binding.root, R.string.dislike_error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) {
                            val postId = state.errorReport.postIdError
                            val post = viewModel.postData.value?.posts?.find { it.id == postId }
                                ?: return@setAction
                            viewModel.likePostById(post)
                        }
                        .show()
                    viewModel.clearDataState()
                }

                FeedErrorMassage.REMOVE_ERROR -> {
                    Snackbar.make(binding.root, R.string.remove_error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) {
                            val postId = state.errorReport.postIdError
                            viewModel.removePostById(postId)
                        }
                        .show()
                }

                FeedErrorMassage.SAVE_ERROR -> {
                    Snackbar.make(binding.root, R.string.save_error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) {
                            viewModel.savePost()
                        }
                        .show()
                    viewModel.clearDataState()
                }
                null -> {}
                else -> {
                    viewModel.clearDataState()
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSinglePostBinding.bind(view)

        val mapView = binding.map
        subscribeToLifecycle(mapView)
    }

    private fun showLoginDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.need_auth)
            .setMessage(R.string.must_login)
            .setPositiveButton(R.string.sign_in) { _, _ ->
                findNavController().navigate(R.id.action_singlePostFragment_to_signInFragment)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .setCancelable(true)
            .show()
    }

    private fun subscribeToLifecycle(mapView: MapView) {
        viewLifecycleOwner.lifecycle.addObserver(
            object : LifecycleEventObserver {
                override fun onStateChanged(
                    source: LifecycleOwner,
                    event: Lifecycle.Event
                ) {
                    when (event) {
                        Lifecycle.Event.ON_START -> {
                            MapKitFactory.getInstance().onStart()
                            mapView.onStart()
                        }

                        Lifecycle.Event.ON_STOP -> {
                            mapView.onStop()
                            MapKitFactory.getInstance().onStop()
                        }

                        Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)

                        else -> Unit
                    }
                }
            }
        )
    }
}