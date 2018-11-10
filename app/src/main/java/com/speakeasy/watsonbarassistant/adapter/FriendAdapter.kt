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

class FriendAdapter(private val context: Context, private val users: MutableList<UserInfo>):
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
        val textView = layout.findViewById<TextView>(R.id.friendUsername)

        val user = users[position]
        textView.text = user.username

        val friendId = user.userId

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
                            removeFriend(uid, fid, user)
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
                            acceptFriendRequest(uid, fid, user)
                        }
                    }
                }

                denyButton.setOnClickListener {
                    fireAuth.currentUser?.uid?.let { uid ->
                        friendId?.let { fid ->
                            denyFriend(uid, fid, user)
                        }
                    }
                }
            }
            in BarAssistant.pendingRequests -> {
                requestButton.text = "Cancel"
                requestButton.setOnClickListener {
                    fireAuth.currentUser?.uid?.let { uid ->
                        friendId?.let { fid ->
                            cancelRequest(uid, fid, user)
                        }
                    }
                }
            }
            else -> {
                requestButton.setOnClickListener { _ ->
                    val currentUid = fireAuth.currentUser?.uid
                    currentUid?.let { uid ->
                        friendId?.let { fid ->
                            createFriendRequest(uid, fid, user)
                        }
                    }
                }
            }
        }
    }

    private fun createFriendRequest(userId: String, friendId: String, user: UserInfo) {
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
                        notifyDataSetChanged()
                    }.addOnFailureListener {
                        context.toast("Failed to request ${user.username} as a friend.")
                    }
                    fireStore.pendingDocument(friendId).get().addOnSuccessListener {
                        val storedIds = getIdList(it, PENDING_LIST)
                        if(userId in storedIds) {
                            acceptFriendRequest(userId, friendId, user)
                        }
                    }
                }.addOnFailureListener { _ ->
                    context.toast("Failed to request ${user.username} as a friend.")
                }
            }
        }
    }

    private fun acceptFriendRequest(userId: String, friendId: String, user: UserInfo) {
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
                    removePendingRequest(user, userId)
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

    private fun removePendingRequest(user: UserInfo, userId: String) {
        fireStore.requestDocument(userId).get().addOnSuccessListener {
            user.userId?.let { uid ->
                val storedStringIds = removeId(it, REQUEST_LIST, uid)
                val newFriendRequests = mapOf(REQUEST_LIST to storedStringIds)
            fireStore.requestDocument(userId).set(newFriendRequests).addOnSuccessListener { _ ->
                    addFriend(user, userId)
                }
            }
        }

    }

    private fun addFriend(user: UserInfo, userId: String) {
        fireStore.friendDocument(userId).get().addOnSuccessListener {
            user.userId?.let { friendId ->
                val storedIds = addId(it, FRIEND_LIST, friendId)
                val newFriends = mapOf(FRIEND_LIST to storedIds)
                fireStore.friendDocument(userId).set(newFriends)
                BarAssistant.requestsInProgress.remove(user)
                BarAssistant.friends.add(user)
                notifyDataSetChanged()
                context.toast("Added ${user.username} as a friend.")
            }
        }.addOnFailureListener {
            context.toast("Failed to add ${user.username} as a friend.")
        }
    }

    private fun removeFriend(userId: String, friendId: String, otherUser: UserInfo) {
        fireStore.friendDocument(userId).get().addOnSuccessListener {
            removeId(it, FRIEND_LIST, friendId)
            fireStore.friendDocument(friendId).get().addOnSuccessListener { document ->
                removeId(document, FRIEND_LIST, userId)
            }
            BarAssistant.friends.remove(otherUser)
            notifyDataSetChanged()
            context.toast("Removed ${otherUser.username} as a friend.")
        }.addOnFailureListener {
            context.toast("Failed to remove ${otherUser.username} as a friend.")
        }
    }

    private fun denyFriend(userId: String, friendId: String, otherUser: UserInfo) {
        fireStore.requestDocument(userId).get().addOnSuccessListener {
            removeId(it, friendId, REQUEST_LIST)
            fireStore.pendingDocument(friendId).get().addOnSuccessListener { document ->
                removeId(document, userId, PENDING_LIST)
            }
            BarAssistant.pendingRequests.remove(otherUser)
            notifyDataSetChanged()
        }
    }

    private fun cancelRequest(userId: String, friendId: String, otherUser: UserInfo) {
        fireStore.pendingDocument(userId).get().addOnSuccessListener {
            removeId(it, friendId, PENDING_LIST)
            fireStore.requestDocument(friendId).get().addOnSuccessListener { document ->
                removeId(document, userId, REQUEST_LIST)
            }
            BarAssistant.requestsInProgress.remove(otherUser)
            notifyDataSetChanged()
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