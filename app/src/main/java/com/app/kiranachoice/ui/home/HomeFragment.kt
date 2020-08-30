package com.app.kiranachoice.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.app.kiranachoice.adapters.CategoryAdapter
import com.app.kiranachoice.adapters.ProductsAdapter
import com.app.kiranachoice.databinding.FragmentHomeBinding
import com.app.kiranachoice.models.CategoryModel

class HomeFragment : Fragment(), CategoryAdapter.CategoryClickListener {

    private var _bindingHome : FragmentHomeBinding? = null
    private val binding get() = _bindingHome!!
    private val homeViewModel by viewModels<HomeViewModel>()
    private lateinit var navController: NavController

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _bindingHome = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        homeViewModel.categoryList.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()){
                binding.recyclerViewCategory1.apply {
                    setHasFixedSize(true)
                    adapter = CategoryAdapter(it, this@HomeFragment)
                }
            }
        })


        binding.recyclerViewCategory2.apply {
            adapter = ProductsAdapter(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingHome = null
    }

    override fun onCategoryItemClick(categoryModel: CategoryModel) {
        navController.navigate(HomeFragmentDirections.actionNavHomeToCategoryFragment(categoryModel, categoryModel.category_name.toString()))
    }
}