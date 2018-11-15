package com.speakeasy.watsonbarassistant.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.*
import com.speakeasy.watsonbarassistant.extensions.*

class FriendAdapter(private val context: Context, private val users: MutableList<UserInfo>,
                    private val refresh: (() -> Unit)):
        RecyclerView.Adapter<FriendAdapter.ViewHolder>() {

    class ViewHolder(val layout: RelativeLayout) : RecyclerView.ViewHolder(layout)

    private val fireStore = FirebaseFirestore.getInstance()
    private val fireAuth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.friend_item_view,
                parent, false) as RelativeLayout
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val layout = holder.layout
        val user = users[position]

        setView(layout, user)
    }

    private fun setView(layout: RelativeLayout, user: UserInfo) {
        val friendId = user.userId
        val textView = layout.findViewById<TextView>(R.id.friendUsername)
        textView.text = user.username
        val requestButton = layout.findViewById<Button>(R.id.friendRequestButton)
        val acceptButton = layout.findViewById<Button>(R.id.acceptRequestButton)
        val denyButton = layout.findViewById<Button>(R.id.denyRequestButton)
        val incomingRequestBar = layout.findViewById<LinearLayout>(R.id.incomingRequestBar)

        when (user) {
            in BarAssistant.friends -> {
                requestButton.text = "Remove"
                requestButton.setOnClickListener { _ ->
                    fireAuth.currentUser?.uid?.let { uid ->
                        friendId?.let { fid ->
                            removeFriend(uid, fid, user, layout)
                        }
                    }
                }
            }
            in BarAssistant.requestsInProgress -> {
                requestButton.visibility = View.GONE
                incomingRequestBar.visibility = View.VISIBLE

                acceptButton.setOnClickListener { _ ->
                    val currentUid = fireAuth.currentUser?.uid
                    currentUid?.let { uid ->
                        friendId?.let { fid ->
                            acceptFriendRequest(uid, fid, user, layout)
                        }
                    }
                }

                denyButton.setOnClickListener {
                    fireAuth.currentUser?.uid?.let { uid ->
                        friendId?.let { fid ->
                            denyFriend(uid, fid, user, layout)
                        }
                    }
                }
            }
            in BarAssistant.pendingRequests -> {
                requestButton.text = "Cancel"
                requestButton.setOnClickListener {
                    fireAuth.currentUser?.uid?.let { uid ->
                        friendId?.let { fid ->
                            cancelRequest(uid, fid, user, layout)
                        }
                    }
                }
            }
            else -> {
                requestButton.setOnClickListener { _ ->
                    val currentUid = fireAuth.currentUser?.uid
                    currentUid?.let { uid ->
                        friendId?.let { fid ->
                            createFriendRequest(uid, fid, user, layout)
                        }
                    }
                }
            }
        }
    }

    private fun createFriendRequest(userId: String, friendId: String, user: UserInfo, layout: RelativeLayout) {
        fireStore.blockedDocument(friendId).get().addOnSuccessListener { blockedDocument ->
            val blockedList = getIdList(blockedDocument, BLOCKED_LIST)
            if(userId !in blockedList) {
                fireStore.requestDocument(friendId).get().addOnSuccessListener { document ->
                    val storedRequestIds = document.get(REQUEST_LIST) as? ArrayList<*>
                    val friendRequestIds = storedRequestIds?.toStringMutableList()
                    friendRequestIds?.add(userId)
                    val newFriendRequestIds = friendRequestIds ?: mutableListOf()
                    val friendRequestUpdate = mapOf(REQUEST_LIST to newFriendRequestIds)
                    fireStore.requestDocument(friendId).set(friendRequestUpdate)
                    fireStore.pendingDocument(userId).get().addOnSuccessListener {
                        val storedStringIds = addId(it, PENDING_LIST, friendId)
                        val newPending = mapOf(PENDING_LIST to storedStringIds)
                        fireStore.pendingDocument(userId).set(newPending)
                        BarAssistant.pendingRequests.add(user)
                        context.toast("Requesting ${user.username} as a friend.")
                        setView(layout, user)
                        refresh()
                        //notifyDataSetChanged()
                    }.addOnFailureListener {
                        context.toast("Failed to request ${user.username} as a friend.")
                    }
                    fireStore.pendingDocument(friendId).get().addOnSuccessListener {
                        val storedIds = getIdList(it, PENDING_LIST)
                        if(userId in storedIds) {
                            acceptFriendRequest(userId, friendId, user, layout)
                        }
                    }
                }.addOnFailureListener { _ ->
                    context.toast("Failed to request ${user.username} as a friend.")
                }
            }
        }
    }

    private fun acceptFriendRequest(userId: String, friendId: String, user: UserInfo, layout: RelativeLayout) {
        fireStore.pendingDocument(friendId).get().addOnSuccessListener {
            val storedIds = it.get(PENDING_LIST) as? ArrayList<*>
            val pendingIds = storedIds?.toStringMutableList()
            val newPendingIds = pendingIds ?: mutableListOf()
            newPendingIds.remove(userId)
            val newPending = mapOf(PENDING_LIST to newPendingIds)
            fireStore.pendingDocument(friendId).set(newPending)
            fireStore.friendDocument(friendId).get().addOnSuccessListener { friendDocument ->
                val newFriendIds = addId(friendDocument, FRIEND_LIST, userId)
                val newFriends = mapOf(FRIEND_LIST to newFriendIds)
                fireStore.friendDocument(friendId).set(newFriends).addOnSuccessListener { _ ->
                    removePendingRequest(user, userId, layout)
                }.addOnFailureListener { _ ->
                    context.toast("Failed to accept friend request.")
                }
            }.addOnFailureListener { _ ->
                context.toast("Failed to accept friend request.")
            }
        }.addOnFailureListener {
            context.toast("Failed to accept friend request.")
        }
    }

    private fun removePendingRequest(user: UserInfo, userId: String, layout: RelativeLayout) {
        fireStore.requestDocument(userId).get().addOnSuccessListener {
            user.userId?.let { uid ->
                val storedStringIds = removeId(it, REQUEST_LIST, uid)
                val newFriendRequests = mapOf(REQUEST_LIST to storedStringIds)
            fireStore.requestDocument(userId).set(newFriendRequests).addOnSuccessListener { _ ->
                    addFriend(user, userId, layout)
                }
            }
        }
        fireStore.pendingDocument(user.userId.toString()).get().addOnSuccessListener {
            val storedStringIds = removeId(it, PENDING_LIST, userId)
            val newFriendPending = mapOf(REQUEST_LIST to storedStringIds)
            fireStore.pendingDocument(user.userId.toString()).set(newFriendPending)
        }
    }

    private fun addFriend(user: UserInfo, userId: String, layout: RelativeLayout) {
        fireStore.friendDocument(userId).get().addOnSuccessListener {
            user.userId?.let { friendId ->
                val storedIds = addId(it, FRIEND_LIST, friendId)
                val newFriends = mapOf(FRIEND_LIST to storedIds)
                fireStore.friendDocument(userId).set(newFriends)
                BarAssistant.requestsInProgress.remove(user)
                BarAssistant.friends.add(user)
                // notifyDataSetChanged()
                setView(layout, user)
                refresh()
                context.toast("Added ${user.username} as a friend.")
            }
        }.addOnFailureListener {
            context.toast("Failed to add ${user.username} as a friend.")
        }
    }

    private fun removeFriend(userId: String, friendId: String, otherUser: UserInfo, layout: RelativeLayout) {
        fireStore.friendDocument(userId).get().addOnSuccessListener {
            val newFriendsCurrUser = removeId(it, FRIEND_LIST, friendId)
            val newFriendsCurrDocument = mapOf(FRIEND_LIST to newFriendsCurrUser)
            fireStore.friendDocument(userId).set(newFriendsCurrDocument)
            fireStore.friendDocument(friendId).get().addOnSuccessListener { document ->
                val otherFriends = removeId(document, FRIEND_LIST, userId)
                val otherDocument = mapOf(FRIEND_LIST to otherFriends)
                fireStore.friendDocument(friendId).set(otherDocument)
            }
            BarAssistant.friends.remove(otherUser)
            // notifyDataSetChanged()
            setView(layout, otherUser)
            refresh()
            context.toast("Removed ${otherUser.username} as a friend.")
        }.addOnFailureListener {
            context.toast("Failed to remove ${otherUser.username} as a friend.")
        }
    }

    private fun denyFriend(userId: String, friendId: String, otherUser: UserInfo, layout: RelativeLayout) {
        fireStore.requestDocument(userId).get().addOnSuccessListener {
            val newRequests = removeId(it, friendId, REQUEST_LIST)
            val newDocument = mapOf(REQUEST_LIST to newRequests)
            fireStore.requestDocument(userId).set(newDocument)
            fireStore.pendingDocument(friendId).get().addOnSuccessListener { document ->
                val newPending = removeId(document, userId, PENDING_LIST)
                val pendingDocument = mapOf(PENDING_LIST to newPending)
                fireStore.pendingDocument(friendId).set(pendingDocument)
            }
            BarAssistant.pendingRequests.remove(otherUser)
            // notifyDataSetChanged()
            setView(layout, otherUser)
            refresh()
        }
    }

    private fun cancelRequest(userId: String, friendId: String, otherUser: UserInfo, layout: RelativeLayout) {
        fireStore.pendingDocument(userId).get().addOnSuccessListener {
            val newRequests = removeId(it, friendId, PENDING_LIST)
            val newRequestDocument = mapOf(PENDING_LIST to newRequests)
            fireStore.pendingDocument(userId).set(newRequestDocument)
            fireStore.requestDocument(friendId).get().addOnSuccessListener { document ->
                val newRequestsList = removeId(document, userId, REQUEST_LIST)
                val newRequest = mapOf(REQUEST_LIST to newRequestsList)
                fireStore.requestDocument(friendId).set(newRequest)
            }
            BarAssistant.pendingRequests.remove(otherUser)
            setView(layout, otherUser)
            refresh()
            // notifyDataSetChanged()
        }
    }

    private fun addId(document: DocumentSnapshot, listName: String, item: String): MutableList<String> {
        val storedIds = getIdList(document, listName)
        storedIds.add(item)
        return storedIds
    }

    private fun removeId(document: DocumentSnapshot, listName: String, item: String): MutableList<String> {
        val storedIds = getIdList(document, listName)
        storedIds.remove(item)
        return storedIds
    }

    private fun getIdList(document: DocumentSnapshot, listName: String): MutableList<String> {
        val storedIds = document.get(listName) as? ArrayList<*>
        return storedIds?.toStringMutableList() ?: mutableListOf()
    }

    override fun getItemCount(): Int = users.count()
}
