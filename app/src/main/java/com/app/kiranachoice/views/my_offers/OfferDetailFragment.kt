package com.app.kiranachoice.views.my_offers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.databinding.FragmentOfferDetailBinding
import com.app.kiranachoice.recyclerView_adapters.TermsAdapter


class OfferDetailFragment : Fragment() {

    private var _binding : FragmentOfferDetailBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOfferDetailBinding.inflate(inflater, container, false)
        return binding.root
    }


    private val args : OfferDetailFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.model = args.offerModel

        val termsAdapter = TermsAdapter()
        binding.recyclerTermsList.adapter = termsAdapter

        termsAdapter.list = args.offerModel.terms
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}