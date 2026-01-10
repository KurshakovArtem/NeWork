package ru.netology.nework.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.MentionAdapter
import ru.netology.nework.adapter.OnMentionListener
import ru.netology.nework.databinding.FragmentMentionBinding
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.dto.MentionUser
import ru.netology.nework.supportingFunctions.AndroidUtils
import ru.netology.nework.viewmodel.PostViewModel
import kotlin.getValue

@AndroidEntryPoint
class MentionsFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentMentionBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = MentionAdapter(object : OnMentionListener {
            override fun onUserSelected(
                user: MentionUser,
                isSelected: Boolean
            ) {
                viewModel.toggleMentionSelection(user.id)
            }
        })

        binding.list.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mentionUsersFlow.collect { list ->
                    adapter.submitList(list)
                }
            }
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
                            findNavController().navigateUp()
                            true
                        }

                        else -> false
                    }
            }, viewLifecycleOwner
        )

        return binding.root
    }
}