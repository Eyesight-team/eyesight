package com.example.eyesight.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.eyesight.R
import com.example.eyesight.UserProfileActivity
import com.example.eyesight.databinding.ActivityMainBinding
import com.example.eyesight.helper.SessionManager
import com.example.eyesight.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sessionManager: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

//        val toolbar = binding.toolbar
//        setSupportActionBar(toolbar)

        // Pastikan tidak ada title default di action bar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

//        if (!sessionManager.isLoggedIn()) {
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//        }

        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        val fullName = sharedPreferences.getString("FIRST_NAME", "No Name") + sharedPreferences.getString("LAST_NAME", "No Last Name")
        val company = sharedPreferences.getString("COMPANY_NAME", "No company")

        binding.toolbarTitleName?.text = fullName
        binding.toolbarDescName?.text = company

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_history,
                R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)

        binding.toolbarProfile?.setOnClickListener {
            startActivity(Intent(this@MainActivity, UserProfileActivity::class.java))
        }


//        auth = Firebase.auth
//        val firebaseUser = auth.currentUser
//
//        if (firebaseUser == null) {
//            // Not signed in, launch the Login activity
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//            return
//        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when(item.itemId) {
//            R.id.action_profile -> {
//                showPopupMenu(findViewById(R.id.action_profile))
//                Toast.makeText(this, "Ditekan", Toast.LENGTH_SHORT).show()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

//    private fun showPopupMenu(view: View) {
//        val popup = PopupMenu(this, view)
//        popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)
//
//        popup.setOnMenuItemClickListener { menuItem ->
//            when(menuItem.itemId) {
//                R.id.profile -> {
//                    Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
//                    true
//                }
//                else -> false
//            }
//        }
//        popup.show()
//    }

    fun setToolbarTitle(title: String) {
        val toolbarTitle = binding.toolbarTitle
        toolbarTitle.text = title
    }

    private fun signOut() {

        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(this@MainActivity)
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }


    }

}
