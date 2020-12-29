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
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.app.kiranachoice.KiranaChoiceMapActivity
import com.app.kiranachoice.MainViewModel
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentMyAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage


class MyAccountFragment : Fragment(), View.OnClickListener {

    private var _bindingAccount: FragmentMyAccountBinding? = null
    private val binding get() = _bindingAccount!!
    private lateinit var navController: NavController
    private lateinit var mAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingAccount = FragmentMyAccountBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        mAuth = FirebaseAuth.getInstance()
        return binding.root
    }


    private val viewModel : MainViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.apply {
            textEditProfile.setOnClickListener(this@MyAccountFragment)
            textMyOrders.setOnClickListener(this@MyAccountFragment)
            textOurStore.setOnClickListener(this@MyAccountFragment)
            textSignOut.setOnClickListener(this@MyAccountFragment)
        }

        viewModel.user.observe(viewLifecycleOwner, {
            binding.user = it
            binding.invalidateAll()
        })
    }


    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser == null) {
            this.findNavController().navigate(R.id.action_myAccountFragment_to_authActivity)
        } else {
            binding.progressBar.root.visibility = View.GONE
        }
    }

    
    override fun onClick(view: View?) {
        when (view?.id) {
            binding.textEditProfile.id -> navController.navigate(R.id.action_myAccountFragment_to_editProfileFragment)
            binding.textMyOrders.id -> {
                navController.navigate(R.id.action_myAccountFragment_to_myOrdersFragment)
            }
            binding.textOurStore.id -> startActivity(
                Intent(
                    requireContext(),
                    KiranaChoiceMapActivity::class.java
                )
            )
            binding.textSignOut.id -> {
                mAuth.signOut()
                binding.invalidateAll()
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