package com.app.kiranachoice.views.checkout_product

import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.ActivityCheckoutBinding

class CheckoutActivity : AppCompatActivity() {

    private lateinit var bindingCheckout: ActivityCheckoutBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: CheckoutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingCheckout = DataBindingUtil.setContentView(this, R.layout.activity_checkout)

        val toolbar = bindingCheckout.toolbarCheckout
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val factory = CheckoutViewModelFactory(this.application)
        viewModel = ViewModelProvider(this, factory).get(CheckoutViewModel::class.java)

        navController = findNavController(R.id.checkout_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

@Suppress("UNCHECKED_CAST")
class CheckoutViewModelFactory(val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CheckoutViewModel(application) as T
    }

}