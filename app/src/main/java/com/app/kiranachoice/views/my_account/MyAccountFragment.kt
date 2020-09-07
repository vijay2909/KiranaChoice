package com.app.kiranachoice.views.my_account

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.app.kiranachoice.DevicecureMapActivity
import com.app.kiranachoice.MainViewModel
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentMyAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView


class MyAccountFragment : Fragment(), View.OnClickListener {

    private var _bindingAccount: FragmentMyAccountBinding? = null
    private val binding get() = _bindingAccount!!
    private lateinit var viewModel: MainViewModel
    private lateinit var navController: NavController
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        _bindingAccount = FragmentMyAccountBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        mAuth = FirebaseAuth.getInstance()
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
            buttonLogin.setOnClickListener(this@MyAccountFragment)
            textSignOut.setOnClickListener(this@MyAccountFragment)
        }

        viewModel.user.observe(viewLifecycleOwner, {
            binding.user = it
            binding.invalidateAll()
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserDetails()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.textEditProfile.id -> navController.navigate(R.id.action_myAccountFragment_to_editProfileFragment)
            binding.textMyOrders.id -> navController.navigate(R.id.action_myAccountFragment_to_myOrdersFragment)
            binding.userImage.id -> {
                CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(requireContext(), this)
            }
            binding.textOurStore.id -> startActivity(
                Intent(
                    requireContext(),
                    DevicecureMapActivity::class.java
                )
            )
            binding.buttonLogin.id -> navController.navigate(R.id.action_myAccountFragment_to_authActivity)
            binding.textSignOut.id -> {
                mAuth.signOut()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.sign_out_msg),
                    Toast.LENGTH_SHORT
                ).show()
                view.findNavController().navigate(R.id.action_myAccountFragment_to_homeFragment)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri: Uri = result.uri
                binding.userImage.setImageURI(resultUri)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingAccount = null
    }
}