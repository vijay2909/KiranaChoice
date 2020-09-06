package com.app.kiranachoice.ui.my_account

import android.R.attr
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.app.kiranachoice.DevicecureMapActivity
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentMyAccountBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView


class MyAccountFragment : Fragment(), View.OnClickListener {

    private var _bindingAccount: FragmentMyAccountBinding? = null
    private val binding get() = _bindingAccount!!
    private val viewModel by viewModels<MyAccountViewModel>()
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bindingAccount = FragmentMyAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.apply {
            textEditProfile.setOnClickListener(this@MyAccountFragment)
            textMyOrders.setOnClickListener(this@MyAccountFragment)
            userImage.setOnClickListener(this@MyAccountFragment)
            textOurStore.setOnClickListener(this@MyAccountFragment)
        }

        binding.buttonLogin.setOnClickListener{
            it.findNavController().navigate(R.id.action_myAccountFragment_to_authFragment)
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            binding.textEditProfile.id -> navController.navigate(R.id.action_myAccountFragment_to_editProfileFragment)
            binding.textMyOrders.id -> navController.navigate(R.id.action_myAccountFragment_to_myOrdersFragment)
            binding.userImage.id -> {
                CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(requireContext(), this);
            }
            binding.textOurStore.id -> startActivity(
                Intent(
                    requireContext(),
                    DevicecureMapActivity::class.java
                )
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri: Uri = result.uri
                binding.userImage.setImageURI(resultUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.e(TAG, "onActivityResult: error : ${error.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingAccount = null
    }

    companion object {
        private const val TAG = "MyAccountFragment"
    }
}