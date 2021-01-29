package com.app.kiranachoice.listeners

import android.view.View
import com.app.kiranachoice.data.domain.Category

interface CategoryClickListener {
    fun onCategoryItemClick(view: View, category: Category)
}