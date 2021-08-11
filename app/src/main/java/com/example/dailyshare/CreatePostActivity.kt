package com.example.dailyshare

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.dailyshare.models.Post
import com.example.dailyshare.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

private const val TAG = "CreatePostActivity"
private const val PICK_PHOTO_CODE = 1
private const val TAKE_PHOTO_CODE = 2
private const val PHOTO_FILE_NAME = "photo.jpg"

class CreatePostActivity : AppCompatActivity() {
    private var takenPhoto: Bitmap ?= null
    private var photoUri: Uri ?= null
    private var submitTime : Int = 0
    private var signInUser : User? = null
    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var photoFile : File

    private lateinit var buttSelectImage : Button
    private lateinit var ivImage : ImageView
    private lateinit var buttSubmit : Button
    private lateinit var etDescription : EditText

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
    Onclick method for buttSubmit
    Alarm the user if he/she didn't enter description or image
    Save the data to
     */
    fun submit( view: View){
        //check if user select a photo
        if (photoUri == null && takenPhoto == null){
            if (submitTime > -1) {
                Toast.makeText(this, "No photo selected!", Toast.LENGTH_SHORT).show()
                submitTime++
                return
            }
        }

        //check if user enter descriptions
        if (etDescription.text.isBlank()){
            if (submitTime < 1){
                Toast.makeText(this,"No description entered! Click again to continue", Toast.LENGTH_SHORT).show()
                submitTime++
                return
            }
        }

        //check if we have a sign in user
        if (signInUser == null){
            Toast.makeText(this, "No sign in user", Toast.LENGTH_SHORT).show()
            return
        }
        buttSubmit.isEnabled = false
        val uploadUri = photoUri as Uri

        //upload the photo to firebase storage
        val photoRef = storageReference.child("image/${System.currentTimeMillis()}-photo.jpg")
        photoRef.putFile(uploadUri)
            .continueWithTask { photoUploadTask ->
                Log.i(TAG, "upload bytes: $")
                //get image url for the image
                photoRef.downloadUrl
            }.continueWithTask{ downloadUrlTask ->
                //Create a post object with the image url and add it into the posts list
                val post = Post(
                    etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signInUser)
                firestoreDB.collection("posts").add(post)
            }.addOnCompleteListener { postCreationTask ->
                buttSubmit.isEnabled = true
                if (!postCreationTask.isSuccessful){
                    Log.e(TAG, "Exception during syncing with Firestore")
                    Toast.makeText(this, "Fail to save post", Toast.LENGTH_SHORT).show()
                }else{
                    etDescription.text.clear()
                    ivImage.setImageResource(0)
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                    var postActivityIntent = Intent(this, PostActivity::class.java)
                    startActivity(postActivityIntent)
                    finish()
                }
            }
    }

    /*
    Onclick method for buttSelectImage
    Allow the user to select image from their device
    or Take an image from Camera
     */
    fun selectImage(view:View){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Image Method")
            .setMessage("Which method you want to use to select your image?")
            .setPositiveButton("Select From Device"){ _, _ ->
                //Let user pick from gallery
                Log.i(TAG,"Open up the picker on device")
                val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
                imagePickerIntent.type = "image/*"
                if (imagePickerIntent.resolveActivity(packageManager) != null){
                    startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
                }
            }
            .setNegativeButton("Take a Photo"){ _, _ ->
                //Let user take a photo
                Log.i(TAG,"Take a photo now")
                val imageTakerIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                photoFile = getPhotoFile(PHOTO_FILE_NAME)
                val fileProvider = FileProvider.getUriForFile(this, "com.example.fileprovider", photoFile)
                photoUri = fileProvider
                imageTakerIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileProvider)
                if (imageTakerIntent.resolveActivity(packageManager) != null){
                    startActivityForResult(imageTakerIntent, TAKE_PHOTO_CODE)
                }
            }
            .show()
    }

    private fun getPhotoFile(photoFileName: String): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(photoFileName,".jpg",storageDir)
    }

    /*
    Corresponding method for startActivityForResult
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Let the user select picture from gallery if the requestCode is PICK_PHOTO_CODE
        if (requestCode == PICK_PHOTO_CODE){
            if (resultCode == Activity.RESULT_OK){
                buttSelectImage.visibility = View.INVISIBLE
                ivImage.visibility = View.VISIBLE
                photoUri = data?.data
                ivImage.setImageURI(photoUri)
            }else{
                Toast.makeText(this, "The user has canceled the image selection", Toast.LENGTH_SHORT ).show()
            }
        }
        //Let the user take a picture
        if (requestCode == TAKE_PHOTO_CODE){
            if (resultCode == Activity.RESULT_OK){
                buttSelectImage.visibility = View.INVISIBLE
                ivImage.visibility = View.VISIBLE

                takenPhoto = BitmapFactory.decodeFile(photoFile.absolutePath)

                ivImage.setImageBitmap(takenPhoto)
            }else{
                Toast.makeText(this, "The user has canceled the photo taken", Toast.LENGTH_SHORT ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        buttSelectImage = findViewById(R.id.buttSelectImage)
        ivImage = findViewById(R.id.ivImage)
        buttSubmit = findViewById(R.id.buttSubmit)
        etDescription = findViewById(R.id.etDescription)
        firestoreDB =  Firebase.firestore
        storageReference = FirebaseStorage.getInstance().reference

        buttSelectImage.visibility = View.VISIBLE
        ivImage.visibility = View.INVISIBLE
        findCurrentUser()
    }
}