package com.app.kiranachoice.ui.my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentMyAccountBinding

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
        binding.textEditProfile.setOnClickListener(this)
        binding.textMyOrders.setOnClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingAccount = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.textEditProfile.id -> navController.navigate(R.id.action_myAccountFragment_to_editProfileFragment)
            binding.textMyOrders.id -> navController.navigate(R.id.action_myAccountFragment_to_myOrdersFragment)
        }
    }
}