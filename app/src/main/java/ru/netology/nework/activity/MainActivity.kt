package ru.netology.nework.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.ActivityMainBinding
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: AuthViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels()

    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapKitFactory.initialize(this)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            postViewModel.padding = systemBars.bottom
            insets
        }

        requestNotificationsPermission()

        bottomNav = binding.bottomNavigation

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        bottomNav.setupWithNavController(navController)

        val appBarConfiguration =
            AppBarConfiguration(setOf(R.id.postsFragment, R.id.eventsFragment, R.id.usersFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isMainScreen = destination.id == R.id.postsFragment ||
                    destination.id == R.id.eventsFragment ||
                    destination.id == R.id.usersFragment
            bottomNav.isVisible = isMainScreen
        }


        supportActionBar?.let { actionBar ->
            actionBar.title = getString(R.string.app_name)
        }




        viewModel.data.observe(this) {
            invalidateOptionsMenu()
        }

        addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.menu_main, menu)

                    menu.setGroupVisible(R.id.authenticated, viewModel.isAuthorized)
                    menu.setGroupVisible(R.id.unauthenticated, !viewModel.isAuthorized)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.signIn -> {
                            findNavController(R.id.nav_host_fragment).navigate(R.id.signInFragment)
                            true
                        }

                        R.id.signUp -> {
                            findNavController(R.id.nav_host_fragment).navigate(R.id.signUpFragment)
                            true
                        }

                        R.id.logout -> {
                            viewModel.removeAuth()
                            true
                        }

                        else -> false
                    }

            }
        )


    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sign_out)
            .setMessage(R.string.are_you_sure)
            .setPositiveButton(R.string.menu_logout) { _, _ ->
                appAuth.removeAuth()
                findNavController(R.id.nav_host_fragment).navigateUp()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .setCancelable(true)
            .show()
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        requestPermissions(arrayOf(permission), 1)
    }
}