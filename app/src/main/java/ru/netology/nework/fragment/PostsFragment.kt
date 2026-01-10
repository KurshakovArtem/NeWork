package ru.netology.nework.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.OnInteractionListener
import ru.netology.nework.adapter.PostAdapter
import ru.netology.nework.api.PostApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.model.FeedErrorMassage
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject


@AndroidEntryPoint
class PostsFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentPostsBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = PostAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                if (appAuth.authStateFlow.value.id != 0L) {
                    viewModel.likeById(post)
                } else {
                    showLoginDialog()
                }
            }

            override fun onEdit(post: Post) {
                super.onEdit(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removePostById(post.id)
            }

            override fun onShare(post: Post) {
                super.onShare(post)
            }

            override fun onVideo(post: Post) {
                super.onVideo(post)
            }

            override fun onMoveToSinglePost(post: Post) {
                super.onMoveToSinglePost(post)
            }

            override fun onSaveRefresh(post: Post) {
                super.onSaveRefresh(post)
            }

            override fun onMoveToSinglePhoto(post: Post) {
                super.onMoveToSinglePhoto(post)
            }

        })

        binding.list.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
        }

        binding.fabButton.setOnClickListener {
            if (appAuth.authStateFlow.value.id != 0L) {
                findNavController().navigate(R.id.action_postsFragment_to_newPostFragment)
            } else {
                showLoginDialog()
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.refresh()
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
                        viewModel.loadPosts()
                    }
                    .show()
            }

            when (state.errorReport?.feedErrorMassage) {
                FeedErrorMassage.LIKE_ERROR -> {
                    Snackbar.make(binding.root, R.string.like_error, Snackbar.LENGTH_LONG)
                        .setAnchorView(bottomNav)
                        .setAction(R.string.retry_loading) {
                            val postId = state.errorReport.postIdError
                            val post = viewModel.data.value?.posts?.find { it.id == postId }
                                ?: return@setAction
                            viewModel.likeById(post)
                        }
                        .show()
                }

                FeedErrorMassage.DISLIKE_ERROR -> {
                    Snackbar.make(binding.root, R.string.dislike_error, Snackbar.LENGTH_LONG)
                        .setAnchorView(bottomNav)
                        .setAction(R.string.retry_loading) {
                            val postId = state.errorReport.postIdError
                            val post = viewModel.data.value?.posts?.find { it.id == postId }
                                ?: return@setAction
                            viewModel.likeById(post)
                        }
                        .show()
                }

                FeedErrorMassage.REMOVE_ERROR -> {
                    Snackbar.make(binding.root, R.string.remove_error, Snackbar.LENGTH_LONG)
                        .setAnchorView(bottomNav)
                        .setAction(R.string.retry_loading) {
                            val postId = state.errorReport.postIdError
                            viewModel.removePostById(postId)
                        }
                        .show()
                }

                FeedErrorMassage.SAVE_ERROR -> {
                    Snackbar.make(binding.root, R.string.save_error, Snackbar.LENGTH_LONG)
                        .setAnchorView(bottomNav)
                        .setAction(R.string.retry_loading) {
//                            val post =
//                                viewModel.data.value?.posts?.find {
//                                    it.id == state.errorReport.postIdError
//                                }
//                                    ?: throw RuntimeException("Post error")
                            viewModel.savePost()
                        }
                        .show()
                }

                FeedErrorMassage.SAVE_REFRESH_ERROR -> {
                    Snackbar.make(binding.root, R.string.save_error, Snackbar.LENGTH_LONG)
                        .setAnchorView(bottomNav)
                        .setAction(R.string.retry_loading) {
                            val post =
                                viewModel.data.value?.posts?.find {
                                    it.id == state.errorReport.postIdError
                                }
                                    ?: throw RuntimeException("Post error")
                            // viewModel.saveRefresh(post)
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
                findNavController().navigate(R.id.action_postsFragment_to_signInFragment)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .setCancelable(true)
            .show()
    }

}