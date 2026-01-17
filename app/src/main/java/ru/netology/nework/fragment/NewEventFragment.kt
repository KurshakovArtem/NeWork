package ru.netology.nework.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.supportingFunctions.AndroidUtils
import ru.netology.nework.supportingFunctions.dpToPx
import ru.netology.nework.supportingFunctions.loadAvatar
import ru.netology.nework.viewmodel.PostViewModel
import kotlin.getValue

@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentNewEventBinding.inflate(inflater, container, false)

        val photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(requireContext(), R.string.image_error, Toast.LENGTH_SHORT)
                        .show()
                    return@registerForActivityResult
                }
                val uri = result.data?.data ?: return@registerForActivityResult
                viewModel.updateEventPhoto(uri, uri.toFile())
            }

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.menu_top_app_bar, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.save -> {
                            if (binding.edit.text.isNotBlank()) {
                                viewModel.setEventContentAndLink(
                                    binding.edit.text.toString(),
                                    binding.link.text.toString()
                                )
                                viewModel.setSelectedSpeakerIds()
                                AndroidUtils.hideKeyboard(requireView())
                                viewModel.saveEvent()
                                true
                            } else false
                        }

                        R.id.clear -> {
                            viewModel.clearEventEditingState()
                            true
                        }

                        else -> false
                    }
            }, viewLifecycleOwner
        )

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding.buttonNewEventPanel.setPadding(0, 0, 0, viewModel.padding)
        val fabParams =  binding.fabButton.layoutParams as ConstraintLayout.LayoutParams
        fabParams.setMargins(0, 0, 0, viewModel.padding)
        binding.fabButton.layoutParams = fabParams

        viewModel.eventPhoto.observe(viewLifecycleOwner) { photo ->
            if (photo == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }
            binding.photo.setImageURI(photo.uri)
            binding.photoContainer.visibility = View.VISIBLE
        }

        binding.removePhoto.setOnClickListener {
            viewModel.removeEventPhoto()
        }

        binding.addNewEventPhoto.setOnClickListener {
            viewModel.setEventContentAndLink(
                binding.edit.text.toString(),
                binding.link.text.toString()
            )
            val options = arrayOf(
                getString(R.string.take_photo), getString(R.string.pick_photo)
            )
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_photo_action))
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            ImagePicker.with(this)
                                .cameraOnly()
                                .crop()
                                .createIntent(photoLauncher::launch)
                        }

                        1 -> {
                            ImagePicker.with(this)
                                .galleryOnly()
                                .crop()
                                .createIntent(photoLauncher::launch)
                        }
                    }
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        binding.addNewEventUsers.setOnClickListener {
            viewModel.setEventContentAndLink(
                binding.edit.text.toString(),
                binding.link.text.toString()
            )
            findNavController().navigate(R.id.action_newEventFragment_to_mentionsFragment)
        }

        if (viewModel.eventEdited.value?.id != 0L && !viewModel.speakerUsersIsTransferred) {
            val speakersList = viewModel.eventEdited.value?.speakerIds ?: emptyList()
            viewModel.setSpeakerUsers(speakersList)
        }

        viewModel.eventEdited.observe(viewLifecycleOwner) { edited ->
            binding.edit.setText(edited.content)
            binding.link.setText(edited.link)
            if (edited.id != 0L) {
                binding.photoContainer.visibility = View.GONE
                binding.addNewEventPhoto.visibility = View.GONE
                binding.addNewEventAttachment.visibility = View.GONE
            }
        }

        val speakersContainer = binding.speakersPreviewContainer

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.speakerUsersFlow.collect { users ->
                    speakersContainer.removeAllViews()

                    val selectedUsers = users.filter { it.isSelected }
                    if (selectedUsers.isEmpty()) {
                        speakersContainer.visibility = View.GONE
                    } else {
                        speakersContainer.visibility = View.VISIBLE
                    }
                    val visibleUsers = selectedUsers.take(5)
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
                        speakersContainer.addView(avatarView)
                    }

                    if (selectedUsers.size > 5) {
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

                            setOnClickListener {
                                findNavController().navigate(R.id.action_newEventFragment_to_mentionsFragment)
                            }
                        }
                        speakersContainer.addView(plusView)
                    }

                }
            }
        }

        binding.addNewEventLocation.setOnClickListener {
            viewModel.setEventContentAndLink(
                binding.edit.text.toString(),
                binding.link.text.toString()
            )
            findNavController().navigate(R.id.action_newEventFragment_to_mapFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.setEventContentAndLink(
                        binding.edit.text.toString(),
                        binding.link.text.toString()
                    )
                    findNavController().popBackStack()
                }
            })

        viewModel.saveBeforeBack.observe(viewLifecycleOwner){
            viewModel.setEventContentAndLink(
                binding.edit.text.toString(),
                binding.link.text.toString()
            )
        }

        binding.fabButton.setOnClickListener {
            viewModel.setEventContentAndLink(
                binding.edit.text.toString(),
                binding.link.text.toString()
            )
            BottomDateAndType().show(parentFragmentManager, "date_type_bottom_sheet")
        }



        return binding.root
    }
}