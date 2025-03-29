package com.example.moodish.adapter

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.moodish.data.model.Post
import com.example.moodish.databinding.ItemPostBinding
import com.squareup.picasso.Picasso
import com.example.moodish.utils.PostUtils
import com.example.moodish.data.AppDatabase
import com.example.moodish.MyPostsActivity
import com.example.moodish.R
import com.example.moodish.databinding.DialogEditPostBinding
import com.example.moodish.utils.ImageUtils

class PostAdapter(
    private val isMyPostsPage: Boolean = false,
    private val context: Context? = null,
    private val database: AppDatabase? = null
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    private var posts = listOf<Post>()
    private var currentImageDialogBinding: DialogEditPostBinding? = null
    private var currentDialog: AlertDialog? = null
    
    // Store the activity result launcher at adapter level
    private val imagePickerLauncher = (context as? AppCompatActivity)?.registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            currentImageDialogBinding?.ivEditImage?.setImageURI(it)
            currentImageDialogBinding?.let { binding ->
                binding.ivEditImage.tag = uri // Store URI for later use
            }
        }
    }

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
        val ctx = context ?: return
        val dialogBinding = DialogEditPostBinding.inflate(LayoutInflater.from(ctx))
        currentImageDialogBinding = dialogBinding
        
        // Set existing values
        dialogBinding.etPostText.setText(post.text)
        
        // Load existing image
        if (!post.imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(post.imageUrl)
                .fit()
                .centerCrop()
                .into(dialogBinding.ivEditImage)
        }

        // Set existing label
        when (post.label) {
            "Romantic" -> dialogBinding.rbRomantic.isChecked = true
            "Family" -> dialogBinding.rbFamily.isChecked = true
            "Solo" -> dialogBinding.rbSolo.isChecked = true
            "Happy" -> dialogBinding.rbHappy.isChecked = true
        }

        // Create dialog
        val dialog = AlertDialog.Builder(ctx)
            .setTitle("Edit Post")
            .setView(dialogBinding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { _, _ ->
                currentImageDialogBinding = null
                currentDialog = null
            }
            .create()

        currentDialog = dialog

        dialogBinding.btnChangeImage.setOnClickListener {
            imagePickerLauncher?.launch("image/*")
        }

        // Show dialog and set positive button listener
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val newText = dialogBinding.etPostText.text.toString()
            val newLabel = when (dialogBinding.rgLabels.checkedRadioButtonId) {
                R.id.rbRomantic -> "Romantic"
                R.id.rbFamily -> "Family"
                R.id.rbSolo -> "Solo"
                R.id.rbHappy -> "Happy"
                else -> post.label ?: ""
            }

            if (newText.isEmpty()) {
                Toast.makeText(ctx, "Post text cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (database != null) {
                val newImageUri = dialogBinding.ivEditImage.tag as? Uri
                
                if (newImageUri != null) {
                    try {
                        val bitmap = ImageUtils.uriToBitmap(ctx, newImageUri)
                        val imageName = "post_${System.currentTimeMillis()}"
                        
                        ImageUtils.uploadImageToStorage(bitmap, imageName) { imageUrl ->
                            if (imageUrl != null) {
                                PostUtils.editPost(
                                    post = post,
                                    newText = newText,
                                    newLabel = newLabel,
                                    newImageUrl = imageUrl,
                                    database = database,
                                    context = ctx
                                ) {
                                    if (ctx is MyPostsActivity) {
                                        ctx.fetchUserPosts()
                                    }
                                }
                                dialog.dismiss()
                                currentImageDialogBinding = null
                                currentDialog = null
                            } else {
                                Toast.makeText(ctx, "Failed to upload image", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Error processing image", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // No image change, just update text and label
                    PostUtils.editPost(
                        post = post,
                        newText = newText,
                        newLabel = newLabel,
                        newImageUrl = post.imageUrl,
                        database = database,
                        context = ctx
                    ) {
                        if (ctx is MyPostsActivity) {
                            ctx.fetchUserPosts()
                        }
                    }
                    dialog.dismiss()
                    currentImageDialogBinding = null
                    currentDialog = null
                }
            }
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