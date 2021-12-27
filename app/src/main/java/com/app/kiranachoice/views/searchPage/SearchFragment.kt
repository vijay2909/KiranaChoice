package com.app.kiranachoice.views.searchPage

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentSearchBinding
import com.app.kiranachoice.recyclerView_adapters.SearchResultsAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _bindingSearch: FragmentSearchBinding? = null
    private val binding get() = _bindingSearch!!

    private var searchView: SearchView? = null

    private val viewModel: SearchViewModel by viewModels() /*by lazy {
        val factory = SearchViewModelFactory(requireActivity().application)
        ViewModelProvider(this, factory).get(SearchViewModel::class.java)
    }*/

    private lateinit var searchResultsAdapter: SearchResultsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingSearch = FragmentSearchBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setHasOptionsMenu(true)

        searchResultsAdapter = SearchResultsAdapter(SearchResultsAdapter.OnClickListener {
            viewModel.showSearchProducts(it)
        })

        binding.recyclerViewSearchItem.adapter = searchResultsAdapter

        viewModel.navigateToSelectedProduct.observe(viewLifecycleOwner, {
            if (null != it) {
                this.findNavController().navigate(
                    SearchFragmentDirections.actionShowProducts(it.categoryName)
                )
                viewModel.showSearchProductsComplete()
            }
        })

        return binding.root
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
        requireActivity().menuInflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.isIconified = false
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.getSearchWords(newText).observe(viewLifecycleOwner, {
                    searchResultsAdapter.submitList(it)
                })
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.getSearchWords(query).observe(viewLifecycleOwner, {
                    searchResultsAdapter.submitList(it)
                })
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        requireActivity().invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_mic) {
            getSpeechInput()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        try {
            resultLauncher.launch(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "Your Device Don't Support Speech Input",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            data?.let {
                val results = it.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                searchView?.setQuery(
                    results?.get(0).toString().toLowerCase(Locale.getDefault()),
                    true
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingSearch = null
    }


}