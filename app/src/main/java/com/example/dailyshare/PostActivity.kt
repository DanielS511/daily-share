package com.example.dailyshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailyshare.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PostActivity : AppCompatActivity() {
    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var posts : MutableList<Post>
    private lateinit var adapter: PostsAdapter
    private lateinit var rvPosts : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        firestoreDB =  Firebase.firestore

        //Set the list of posts
        posts = mutableListOf()
        //Set the adapter
        adapter = PostsAdapter(this, posts)

        //Bind the adapter and layout manager with the recycleView
        rvPosts = findViewById(R.id.rvPosts)
        rvPosts.adapter = adapter
        rvPosts.layoutManager = LinearLayoutManager(this)

        /*
        get posts from database
         */
        val postsReference = firestoreDB
            .collection("posts")
            .limit(40)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

        postsReference.addSnapshotListener { value, error ->
            if (error != null || value == null){
                Log.e("PostActivity", "Error when querying posts", error)
                return@addSnapshotListener
            }
            val postList = value.toObjects(Post::class.java)

            //clear exist items in posts and add data acquired to posts
            posts.clear()
            posts.addAll(postList)

            //update the recyclerView
            adapter.notifyDataSetChanged()

            for (post in postList){
                Log.i("PostActivity", "Post $post")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuProfile){
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}