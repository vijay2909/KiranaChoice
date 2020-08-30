package com.app.kiranachoice.ui.category

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.adapters.SubCategoryAdapter
import com.app.kiranachoice.databinding.FragmentCategoryBinding


class CategoryFragment : Fragment() {

    private var _bindingCategory: FragmentCategoryBinding? = null
    private val binding get() = _bindingCategory!!
    private val viewModel by viewModels<CategoryViewModel>()

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

        viewModel.getSubCategories(args.categoryModel)

        viewModel.subCategoryList.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                binding.recyclerViewSubCategory.apply {
                    setHasFixedSize(true)
                    adapter = SubCategoryAdapter(it)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingCategory = null
    }
}