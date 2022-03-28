package com.johnv.johnvinstagramclone.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.johnv.johnvinstagramclone.MainActivity
import com.johnv.johnvinstagramclone.Post
import com.johnv.johnvinstagramclone.PostAdapter
import com.johnv.johnvinstagramclone.R
import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseQuery

open class FeedFragment : Fragment() {

    lateinit var rvPosts: RecyclerView

    lateinit var adapter: PostAdapter

    var allPosts: ArrayList<Post> = ArrayList()

    lateinit var swipeContainer: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // This is where we set up our views and click listeners

        swipeContainer = view.findViewById(R.id.swipeContainer)

        swipeContainer.setOnRefreshListener {
            Log.i(TAG, "Refreshing Posts")
            queryPosts()
        }

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);

        rvPosts = view.findViewById(R.id.rvPosts)

        adapter = PostAdapter(requireContext(), allPosts)
        rvPosts.adapter = adapter

        rvPosts.layoutManager = LinearLayoutManager(requireContext())

        queryPosts()
    }

    open fun queryPosts(){
        val query: ParseQuery<Post> = ParseQuery.getQuery(Post::class.java)
        query.include(Post.KEY_USER)
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
                        //clear current posts
                        adapter.clear()
                        allPosts.addAll(posts)
                        adapter.notifyDataSetChanged()
                        swipeContainer.setRefreshing(false)

                    }
                }
            }

        })
    }

    companion object {
        const val TAG = "FeedFragment"
    }

}