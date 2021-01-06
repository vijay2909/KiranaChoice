package com.app.kiranachoice.views.chat

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentChatBinding
import com.app.kiranachoice.data.Chat
import com.app.kiranachoice.network.SendNotificationAPI
import com.app.kiranachoice.recyclerView_adapters.ChatAdapter
import com.app.kiranachoice.utils.ADMIN
import com.app.kiranachoice.utils.CHAT_REFERENCE
import com.app.kiranachoice.utils.USER
import com.app.kiranachoice.utils.UserPreferences
import com.app.kiranachoice.views.authentication.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var mLinearLayoutManager: LinearLayoutManager

    private var chatList = ArrayList<Chat>()

    private lateinit var mAuth: FirebaseAuth

    private lateinit var childEventListener: ChildEventListener

    private lateinit var dbRef: DatabaseReference

    private lateinit var userPreferences: UserPreferences

    private var totalChild = 0L

    var count = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()

        userPreferences = UserPreferences(requireContext())

        mLinearLayoutManager = LinearLayoutManager(requireContext())
        mLinearLayoutManager.stackFromEnd = true
        binding.messageRecyclerView.layoutManager = mLinearLayoutManager

        dbRef = FirebaseDatabase.getInstance().getReference(CHAT_REFERENCE).child(mAuth.currentUser?.uid.toString())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.messageEditText.addTextChangedListener {
            binding.sendButton.isEnabled = it.toString().trim().isNotEmpty()
        }

        binding.addCameraImage.setOnClickListener {
            findNavController().navigate(R.id.action_chatFragment_to_cameraFragment)
        }

        binding.sendButton.setOnClickListener {
            val msg = binding.messageEditText.text.toString()
            val chat = Chat(
                msg,
                System.currentTimeMillis(),
                null,
                USER /* no image */
            )

            // set value into database
            dbRef.push().setValue(chat)

            binding.messageEditText.setText("")
        }


        binding.addMessageImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE)
        }


        val chatAdapter = ChatAdapter()
        binding.messageRecyclerView.adapter = chatAdapter

        // get total children in inbox
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalChild = snapshot.childrenCount

                childEventListener = object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        ++count

                        snapshot.getValue(Chat::class.java)?.let { chat ->

                            if (count >= totalChild && chat.sender == USER && !chat.read) {
                                sendNotificationToAdmin(buildNotificationPayload(chat.msg.toString()))
                            }

                            // check receipt on admin msg
                            // let admin to know that user has been read msg
                            if (chat.sender == ADMIN) {
                                dbRef.child(snapshot.key.toString()).updateChildren(
                                    mapOf("read" to true)
                                )
                            }

                            chatList.add(chat)
                            chatAdapter.submitList(chatList)
                            if (_binding != null) binding.messageRecyclerView.scrollToPosition(
                                chatAdapter.itemCount - 1
                            )
                        }
                    }


                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        snapshot.getValue(Chat::class.java)?.let {
                            if (it.sender == USER) {
                                chatList[chatAdapter.itemCount - 1].read = it.read
                                chatAdapter.notifyItemChanged(chatAdapter.itemCount - 1)
                            }
                        }
                    }


                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        Log.e(TAG, "onChildRemoved: called")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        Log.w(TAG, "onChildMoved: called")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i(TAG, "onCancelled: called: ${error.message}")
                    }
                }

                dbRef.addChildEventListener(childEventListener)
            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }


    private fun sendNotificationToAdmin(payload: JsonObject) {

        SendNotificationAPI.getInstance().sendChatNotification(payload)
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {}

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {}

            })

    }


    private fun buildNotificationPayload(msg: String): JsonObject {
        val userName = userPreferences.getUserName()
        // compose notification json payload
        val payload = JsonObject()
        payload.addProperty("to", "/topics/adminChat")

        // compose data payload here
        val data = JsonObject()
        data.addProperty("title", userName)
        data.addProperty("body", msg)

        // add data payload
        payload.add("notification", data)

        return payload
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    val uri = data.data
                    Log.d(TAG, "Uri: " + uri.toString())
                    val tempMessage = Chat(
                        null, System.currentTimeMillis(), LOADING_IMAGE_URL,
                        USER
                    )
                    dbRef.push().setValue(tempMessage) { databaseError, databaseReference ->
                        if (databaseError == null) {
                            val key = databaseReference.key
                            val storageReference = FirebaseStorage.getInstance()
                                .getReference(mAuth.currentUser?.uid.toString())
                                .child(key!!)
                                .child(uri?.lastPathSegment.toString())
                            putImageInStorage(storageReference, uri, key)
                        }
                    }
                }
            }
        }
    }


    private fun putImageInStorage(storageReference: StorageReference, uri: Uri?, key: String) {
        if (uri != null) {
            storageReference.putFile(uri)
                .addOnSuccessListener {
                    it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        val chat = Chat(
                            null, System.currentTimeMillis(), uri.toString(),
                            USER
                        )

                        dbRef.child(key).setValue(chat)
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "Image upload task was not successful. ${it.message}")
                }
        }
    }


    override fun onStart() {
        super.onStart()
        count = 0
        if (mAuth.currentUser == null)
            startActivity(Intent(requireContext(), AuthActivity::class.java))
    }


    override fun onDestroy() {
        super.onDestroy()
        if (this::childEventListener.isInitialized)
            dbRef.removeEventListener(childEventListener)
        _binding = null
    }

    companion object {
        private const val TAG = "ChatFragment"
        private const val REQUEST_IMAGE = 2
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    }

}