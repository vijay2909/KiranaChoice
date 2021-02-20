package com.app.kiranachoice.views.chat

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kiranachoice.R
import com.app.kiranachoice.data.network_models.Chat
import com.app.kiranachoice.databinding.FragmentChatBinding
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
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject


class ChatFragment : Fragment(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var mLinearLayoutManager: LinearLayoutManager

    private var chatList = ArrayList<Chat>()

    private lateinit var mAuth: FirebaseAuth

    private lateinit var childEventListener: ChildEventListener

    private lateinit var dbRef: DatabaseReference

    @Inject
    lateinit var userPreferences: UserPreferences

    private var totalChild = 0L

    @Inject
    lateinit var sendChatNotificationAPI: SendNotificationAPI

    var count = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()

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
            hasPermissionAllowed()
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
                        Timber.e( "onChildRemoved: called")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        Timber.w( "onChildMoved: called")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Timber.i( "onCancelled: called: ${error.message}")
                    }
                }

                dbRef.addChildEventListener(childEventListener)
            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }


    @AfterPermissionGranted(REQUEST_CODE_PERMISSIONS)
    private fun hasPermissionAllowed() {
        if (EasyPermissions.hasPermissions(requireContext(), CAMERA_PERMISSION)) {
            // Already have permission, do the thing
            findNavController().navigate(R.id.action_chatFragment_to_cameraFragment)
        } else {
            val request = PermissionRequest.Builder(
                this, REQUEST_CODE_PERMISSIONS, CAMERA_PERMISSION)
//                .setTheme(R.style.my_fancy_style)
                .setRationale(R.string.rationale_camera)
                .setPositiveButtonText(R.string.rationale_ok)
                .build()
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(request)
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Timber.d( "onPermissionsDenied: called")
        hasPermissionAllowed()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Timber.w( "onPermissionsGranted: called")
        hasPermissionAllowed()
    }

    override fun onRationaleAccepted(requestCode: Int) {
        Timber.i( "onRationaleAccepted: called")
        hasPermissionAllowed()
    }

    override fun onRationaleDenied(requestCode: Int) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, CAMERA_ARRAY_PERMISSION)) {
            Timber.d( "onRationaleDenied: if block")
            AppSettingsDialog.Builder(this).build().show()
        }else{
            Timber.e( "onRationaleDenied: called")
            hasPermissionAllowed()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    private fun sendNotificationToAdmin(payload: JsonObject) {

        sendChatNotificationAPI.sendChatNotification(payload)
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
        Timber.d( "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    val uri = data.data
                    Timber.d( "Uri: %s", uri.toString())
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
                    Timber.w( "Image upload task was not successful. ${it.message}")
                }
        }
    }


    override fun onStart() {
        super.onStart()
        count = 0
        if (mAuth.currentUser == null) {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            findNavController().popBackStack()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (this::childEventListener.isInitialized)
            dbRef.removeEventListener(childEventListener)
        _binding = null
    }

    companion object {
        private const val REQUEST_IMAGE = 2
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"

        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
//        private val CAMERA_ARRAY_PERMISSION = arrayOf(Manifest.permission.CAMERA)
        private val CAMERA_ARRAY_PERMISSION = listOf(Manifest.permission.CAMERA)

    }

}