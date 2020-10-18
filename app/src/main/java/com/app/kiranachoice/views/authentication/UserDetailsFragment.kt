package com.app.kiranachoice.views.authentication

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentUserDetailsBinding

class UserDetailsFragment : Fragment() {

    private var _bindingUserDetails: FragmentUserDetailsBinding? = null
    private val binding get() = _bindingUserDetails!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bindingUserDetails = FragmentUserDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    private val viewModel: AuthViewModel by activityViewModels()
    private val args: UserDetailsFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etPhoneNumber.setText(args.phoneNumber)

        binding.etEmail.addTextChangedListener {
            val name = binding.etName.text.toString().trim()
            setSubmitButtonVisibility(name, it.toString())
        }

        binding.etName.addTextChangedListener {
            val email = binding.etEmail.text.trim()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = null
                setSubmitButtonVisibility(it.toString(), email.toString())
            } else {
                binding.etEmail.error = ""
            }
        }

        binding.btnSubmit.setOnClickListener {
            binding.progressBar.root.visibility = View.VISIBLE
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            viewModel.saveUser(name, email)
        }

        viewModel.userAlreadyExist.observe(viewLifecycleOwner, {
            if (it){
                viewModel.eventUserAlreadyExistFinished()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.login_successful),
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().finish()
            }
        })
    }


    private fun setSubmitButtonVisibility(name: String?, email: String?) {
        if (name.isNullOrEmpty() || email.isNullOrEmpty()) {
            binding.btnSubmit.isEnabled = false
            binding.btnSubmit.icon = null
        } else {
            binding.btnSubmit.isEnabled = true
            binding.btnSubmit.icon =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_check_white, null)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _bindingUserDetails = null
    }

}