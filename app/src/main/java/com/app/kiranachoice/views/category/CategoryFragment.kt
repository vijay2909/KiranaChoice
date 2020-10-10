package com.app.kiranachoice.views.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.recyclerView_adapters.SubCategoryAdapter
import com.app.kiranachoice.databinding.FragmentCategoryBinding
import com.app.kiranachoice.models.SubCategoryModel


class CategoryFragment : Fragment(), SubCategoryAdapter.SubCategoryClickListener {

    private var _bindingCategory: FragmentCategoryBinding? = null
    private val binding get() = _bindingCategory!!
    private val viewModel by viewModels<CategoryViewModel>()
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bindingCategory = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }


    private val args: CategoryFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        viewModel.getSubCategories(args.categoryModel)

        viewModel.subCategoryList.observe(viewLifecycleOwner, {
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

    override fun onSubCategoryItemClicked(subCategoryModel: SubCategoryModel) {
        navController.navigate(
            CategoryFragmentDirections.actionCategoryFragmentToProductsFragment(
                subCategoryModel, subCategoryModel.sub_category_name.toString()
            )
        )
    }
}