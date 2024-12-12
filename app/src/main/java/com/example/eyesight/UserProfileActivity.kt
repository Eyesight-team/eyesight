package com.example.eyesight

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eyesight.adapter.HistoryAdapter
import com.example.eyesight.databinding.ActivityFormBinding
import com.example.eyesight.databinding.ActivityUserProfileBinding
import com.example.eyesight.helper.SessionManager
import com.example.eyesight.ui.auth.LoginActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding

    private lateinit var historyAdapter: HistoryAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId != null) {
            val db = Firebase.firestore

            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firstName = document.getString("first_name")?.trim() ?: "User Not"
                        val lastName = document.getString("last_name")?.trim() ?: "Found"
                        val company = document.getString("company_name")?.trim() ?: "Perusahaan Tidak Ditemukan"
                        val jobdesc = document.getString("jobdesc")?.trim() ?: "Not Found"
                        val haveProduct = document.getBoolean("have_product")
                        val history = document.getBoolean("history")
                        val product = document.getString("product_name")?.trim() ?: "Not Found"

                        if (haveProduct == false) {
                            binding.productImg.visibility = View.INVISIBLE
                            binding.productImgNone.visibility = View.VISIBLE

                        } else {
                            binding.productImg.visibility = View.VISIBLE
                            binding.productImgNone.visibility = View.INVISIBLE

                        }

                        if (history == true) {
                            binding.rvReview.visibility = View.VISIBLE
                            binding.historyNotyet.visibility = View.GONE
                        } else {
                            binding.rvReview.visibility = View.INVISIBLE
                            binding.historyNotyet.visibility = View.VISIBLE
                        }

                        binding.tvName.text = "${firstName} ${lastName}"
                        binding.resultCompany.text = company
                        binding.titleProduct.text = product
                        binding.tvJobdesc.text = jobdesc
                    }

                }

        }

        val dummyData = listOf(
            HistoryItem("6000", "4000", "2000", "18-01-2004"),
            HistoryItem("6000", "5000", "1000", "19/01/2004"),
            HistoryItem("6000", "4500", "1500", "20/01/2004")
        )

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvReview.layoutManager = layoutManager

        historyAdapter = HistoryAdapter(dummyData) { total, lulus, gagal, date ->
        }

        binding.rvReview.adapter = historyAdapter

        binding.closeBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.leaveBtn.setOnClickListener {
            auth.signOut()
            clearSharedPreferences()
            SessionManager(this).clearSession()
            startActivity(Intent(this@UserProfileActivity, LoginActivity::class.java))
            finish()
        }

    }

    private fun clearSharedPreferences() {
        val sharedPreferences = getSharedPreferences("YourSharedPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

}