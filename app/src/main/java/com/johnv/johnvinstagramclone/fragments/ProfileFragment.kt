package com.johnv.johnvinstagramclone.fragments

import android.util.Log
import com.johnv.johnvinstagramclone.MainActivity
import com.johnv.johnvinstagramclone.Post
import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseQuery
import com.parse.ParseUser

class ProfileFragment : FeedFragment() {
     override fun queryPosts() {
         // Specify which class to query
         val query: ParseQuery<Post> = ParseQuery.getQuery(Post::class.java)
         // Find all Post Objects
         query.include(Post.KEY_USER)
         // Only return posts from currently signed in user
         query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser())
         query.addDescendingOrder("createdAt")
         query.findInBackground(object : FindCallback<Post> {
             override fun done(posts: MutableList<Post>?, e: ParseException?) {
                 if (e != null) {
                     //something went wrong
                     Log.e(MainActivity.TAG,"Error fetching posts")
                 } else {
                     if(posts != null){
                         for (post in posts){
                             Log.i(MainActivity.TAG, "Post: " + post.getDescription() + ", username: " + post.getUser()?.username)
                         }
                         adapter.clear()
                         allPosts.addAll(posts)
                         adapter.notifyDataSetChanged()
                         swipeContainer.setRefreshing(false)
                     }
                 }
             }

         })
     }
}