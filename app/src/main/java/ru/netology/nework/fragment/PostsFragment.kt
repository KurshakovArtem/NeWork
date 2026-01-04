package ru.netology.nework.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.adapter.OnInteractionListener
import ru.netology.nework.adapter.PostAdapter
import ru.netology.nework.api.PostApiService
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject


@AndroidEntryPoint
class PostsFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()

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

        val adapter = PostAdapter(object : OnInteractionListener{
            override fun onLike(post: Post) {
                super.onLike(post)
            }

            override fun onEdit(post: Post) {
                super.onEdit(post)
            }

            override fun onRemove(post: Post) {
                super.onRemove(post)
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


        binding.testButton.setOnClickListener {
            viewModel.loadPosts()
        }

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
        }



        return binding.root
    }


}