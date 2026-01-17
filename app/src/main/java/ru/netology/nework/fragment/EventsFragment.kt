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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.EventAdapter
import ru.netology.nework.adapter.OnEventListener
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentEventsBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.model.FeedErrorMassage
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

            override fun onParticipants(event: Event) {
                if (appAuth.authStateFlow.value.id != 0L) {
                    viewModel.addParticipantsById(event)
                } else {
                    showLoginDialog()
                }
            }

            override fun onEdit(event: Event) {
                viewModel.setEditEvent(event)
                findNavController().navigate(R.id.action_eventsFragment_to_newEventFragment)
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

        binding.fabButton.setOnClickListener {
            if (appAuth.authStateFlow.value.id != 0L) {
                findNavController().navigate(R.id.action_eventsFragment_to_newEventFragment)
            } else {
                showLoginDialog()
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.eventsRefresh()
        }

        val bottomNav =
            (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAnchorView(bottomNav)
                    .setAction(R.string.retry_loading) {
                        viewModel.loadEvents()
                    }
                    .show()
            }

            when (state.errorReport?.feedErrorMassage) {
                FeedErrorMassage.LIKE_ERROR -> {
                    Snackbar.make(binding.root, R.string.like_error, Snackbar.LENGTH_LONG)
                        .setAnchorView(bottomNav)
                        .setAction(R.string.retry_loading) {
                            val eventId = state.errorReport.postIdError
                            val event = viewModel.eventData.value?.events?.find { it.id == eventId }
                                ?: return@setAction
                            viewModel.likeEventById(event)
                        }
                        .show()
                }

                FeedErrorMassage.DISLIKE_ERROR -> {
                    Snackbar.make(binding.root, R.string.dislike_error, Snackbar.LENGTH_LONG)
                        .setAnchorView(bottomNav)
                        .setAction(R.string.retry_loading) {
                            val eventId = state.errorReport.postIdError
                            val event = viewModel.eventData.value?.events?.find { it.id == eventId }
                                ?: return@setAction
                            viewModel.likeEventById(event)
                        }
                        .show()
                }

                FeedErrorMassage.REMOVE_ERROR -> {
                    Snackbar.make(binding.root, R.string.remove_error, Snackbar.LENGTH_LONG)
                        .setAnchorView(bottomNav)
                        .setAction(R.string.retry_loading) {
                            val eventId = state.errorReport.postIdError
                            viewModel.removeEventById(eventId)
                        }
                        .show()
                }

                FeedErrorMassage.SAVE_ERROR -> {
                    Snackbar.make(binding.root, R.string.save_error, Snackbar.LENGTH_LONG)
                        .setAnchorView(bottomNav)
                        .setAction(R.string.retry_loading) {
                            viewModel.saveEvent()
                        }
                        .show()
                }

                FeedErrorMassage.PARTICIPANTS_ERROR -> {
                    Snackbar.make(binding.root, R.string.participants_error, Snackbar.LENGTH_LONG)
                        .setAnchorView(bottomNav)
                        .setAction(R.string.retry_loading) {
                            val eventId = state.errorReport.postIdError
                            val event = viewModel.eventData.value?.events?.find { it.id == eventId }
                                ?: return@setAction
                            viewModel.addParticipantsById(event)
                        }
                        .show()
                }

                null -> {} //нет смысла уведомлять об успешной операции
            }
        }

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