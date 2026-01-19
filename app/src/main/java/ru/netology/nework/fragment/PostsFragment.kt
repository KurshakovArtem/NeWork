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
import ru.netology.nework.adapter.OnInteractionListener
import ru.netology.nework.adapter.PostAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.model.FeedErrorMassage
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject


@AndroidEntryPoint
class PostsFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()

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
                    viewModel.likePostById(post)
                } else {
                    showLoginDialog()
                }
            }

            override fun onEdit(post: Post) {
                viewModel.setEditPost(post)
                findNavController().navigate(R.id.action_postsFragment_to_newPostFragment)
            }

            override fun onRemove(post: Post) {
                viewModel.removePostById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onMoveToSinglePost(post: Post) {
                viewModel.singlePostUse = post
                findNavController().navigate(R.id.action_postsFragment_to_singlePostFragment)
            }

        })

        binding.list.adapter = adapter

        viewModel.postData.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.emptyText.isVisible = state.empty
        }

        binding.fabButton.setOnClickListener {
            if (appAuth.authStateFlow.value.id != 0L) {
                findNavController().navigate(R.id.action_postsFragment_to_newPostFragment)
            } else {
                showLoginDialog()
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.postRefresh()
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
                viewModel.clearDataState()
            }

            when (state.errorReport?.feedErrorMassage) {
                FeedErrorMassage.LIKE_ERROR -> {
                    Snackbar.make(binding.root, R.string.like_error, Snackbar.LENGTH_LONG)
                        .setAnchorView(bottomNav)
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
                        .setAnchorView(bottomNav)
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
                            viewModel.savePost()
                        }
                        .show()
                    viewModel.clearDataState()
                }
                null -> {}  //нет смысла уведомлять об успешной операции
                else -> {
                    viewModel.clearDataState()
                }
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