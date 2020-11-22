package com.app.kiranachoice.views.my_offers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.app.kiranachoice.databinding.FragmentMyOffersBinding
import com.app.kiranachoice.models.OfferModel
import com.app.kiranachoice.recyclerView_adapters.OffersAdapter

class MyOffersFragment : Fragment(), OffersAdapter.OfferClickListener {

    private var _binding: FragmentMyOffersBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MyOffersViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(MyOffersViewModel::class.java)
        _binding = FragmentMyOffersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val offersAdapter = OffersAdapter(this)
        binding.recyclerOfferList.adapter = offersAdapter

        viewModel.offersList.observe(viewLifecycleOwner, {
            offersAdapter.list = it
            binding.progressBar.root.visibility = View.GONE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onOfferItemClicked(offersModel: OfferModel) {
        findNavController().navigate(MyOffersFragmentDirections.actionMyOffersFragmentToOfferDetailFragment(offersModel))
    }

}