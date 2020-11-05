package com.app.kiranachoice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.app.kiranachoice.databinding.ActivityMainBinding
import com.app.kiranachoice.databinding.NavHeaderMainBinding
import com.app.kiranachoice.views.authentication.AuthActivity
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private lateinit var navController: NavController

    private var totalCartItem = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val mainViewModelFactory = MainViewModelFactory(this.application)
        viewModel = ViewModelProvider(this, mainViewModelFactory).get(MainViewModel::class.java)


        val navHeader: NavHeaderMainBinding = DataBindingUtil.inflate(
            layoutInflater, R.layout.nav_header_main, binding.navView, false
        )
        navHeader.lifecycleOwner = this

        viewModel.user.observe(this, {
            navHeader.user = it
        })

        binding.navView.addHeaderView(navHeader.root)

        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        navController = host.navController

        navHeader.buttonLogin.setOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.editProfileFragment,
                R.id.myOrdersFragment,
                R.id.contactUsFragment,
                R.id.homeFragment,
                R.id.chatFragment,
                R.id.myOffersFragment,
                R.id.myAccountFragment
            ), binding.drawerLayout
        )

        setupActionBar()

        setupNavigationMenu()

        setupBottomNavMenu()

        viewModel.allCartItems.observe(this, {
            it?.let {
                totalCartItem = it.size
                invalidateOptionsMenu()
            }
        })

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.cartFragment, R.id.contactUsFragment,
                R.id.myOrdersFragment, R.id.editProfileFragment,
                R.id.addressFragment, R.id.paymentFragment -> {
                    binding.appBarMain.bottomNavView.visibility = View.GONE
                }
                else -> {
                    binding.appBarMain.bottomNavView.visibility = View.VISIBLE
                }
            }
        }

        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                val productId = deepLink.toString().substringAfter("=")

            }
            .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    private fun setupActionBar() {
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun setupNavigationMenu() {
        binding.navView.setupWithNavController(navController)
    }

    private fun setupBottomNavMenu() {
        binding.appBarMain.bottomNavView.setupWithNavController(navController)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val menuItem = menu.findItem(R.id.cartFragment)

        val actionView = menuItem.actionView

        val cartBadgeTextView = actionView.findViewById<TextView>(R.id.cart_badge_text_view)

        if (totalCartItem == 0) {
            cartBadgeTextView.visibility = View.GONE
        } else {
            cartBadgeTextView.text = totalCartItem.toString()
            cartBadgeTextView.visibility = View.VISIBLE
        }

        actionView.setOnClickListener { onOptionsItemSelected(menuItem) }

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }


    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }


    override fun onStart() {
        super.onStart()
        viewModel.getUserDetails()
    }
}