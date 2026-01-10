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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
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
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.supportingFunctions.AndroidUtils
import ru.netology.nework.supportingFunctions.dpToPx
import ru.netology.nework.supportingFunctions.loadAvatar
import ru.netology.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )


        val photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(requireContext(), R.string.image_error, Toast.LENGTH_SHORT)
                        .show()
                    return@registerForActivityResult
                }
                val uri = result.data?.data ?: return@registerForActivityResult
                viewModel.updatePhoto(uri, uri.toFile())
            }

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.menu_new_post, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.save -> {
                            if (binding.edit.text.isNotBlank()) {
                                val content = binding.edit.text.toString()
                                viewModel.setContent(content)
                                viewModel.setLink(binding.link.text.toString())
                                viewModel.setSelectedMentionIds()
                                AndroidUtils.hideKeyboard(requireView())
                                viewModel.savePost()
                                true
                            } else false


                            //viewModel.save(content)
                        }

                        else -> false
                    }
            }, viewLifecycleOwner
        )

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding.buttonNewPostPanel.setPadding(0, 0, 0, viewModel.padding)

        viewModel.photo.observe(viewLifecycleOwner) { photo ->
            if (photo == null) {
                binding.photoContainer.isGone = true
                return@observe
            }
            binding.photo.setImageURI(photo.uri)
            binding.photoContainer.isVisible = true
        }

        binding.removePhoto.setOnClickListener {
            viewModel.removePhoto()
        }

        binding.addNewPostPhoto.setOnClickListener {
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

        binding.addNewPostUsers.setOnClickListener {
            findNavController().navigate(R.id.action_newPostFragment_to_mentionsFragment)
        }

        val mentionsContainer = binding.mentionsPreviewContainer

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mentionUsersFlow.collect { users ->
                    mentionsContainer.removeAllViews()

                    val selectedUsers = users.filter { it.isSelected }
                    if (selectedUsers.isEmpty()) {
                        mentionsContainer.visibility = View.GONE
                    } else {
                        mentionsContainer.visibility = View.VISIBLE
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
                        mentionsContainer.addView(avatarView)
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
                                findNavController().navigate(R.id.action_newPostFragment_to_mentionsFragment)
                            }
                        }
                        mentionsContainer.addView(plusView)
                    }

                }
            }
        }

        binding.addNewPostLocation.setOnClickListener {
            findNavController().navigate(R.id.action_newPostFragment_to_mapFragment)
        }






        return binding.root
    }
}