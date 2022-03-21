package com.johnv.johnvinstagramclone

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.parse.*
import java.io.File

class MainActivity : AppCompatActivity() {

    val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
    val photoFileName = "photo.jpg"
    var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.takePhotoBtn).setOnClickListener {
            // Launch camera to let user take picture
            onLaunchCamera()
        }

        findViewById<Button>(R.id.submitBtn).setOnClickListener {
            // allow user to submit post to our Parse server
            // get description from etDescription field to upload to Parse
            val description = findViewById<EditText>(R.id.etDescription).text.toString()
            val user = ParseUser.getCurrentUser()
            if(photoFile != null){
                submitPost(description, user, photoFile!!)
            } else {
                Log.e(TAG,"No File Selected")
                Toast.makeText(this,"No picture selected",Toast.LENGTH_SHORT).show()
            }

        }

        findViewById<Button>(R.id.logoutBtn).setOnClickListener {
            // allow user to log out
            ParseUser.logOut()
            val user = ParseUser.getCurrentUser() // this will now be null
            // once logged out, send user back to Login screen
            goToLoginActivity()
        }


        // queryPosts()
    }

    // submit post to our Parse server
    fun submitPost(description: String, user: ParseUser, file: File){
        // create the post object
        val post = Post()
        post.setDescription(description)
        post.setUser(user)
        post.setImage(ParseFile(file))
        post.saveInBackground { e ->
            if (e != null){
                // something has gone wrong
                Log.e(TAG, "Error while saving post")
                e.printStackTrace()
                // TODO: Show a toast to tell user something went wrong with saving the post
            } else {
                Log.i(TAG, "Successfully saved post")
                // TODO: Resetting the EditText field to be empty
                val etDescription : EditText = findViewById<EditText>(R.id.etDescription)
                etDescription.getText().clear()
                // TODO: Reset the ImageView to be empty
                val ivPhoto: ImageView = findViewById<ImageView>(R.id.ivPhoto)
                ivPhoto.setImageResource(android.R.color.transparent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                val takenImage = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                val ivPreview: ImageView = findViewById(R.id.ivPhoto)
                ivPreview.setImageBitmap(takenImage)
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onLaunchCamera() {
        // create Intent to take a picture and return control to the calling application
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName)

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        if (photoFile != null) {
            val fileProvider: Uri =
                FileProvider.getUriForFile(this, "com.johnv.fileprovider", photoFile!!)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(packageManager) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
            }
        }
    }

    fun getPhotoFileUri(fileName: String): File {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        val mediaStorageDir =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG)

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory")
        }

        // Return the file target for the photo based on filename
        return File(mediaStorageDir.path + File.separator + fileName)
    }

    fun queryPosts(){
        val query: ParseQuery<Post> = ParseQuery.getQuery(Post::class.java)
        query.include(Post.KEY_USER)
        query.findInBackground(object :FindCallback<Post>{
            override fun done(posts: MutableList<Post>?, e: ParseException?) {
                if (e != null) {
                    //something went wrong
                    Log.e(TAG,"Error fetching posts")
                } else {
                    if(posts != null){
                        for (post in posts){
                            Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser()?.username)
                        }
                    }
                }
            }

        })
    }

    private fun goToLoginActivity(){
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        const val TAG = "MainActivity"
    }
}