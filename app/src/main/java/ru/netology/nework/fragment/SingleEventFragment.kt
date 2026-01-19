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
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.ui_view.ViewProvider
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentSingleEventBinding
import ru.netology.nework.databinding.LocationBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.supportingFunctions.convertResponseToCardPost
import ru.netology.nework.supportingFunctions.converterNumToString
import ru.netology.nework.supportingFunctions.dpToPx
import ru.netology.nework.supportingFunctions.loadAttachmentImage
import ru.netology.nework.supportingFunctions.loadAvatar
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class SingleEventFragment : Fragment() {

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

        val binding = FragmentSingleEventBinding.inflate(
            inflater,
            container,
            false
        )

        binding.root.setPadding(0, 0, 0, viewModel.padding)

        val singleEvent: Event = viewModel.singleEventUse ?: {
            findNavController().navigateUp()
        } as Event

        viewModel.eventData.observe(viewLifecycleOwner) { state ->
            val event = state.events.find { it.id == singleEvent.id } ?: singleEvent

            with(binding) {
                cardAuthor.text = event.author
                cardPublished.text = convertResponseToCardPost(event.published)
                cardContent.text = event.content

                if (!event.link.isNullOrBlank()) {
                    cardLink.visibility = View.VISIBLE
                    cardLink.text = event.link
                } else {
                    cardLink.visibility = View.GONE
                }

                if (event.authorAvatar.isNullOrBlank()) {
                    cardAvatar.setImageResource(R.drawable.ic_empty_avatar_24)
                } else {
                    cardAvatar.loadAvatar(event.authorAvatar)
                }

                if (event.likedByMe) {
                    cardLikeButton.setIconResource(R.drawable.ic_liked_24)
                } else {
                    cardLikeButton.setIconResource(R.drawable.ic_like_24)
                }

                if (event.participatedByMe) {
                    cardParticipantsButton.setIconResource(R.drawable.ic_people_24)
                } else {
                    cardParticipantsButton.setIconResource(R.drawable.ic_people_outline_24)
                }

                cardLikeButton.text = converterNumToString(event.likeOwnerIds?.size ?: 0)

                cardTypeEvent.text = event.type.toString()

                cardDateEvent.text = convertResponseToCardPost(event.published)

                if (!event.participantsIds.isNullOrEmpty()) {
                    cardParticipantsButton.text = event.participantsIds.size.toString()
                } else {
                    cardParticipantsButton.text = "0"
                }

                cardLikeButton.setOnClickListener {
                    if (appAuth.authStateFlow.value.id != 0L) {
                        viewModel.likeEventById(event)
                    } else {
                        showLoginDialog()
                    }
                }

                cardParticipantsButton.setOnClickListener {
                    if (appAuth.authStateFlow.value.id != 0L) {
                        viewModel.addParticipantsById(event)
                    } else {
                        showLoginDialog()
                    }
                }

                cardMenu.isVisible = event.ownedByMe
                cardMenu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.option_post)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    viewModel.removeEventById(event.id)
                                    findNavController().navigateUp()
                                    true
                                }

                                R.id.edit -> {
                                    viewModel.setEditEvent(event)
                                    findNavController().navigate(R.id.action_singleEventFragment_to_newEventFragment)
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }

                val likedUsers = event.users?.filter { user ->
                    user.key == event.likeOwnerIds?.find { it == user.key }
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

                val participantsUsers = event.users?.filter { user ->
                    user.key == event.participantsIds?.find { it == user.key }
                }?.map { it.value } ?: emptyList()

                participantsPreviewContainer.removeAllViews()
                val visibleParticipants = participantsUsers.take(5)
                visibleParticipants.forEachIndexed { index, user ->
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
                    participantsPreviewContainer.addView(avatarView)
                }

                if (participantsUsers.size > 5) {
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
                    participantsPreviewContainer.addView(plusView)
                }

                val speakersUsers = event.users?.filter { user ->
                    user.key == event.speakerIds?.find { it == user.key }
                }?.map { it.value } ?: emptyList()

                if (speakersUsers.isEmpty()) {
                    speakersPreviewContainer.visibility = View.GONE
                    cardSpeakers.visibility = View.GONE
                } else {
                    speakersPreviewContainer.visibility = View.VISIBLE
                    cardSpeakers.visibility = View.VISIBLE
                }
                speakersPreviewContainer.removeAllViews()
                val visibleSpeakers = speakersUsers.take(5)
                visibleSpeakers.forEachIndexed { index, user ->
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
                    speakersPreviewContainer.addView(avatarView)
                }

                if (speakersUsers.size > 5) {
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
                    speakersPreviewContainer.addView(plusView)
                }



                when (event.attachment?.type) {

                    AttachmentType.EMPTY, null -> {
                        cardAttachment.visibility = View.GONE
                    }

                    AttachmentType.IMAGE -> {
                        cardAttachment.visibility = View.VISIBLE
                        cardAttachmentImage.visibility = View.VISIBLE
                        cardAttachmentVideo.visibility = View.GONE
                        cardAttachmentImage.loadAttachmentImage(event.attachment.url)
                    }

                    AttachmentType.VIDEO -> {
                        cardAttachment.visibility = View.VISIBLE
                        cardAttachmentImage.visibility = View.GONE
                        cardAttachmentVideo.apply {
                            visibility = View.VISIBLE
                            setMediaController(MediaController(binding.root.context))
                            setVideoURI(event.attachment.url.toUri())
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
                            setVideoURI(event.attachment.url.toUri())
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

        if (singleEvent.coords == null) {
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
                val point = Point(singleEvent.coords.lat, singleEvent.coords.long)
                @Suppress("DEPRECATION")
                collection.addPlacemark(
                    point,
                    ViewProvider(placeBinding.root)
                )
                val cameraPosition = mapWindow.map.cameraPosition
                mapWindow.map.move(
                    CameraPosition(
                        Point(singleEvent.coords.lat, singleEvent.coords.long),
                        10F,
                        cameraPosition.azimuth,
                        cameraPosition.tilt,
                    )
                )
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSingleEventBinding.bind(view)
        val mapView = binding.map
        subscribeToLifecycle(mapView)
    }

    private fun showLoginDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.need_auth)
            .setMessage(R.string.must_login)
            .setPositiveButton(R.string.sign_in) { _, _ ->
                findNavController().navigate(R.id.action_singleEventFragment_to_signInFragment)
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