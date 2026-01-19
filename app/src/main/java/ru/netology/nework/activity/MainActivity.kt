package ru.netology.nework.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
    private var currentDestinationId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
            currentDestinationId = destination.id

            val isMainScreen = destination.id == R.id.postsFragment ||
                    destination.id == R.id.eventsFragment ||
                    destination.id == R.id.usersFragment
            bottomNav.isVisible = isMainScreen

            supportActionBar?.title = when (destination.id) {
                R.id.postsFragment -> getString(R.string.posts)
                R.id.eventsFragment -> getString(R.string.events)
                R.id.usersFragment -> getString(R.string.users)
                R.id.newPostFragment -> getString(R.string.new_post)
                R.id.newEventFragment -> getString(R.string.new_event)
                R.id.signInFragment -> getString(R.string.login)
                R.id.signUpFragment -> getString(R.string.registration)
                R.id.mapFragment -> getString(R.string.map)
                R.id.mentionsFragment -> getString(R.string.users)
                R.id.singlePostFragment -> getString(R.string.post)
                R.id.singleEventFragment -> getString(R.string.event)


                else -> getString(R.string.app_name)
            }
            invalidateOptionsMenu()
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

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)

        val isMainScreen = currentDestinationId == R.id.postsFragment ||
                currentDestinationId == R.id.eventsFragment ||
                currentDestinationId == R.id.usersFragment

        menu.setGroupVisible(R.id.authenticated, isMainScreen && viewModel.isAuthorized)
        menu.setGroupVisible(R.id.unauthenticated, isMainScreen && !viewModel.isAuthorized)

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        postViewModel.saveBeforeBack.value = !(postViewModel.saveBeforeBack.value ?: false)
        return navController.navigateUp() || super.onSupportNavigateUp()
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