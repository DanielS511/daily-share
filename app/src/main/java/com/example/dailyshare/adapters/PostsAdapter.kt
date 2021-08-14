package com.example.dailyshare.adapters
/*
This is the adapter to make posts appear on the recycleView
A post as a view is represent by post_item.xml
 */

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dailyshare.R
import com.example.dailyshare.models.Post

class PostsAdapter (val context: Context, val posts: List<Post>)
    : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        private val tvName : TextView = itemView.findViewById(R.id.tvName)
        private val tvDescription : TextView = itemView.findViewById(R.id.tvDescription)
        private val tvRelativeTime : TextView = itemView.findViewById(R.id.tvRelativeTime)
        private val ivPicture : ImageView = itemView.findViewById(R.id.ivPicture)
        fun bind(post: Post) {
            tvName.text = post.user?.username
            tvDescription.text = post.description
            Glide.with(context).load(post.imageUrl).into(ivPicture)
            tvRelativeTime.text = DateUtils.getRelativeTimeSpanString(post.creationTimeMs)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}