package com.app.kiranachoice

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.app.kiranachoice.databinding.ActivityMainBinding
import com.app.kiranachoice.databinding.NavHeaderMainBinding
import com.app.kiranachoice.views.authentication.AuthActivity

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

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

        val navController = findNavController(R.id.nav_host_fragment)

        navHeader.buttonLogin.setOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.appBarMain.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.cartFragment,
                R.id.editProfileFragment,
                R.id.myOrdersFragment,
                R.id.feedbackFragment,
                R.id.contactUsFragment,
                R.id.homeFragment,
                R.id.chatFragment,
                R.id.myOffersFragment,
                R.id.myAccountFragment
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        binding.appBarMain.bottomNavView.setupWithNavController(navController)

        viewModel.getAllCartItems().observe(this, {
            it?.let {
                totalCartItem = it.size
                invalidateOptionsMenu()
            }
        })

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.cartFragment -> {
                    binding.appBarMain.bottomNavView.visibility = View.GONE
                }
                else -> {
                    binding.appBarMain.bottomNavView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val menuItem = menu.findItem(R.id.cartFragment)

        val actionView = menuItem.actionView

        val cartBadgeTextView = actionView.findViewById<TextView>(R.id.cart_badge_text_view)

        if (totalCartItem == 0) {
            cartBadgeTextView.visibility = View.GONE
        }else {
            cartBadgeTextView.text = totalCartItem.toString()
            cartBadgeTextView.visibility = View.VISIBLE
        }

        actionView.setOnClickListener { onOptionsItemSelected(menuItem) }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment))
                || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getUserDetails()
    }
}