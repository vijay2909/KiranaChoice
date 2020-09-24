package com.app.kiranachoice.views.edit_profile

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.app.kiranachoice.MainViewModel
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentEditProfileBinding
import com.app.kiranachoice.MainViewModelFactory
import com.app.kiranachoice.views.authentication.AuthActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

private const val PICK_IMAGE_REQUEST = 1

class EditProfileFragment : Fragment(), View.OnClickListener {

    private var _bindingEdit: FragmentEditProfileBinding? = null
    private val binding get() = _bindingEdit!!

    private lateinit var viewModel: MainViewModel
    private var imageUri: Uri? = null

    private lateinit var mAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainViewModelFactory = MainViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), mainViewModelFactory).get(MainViewModel::class.java)
        _bindingEdit = FragmentEditProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.mainViewModel = viewModel
        mAuth = FirebaseAuth.getInstance()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            editUserImage.setOnClickListener(this@EditProfileFragment)
            btnUpdateDetails.setOnClickListener(this@EditProfileFragment)
        }

        viewModel.onDetailsUpdate.observe(viewLifecycleOwner, {
            if (it) {
                Toast.makeText(requireContext(), getString(R.string.update_msg), Toast.LENGTH_SHORT)
                    .show()
                viewModel.onDetailsUpdated()
                view.findNavController().popBackStack()
            }
        })

        viewModel.imageUrl.observe(viewLifecycleOwner, {
            Glide.with(requireContext()).asBitmap()
                .load(it)
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.signatureOf(ObjectKey(System.currentTimeMillis())))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
                .into(binding.circleImageView)
            binding.textPleaseWait.visibility = View.GONE
        })
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let {
                        launchImageCrop(it)
                    }
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    imageUri = result.uri
                    viewModel.fileExtension = getFileExtension(imageUri)
                    binding.textPleaseWait.visibility = View.VISIBLE
                    viewModel.updateImage(imageUri)
                }
            }
        }
    }

    private fun launchImageCrop(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(requireContext(), this)
    }

    private fun getFileExtension(uri: Uri?): String? {
        val cR: ContentResolver? = activity?.contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(uri?.let { cR?.getType(it) })
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser == null) {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }else{
            // progress bar is running.. if user logged in then stop the progress bar
            binding.progressBar.root.visibility = View.GONE
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.editUserImage.id -> pickFromGallery()
            binding.btnUpdateDetails.id -> {
                binding.textPleaseWait.visibility = View.VISIBLE
                binding.btnUpdateDetails.visibility = View.GONE
                viewModel.userName = binding.etUserName.text.toString().trim()
                viewModel.saveData()
            }
        }
    }

}