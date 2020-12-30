package com.app.kiranachoice

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.app.kiranachoice.data.db.CartDatabase
import com.app.kiranachoice.databinding.ActivityMainBinding
import com.app.kiranachoice.databinding.NavHeaderMainBinding
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.views.authentication.AuthActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private lateinit var navController: NavController

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val localDatabase = CartDatabase.getInstance(applicationContext)
        val mainViewModelFactory = MainViewModelFactory(DataRepository(localDatabase.databaseDao))
        viewModel = ViewModelProvider(this, mainViewModelFactory).get(MainViewModel::class.java)

        mAuth = FirebaseAuth.getInstance()

        val navHeader: NavHeaderMainBinding = DataBindingUtil.inflate(
            layoutInflater, R.layout.nav_header_main, binding.navView, false
        )
        navHeader.lifecycleOwner = this

        viewModel.user.observe(this, { user ->
            navHeader.user = user
        })

        binding.textAppVersion.text =
            getString(R.string.app_version).plus(" ${BuildConfig.VERSION_NAME}")

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

        /*viewModel.allCartItems.observe(this, {
            it?.let {
                totalCartItem = it.size
                invalidateOptionsMenu()
            }
        })*/

        binding.navView.setNavigationItemSelectedListener(this)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.cartFragment, R.id.contactUsFragment,
                R.id.myOrdersFragment, R.id.editProfileFragment,
                R.id.addressFragment, R.id.paymentFragment, R.id.chatFragment -> {
                    binding.appBarMain.bottomNavView.visibility = View.GONE
                }
                else -> {
                    binding.appBarMain.bottomNavView.visibility = View.VISIBLE
                }
            }
        }
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


    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
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
    }*/


    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.cartFragment){
            if (mAuth.currentUser == null){
                startActivity(Intent(this, AuthActivity::class.java))
            }else {
                navController.navigate(R.id.cartFragment)
            }
        }
        return *//*item.onNavDestinationSelected(navController) || *//*super.onOptionsItemSelected(item)
    }*/


    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }



    private fun onShareClicked() {

        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            val msg = "https://play.google.com/store/apps/details?id=$packageName"
            shareIntent.putExtra(Intent.EXTRA_TEXT, msg)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val handled = NavigationUI.onNavDestinationSelected(item, navController)
        if (!handled) {
            when (item.itemId) {
                R.id.nav_share -> {
                    onShareClicked()
                }
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

}


class MainViewModelFactory(private val dataRepository: DataRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(dataRepository = dataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}