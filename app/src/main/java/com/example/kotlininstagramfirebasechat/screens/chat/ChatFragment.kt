package com.example.kotlininstagramfirebasechat.screens.chat


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import com.example.kotlininstagramfirebasechat.MainActivity
import com.example.kotlininstagramfirebasechat.R
import com.example.kotlininstagramfirebasechat.models.ChatMessage
import com.example.kotlininstagramfirebasechat.models.User
import com.example.kotlininstagramfirebasechat.utils.DateUtils.getFormattedTimeChatLog
import com.example.kotlininstagramfirebasechat.utils.FirebaseHelper
import com.example.kotlininstagramfirebasechat.utils.hideKeyboard
import com.example.kotlininstagramfirebasechat.utils.showToast
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_chat_current_user.view.*
import kotlinx.android.synthetic.main.item_chat_companion_user.view.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.item_chat_companion_user_light.view.*
import kotlinx.android.synthetic.main.item_chat_current_user_light.view.*
import java.lang.Exception

class ChatFragment : Fragment(R.layout.fragment_chat) {

    companion object {
        val TAG = ChatFragment::class.java.simpleName
    }

    private var companionUser = User()
    private var currentUser = User()
    private lateinit var firebase: FirebaseHelper

    val adapter = GroupAdapter<ViewHolder>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebase = FirebaseHelper()
        firebase.currentUserReference().addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(data: DataSnapshot) {
                currentUser = data.getValue(User::class.java) ?: User()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ", error.toException())
            }

        })

        arguments?.let {
            val uid = ChatFragmentArgs.fromBundle(it).companionUserUid
            firebase.userReference(uid).addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(data: DataSnapshot) {
                    companionUser = data.getValue(User::class.java) ?: User()
                    listenForMessages()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ", error.toException())
                }

            })
        }

        recyclerview_chat_log.adapter = adapter

        send_button_chat_log.setOnClickListener { performSendMessage() }
    }

    private fun listenForMessages() {
        var sameUser = User().uid
        val fromId = currentUser.uid
        val toId = companionUser.uid
        val ref = firebase.messages(fromId, toId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "database error: " + databaseError.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "has children: " + dataSnapshot.hasChildren())
            }
        })

        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                dataSnapshot.getValue(ChatMessage::class.java)?.let {

                    if (it.fromId == currentUser.uid) {
                        if (it.fromId == sameUser) {
                            adapter.add(ChatFromItemLight(it.text))
                        } else {
                            sameUser = it.fromId
                            adapter.add(ChatFromItem(it.text, it.timestamp))
                        }
                    } else {
                        if (it.fromId == sameUser) {
                            adapter.add(ChatToItemLight(it.text))
                        } else {
                            sameUser = it.fromId
                            adapter.add(ChatToItem(it.text, it.timestamp))
                        }
                    }
                }
                try {
                    recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
                } catch (e: Exception) {
                    Log.d(TAG, e.message ?: return)
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        })
    }

    private fun performSendMessage() {
        val text = edittext_chat_log.text.toString()
        if (text.isEmpty()) return

        val fromId = currentUser.uid
        val toId = companionUser.uid

        val reference = firebase.messages(fromId, toId).push()
        val toReference = firebase.messages(toId, fromId).push()

        val chatMessage =
            ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message: ${reference.key}")
                edittext_chat_log.text.clear()
                recyclerview_chat_log.smoothScrollToPosition(adapter.itemCount - 1)
            }

        toReference.setValue(chatMessage)

        firebase.latestMessages(fromId, toId).setValue(chatMessage)
        firebase.latestMessages(toId, fromId).setValue(chatMessage)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard(activity as MainActivity)
    }

}

class ChatFromItem(val text: String, private val timestamp: Long) :
    Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.run {
            textview_from_row.text = text
            from_msg_time.text = getFormattedTimeChatLog(timestamp)
        }

    }

    override fun getLayout(): Int {
        return R.layout.item_chat_current_user
    }

}

class ChatToItem(val text: String, private val timestamp: Long) :
    Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.run {
            textview_to_row.text = text
            to_msg_time.text = getFormattedTimeChatLog(timestamp)
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_chat_companion_user
    }

}

class ChatFromItemLight(val text: String) :
    Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.run {
            textview_from_row_light.text = text
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_chat_current_user_light
    }

}

class ChatToItemLight(val text: String) :
    Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.run {
            textview_to_row_light.text = text
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_chat_companion_user_light
    }
}