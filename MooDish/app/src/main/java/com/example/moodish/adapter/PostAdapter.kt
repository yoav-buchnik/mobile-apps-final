package com.example.moodish.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.moodish.data.model.Post
import com.example.moodish.databinding.ItemPostBinding
import com.squareup.picasso.Picasso
import com.example.moodish.utils.PostUtils
import com.example.moodish.data.AppDatabase
import com.example.moodish.MyPostsActivity
import com.example.moodish.R
import com.example.moodish.databinding.DialogEditPostBinding

class PostAdapter(
    private val isMyPostsPage: Boolean = false,
    private val context: Context? = null,
    private val database: AppDatabase? = null
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    private var posts = listOf<Post>()

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.tvPostText.text = post.text
            binding.tvLabel.text = post.label
            
            // Load image using Picasso
            if (!post.imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(post.imageUrl)
                    .fit()
                    .centerCrop()
                    .into(binding.ivPostImage)
            }

            // Show/hide edit and delete buttons based on isMyPostsPage
            binding.ibEditPost.visibility = if (isMyPostsPage) View.VISIBLE else View.GONE
            binding.ibDeletePost.visibility = if (isMyPostsPage) View.VISIBLE else View.GONE

            if (isMyPostsPage) {
                binding.ibEditPost.setOnClickListener {
                    showEditDialog(post)
                }

                binding.ibDeletePost.setOnClickListener {
                    // Show confirmation dialog
                    AlertDialog.Builder(itemView.context)
                        .setTitle("Delete Post")
                        .setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Delete") { _, _ ->
                            if (context != null && database != null) {
                                PostUtils.deletePost(
                                    post = post,
                                    database = database,
                                    context = context
                                ) {
                                    // Refresh the posts after successful deletion
                                    if (context is MyPostsActivity) {
                                        context.fetchUserPosts()
                                    }
                                }
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
    }

    private fun showEditDialog(post: Post) {
        context?.let { ctx ->
            // Create custom dialog layout
            val dialogBinding = DialogEditPostBinding.inflate(LayoutInflater.from(ctx))
            
            // Pre-fill existing values
            dialogBinding.etPostText.setText(post.text)
            
            // Setup label radio buttons
            val labelGroup = dialogBinding.rgLabels
            when (post.label) {
                "Romantic" -> labelGroup.check(R.id.rbRomantic)
                "Family" -> labelGroup.check(R.id.rbFamily)
                "Solo" -> labelGroup.check(R.id.rbSolo)
                "Happy" -> labelGroup.check(R.id.rbHappy)
            }

            AlertDialog.Builder(ctx)
                .setTitle("Edit Post")
                .setView(dialogBinding.root)
                .setPositiveButton("Save") { dialog, _ ->
                    val newText = dialogBinding.etPostText.text.toString()
                    val newLabel = when (dialogBinding.rgLabels.checkedRadioButtonId) {
                        R.id.rbRomantic -> "Romantic"
                        R.id.rbFamily -> "Family"
                        R.id.rbSolo -> "Solo"
                        R.id.rbHappy -> "Happy"
                        else -> post.label ?: ""
                    }

                    if (newText.isNotEmpty() && database != null) {
                        PostUtils.editPost(
                            post = post,
                            newText = newText,
                            newLabel = newLabel,
                            database = database,
                            context = ctx
                        ) {
                            // Refresh the posts after successful edit
                            if (ctx is MyPostsActivity) {
                                ctx.fetchUserPosts()
                            }
                        }
                    } else {
                        Toast.makeText(ctx, "Post text cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount() = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
} 