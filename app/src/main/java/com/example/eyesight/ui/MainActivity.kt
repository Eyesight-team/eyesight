package com.example.eyesight.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId != null) {
            val db = Firebase.firestore
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                            val firstName = document.getString("first_name")?.trim() ?: "User Not"
                            val lastName = document.getString("last_name")?.trim() ?: "Found"
                            val company = document.getString("company_name")?.trim() ?: "Perusahaan Tidak Ditemukan"

                            binding.toolbarTitleName.text = "$firstName $lastName"
                            binding.toolbarDescName.text = company

//                        } else {
//                            showToast("No user data found")
//                            Log.d(TAG, "No user data found")
//                        }

                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error getting document ${e.message}")
                }
        } else {
            Log.w(TAG, "User ID is null")
        }

//        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_product,
                R.id.navigation_history,
                R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)

        binding.toolbarProfile.setOnClickListener {
            startActivity(Intent(this@MainActivity, UserProfileActivity::class.java))
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

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

    companion object {
        private const val TAG = "MainActivity"
    }

}
