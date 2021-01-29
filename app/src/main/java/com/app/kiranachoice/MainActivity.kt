package com.app.kiranachoice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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


class MainActivity : AppCompatActivity()/*BaseActivity()*/, NavigationView.OnNavigationItemSelectedListener {

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


        binding.navView.setNavigationItemSelectedListener(this)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.cartFragment, R.id.contactUsFragment,
                R.id.myOrdersFragment, R.id.editProfileFragment,
                R.id.addressFragment, R.id.paymentFragment, R.id.chatFragment -> {
                    binding.appBarMain.bottomNavView.visibility = View.GONE
                    supportActionBar?.show()
                }
                R.id.cameraFragment -> {
                    binding.appBarMain.bottomNavView.visibility = View.GONE
                    supportActionBar?.hide()
                }
                else -> {
                    binding.appBarMain.bottomNavView.visibility = View.VISIBLE
                    supportActionBar?.show()
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

    override fun onStart() {
        super.onStart()
        registerReceiver(networkErrorBroadCast, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkErrorBroadCast)
    }


    private val networkErrorBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val notConnected = intent?.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) ?: return
            if (notConnected) {
                disconnected()
            } else {
                connected()
            }
        }
    }

    private fun connected() {
        binding.appBarMain.imgNoInternet.visibility = View.INVISIBLE
    }

    private fun disconnected() {
        binding.appBarMain.imgNoInternet.visibility = View.VISIBLE
    }

    companion object{
        private const val TAG = "MainActivity"
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