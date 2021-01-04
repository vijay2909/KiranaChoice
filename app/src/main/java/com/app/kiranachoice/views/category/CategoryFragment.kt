package com.app.kiranachoice.views.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.data.db.CartDatabase
import com.app.kiranachoice.data.domain.SubCategory
import com.app.kiranachoice.databinding.FragmentCategoryBinding
import com.app.kiranachoice.recyclerView_adapters.SubCategoryAdapter
import com.app.kiranachoice.repositories.DataRepository


class CategoryFragment : Fragment(), SubCategoryAdapter.SubCategoryClickListener {

    private var _bindingCategory: FragmentCategoryBinding? = null
    private val binding get() = _bindingCategory!!
    private lateinit var navController: NavController

    private val args: CategoryFragmentArgs by navArgs()
    private lateinit var viewModel : CategoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingCategory = FragmentCategoryBinding.inflate(inflater, container, false)
        val localDatabase = CartDatabase.getInstance(requireContext().applicationContext)
        val factory = CategoryViewModelFactory(args.categoryName, DataRepository(localDatabase.databaseDao))
        viewModel = ViewModelProvider(this, factory).get(CategoryViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        viewModel.subCategories.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                binding.recyclerViewSubCategory.apply {
                    setHasFixedSize(true)
                    adapter = SubCategoryAdapter(it, this@CategoryFragment)
                }
            }
            binding.shimmerLayout.rootLayout.stopShimmer()
            binding.shimmerLayoutRootLayout.visibility = View.GONE
            binding.actualUiLayout.visibility = View.VISIBLE
        })
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerLayout.rootLayout.startShimmer()
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerLayout.rootLayout.stopShimmer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingCategory = null
    }

    override fun onSubCategoryItemClicked(subCategory: SubCategory) {
        navController.navigate(
            CategoryFragmentDirections.actionCategoryFragmentToProductsFragment(subCategory.name)
        )
    }
}

class CategoryViewModelFactory(private val categoryName: String, private val dataRepository: DataRepository): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)){
            return CategoryViewModel(categoryName, dataRepository) as T
        }
        throw IllegalArgumentException ("Unknown ViewModel")
    }
}