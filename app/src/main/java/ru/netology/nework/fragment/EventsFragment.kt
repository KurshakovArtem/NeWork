package ru.netology.nework.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.EventAdapter
import ru.netology.nework.adapter.OnEventListener
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentEventsBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class EventsFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentEventsBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = EventAdapter(object : OnEventListener {
            override fun onLike(event: Event) {
                if (appAuth.authStateFlow.value.id != 0L) {
                    viewModel.likeEventById(event)
                } else {
                    showLoginDialog()
                }
            }

            override fun onEdit(event: Event) {
                super.onEdit(event)
            }

            override fun onRemove(event: Event) {
                viewModel.removeEventById(event.id)
            }

            override fun onShare(event: Event) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, event.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onMoveToSinglePost(event: Event) {
                super.onMoveToSinglePost(event)
            }
        })

        binding.list.adapter = adapter

        viewModel.eventData.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.events)
            binding.emptyText.isVisible = state.empty
        }

//        binding.fabButton.setOnClickListener {
//            if (appAuth.authStateFlow.value.id != 0L) {
//                findNavController().navigate(R.id.action_postsFragment_to_newPostFragment)
//            } else {
//                showLoginDialog()
//            }
//        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.eventsRefresh()
        }

        val bottomNav =
            (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))


        return binding.root
    }


    private fun showLoginDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.need_auth)
            .setMessage(R.string.must_login)
            .setPositiveButton(R.string.sign_in) { _, _ ->
                findNavController().navigate(R.id.action_eventsFragment_to_signInFragment)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .setCancelable(true)
            .show()
    }

}