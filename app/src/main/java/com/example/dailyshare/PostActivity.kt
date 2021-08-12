package com.example.dailyshare

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailyshare.models.Post
import com.example.dailyshare.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
private const val EXTRA_USERNAME = "EXTRA_USERNAME"
private const val TAG = "PostActivity"
open class PostActivity : AppCompatActivity() {

    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var posts : MutableList<Post>
    private lateinit var adapter: PostsAdapter
    private lateinit var rvPosts : RecyclerView

    private var signInUser : User? = null

    //find out who is the current user
    private fun findCurrentUser(){
        firestoreDB.collection("users")
            .document(Firebase.auth.currentUser?.uid as String)
            .get()
            .addOnSuccessListener {  DocumentSnapshot ->
                signInUser = DocumentSnapshot.toObject(User ::class.java)
                Log.i(TAG, "Current User is $signInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Unable to get current user", exception)
            }
    }

    /*
    Determine the posts is for post activity or profile activity
    Then set up the posts
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun setUpPosts(){
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
        var postsReference = firestoreDB
            .collection("posts")
            .limit(40)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

        //Check if we are on PostActivity or ProfileActivity
        //Profile Activity has a EXTRA_USERNAME
        val username = intent.getStringExtra(EXTRA_USERNAME)
        if (username != null){
            supportActionBar?.title = username
            postsReference = postsReference.whereEqualTo("user.user_name", username)
        }

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

    //Set up the floating action button to create post
    fun createPost(view: View) {
        val intent = Intent(this, CreatePostActivity::class.java)
        startActivity(intent)
    }

    //Set up the option menu for post activity
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //navigate the user to profile activity
        if (item.itemId == R.id.menuProfile){
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME, signInUser?.username)
            startActivity(intent)
        }

        //navigate the user to weather activity
        if (item.itemId == R.id.menuWeather){
            val intent = Intent(this, WeatherActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        firestoreDB =  Firebase.firestore
        findCurrentUser()
        setUpPosts()

    }
}