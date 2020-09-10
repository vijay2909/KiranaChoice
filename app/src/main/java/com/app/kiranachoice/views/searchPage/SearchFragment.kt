package com.app.kiranachoice.views.searchPage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.kiranachoice.MainViewModel
import com.app.kiranachoice.databinding.FragmentSearchBinding
import com.app.kiranachoice.recyclerView_adapters.SearchResultsAdapter
import com.app.kiranachoice.views.MainViewModelFactory
import java.util.*

class SearchFragment : Fragment() {

    private var _bindingSearch: FragmentSearchBinding? = null
    private val binding get() = _bindingSearch!!

    private lateinit var viewModel: MainViewModel
    private var searchResultsAdapter: SearchResultsAdapter? = null

    private var userInput: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainViewModelFactory = MainViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), mainViewModelFactory).get(MainViewModel::class.java)
        _bindingSearch = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(input: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.searchButton.visibility = View.VISIBLE
                binding.searchProgress.visibility = View.INVISIBLE
            }

            override fun onTextChanged(input: CharSequence?, start: Int, before: Int, count: Int) {
                userInput = input.toString().toLowerCase(Locale.getDefault())
            }

            override fun afterTextChanged(p0: Editable?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.searchButton.visibility = View.INVISIBLE
                    binding.searchProgress.visibility = View.VISIBLE
                    userInput?.let { viewModel.getResultFromProducts(it) }
                }, 500)
            }

        })

        viewModel.resultList.observe(viewLifecycleOwner, {
            binding.searchProgress.visibility = View.INVISIBLE
            binding.searchButton.visibility = View.VISIBLE
            if (!it.isNullOrEmpty()) {
                binding.recyclerViewSearchItem.apply {
                    addItemDecoration(
                        DividerItemDecoration(
                            requireContext(),
                            DividerItemDecoration.VERTICAL
                        )
                    )
                    searchResultsAdapter = SearchResultsAdapter(it)
                    adapter = searchResultsAdapter
                }
            } else {
                binding.recyclerViewSearchItem.visibility = View.GONE
                binding.textNoResult.visibility = View.VISIBLE
            }
            searchResultsAdapter?.notifyDataSetChanged()
        })

        binding.micButton.setOnClickListener { getSpeechInput() }
    }

    private fun getSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, 10)
        } else {
            Toast.makeText(
                requireContext(),
                "Your Device Don't Support Speech Input",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            10 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    binding.searchBox.setText(result?.get(0))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingSearch = null
    }

}