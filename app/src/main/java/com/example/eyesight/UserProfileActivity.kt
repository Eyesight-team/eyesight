package com.example.eyesight

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

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding

    private lateinit var historyAdapter: HistoryAdapter

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()

        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        val firstName = sharedPreferences.getString("FIRST_NAME", "No Name").toString().trim()
        val lastName = sharedPreferences.getString("LAST_NAME", "No Last Name").toString().trim()
        val companyName = sharedPreferences.getString("COMPANY_NAME", "No Company Name")
        val product = sharedPreferences.getString("PRODUCT_NAME", "Produk tidak ditemukan")
        val jobdesc = sharedPreferences.getString("JOBDESC", "None")

        if (product == null) {
            binding.productImg.visibility = View.INVISIBLE
            binding.productImgNone.visibility = View.VISIBLE
        } else {
            binding.productImg.visibility = View.VISIBLE
            binding.productImgNone.visibility = View.INVISIBLE
        }

        binding.tvName.text = "${firstName} ${lastName}"
        binding.resultCompany.text = companyName
        binding.titleProduct.text = product
        binding.tvJobdesc.text = jobdesc


        val dummyData = listOf(
            HistoryItem("6000", "4000", "2000", "18-01-2004"),
            HistoryItem("6000", "5000", "1000", "19/01/2004"),
            HistoryItem("6000", "4500", "1500", "20/01/2004")
        )

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvReview.layoutManager = layoutManager

        historyAdapter = HistoryAdapter(dummyData) { total, lulus, gagal, date ->
            showToast("Total: ${total}, Lulus: ${lulus}, Gagal: ${gagal}, Date: ${date}")
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

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}