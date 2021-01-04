package com.app.kiranachoice.listeners

import com.app.kiranachoice.data.domain.Category

interface CategoryClickListener {
    fun onCategoryItemClick(category: Category)
}