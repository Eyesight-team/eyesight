package com.example.eyesight

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.eyesight.databinding.ActivityFormBinding
import com.example.eyesight.helper.SessionManager
import com.example.eyesight.ui.MainActivity
import com.example.eyesight.ui.auth.LoginActivity
import com.example.eyesight.ui.home.HomeViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firestore.v1.FirestoreGrpc.FirestoreImplBase
import java.io.FileInputStream
import java.util.Calendar

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()

        sessionManager = SessionManager(this)

        binding.backBtn.setOnClickListener {
                auth.signOut()
                startActivity(Intent(this@FormActivity, LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            val firstName = binding.edtFirstName.text.toString().trim()
            val lastName = binding.edtLastName.text.toString().trim()
            val birthDate = binding.edtBirthOfDate.text.toString().trim()
            val phoneNumber = binding.edtPhoneNumber.text.toString().trim()
            val companyName = binding.edtCompany.text.toString().trim()
            val companyAddress = binding.edtCompanyAddress.text.toString().trim()
            val jobDesc = binding.edtJobdesc.text.toString().trim()

            val haveProduct = when (binding.radioGroup.checkedRadioButtonId) {
                R.id.radioButtonOptionAlready -> true
                R.id.radioButtonOptionNotYet -> false
                else -> null
            }

            if (haveProduct == null) {
                Toast.makeText(this, "Please select if you have a product or not!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val optionProduct = if (haveProduct) {
                when (binding.radioGroup2.checkedRadioButtonId) {
                    R.id.product_option_1 -> "Option A"
                    R.id.product_option_2 -> "Option B"
                    R.id.product_option_3 -> "Option C"
                    else -> "Produk tidak ditemukan"
                }
            } else {
                "Belum memiliki Produk"
            }

            if (validateForm(
                    firstName,
                    lastName,
                    birthDate,
                    phoneNumber,
                    companyName,
                    companyAddress,
                    jobDesc,
                    haveProduct,
                    optionProduct
                )
            ) {
                // Simpan data ke SharedPreferences
                saveUserData(
                    firstName,
                    lastName,
                    birthDate,
                    phoneNumber,
                    companyName,
                    companyAddress,
                    jobDesc,
                    haveProduct,
                    optionProduct
                )

                Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this@FormActivity, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.edtBirthOfDate.setOnClickListener{
            showDatePickerDialog()
        }


        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.radioButtonOptionAlready -> {
                    val colorStateList = ContextCompat.getColorStateList(this, R.color.secondary)
                    binding.radioButtonOptionAlready.buttonTintList = colorStateList
                    binding.radioGroup2.visibility = View.VISIBLE
                    binding.titleOptionProduct.visibility = View.VISIBLE

//                    sharedPreferences.edit().apply{
//                        putBoolean("HAVE_PRODUCT", true)
//                        putString("PRODUCT", "Already have product")
//                        apply()
//                    }
                }
                R.id.radioButtonOptionNotYet -> {
                    val colorStateList = ContextCompat.getColorStateList(this, R.color.secondary)
                    binding.radioButtonOptionNotYet.buttonTintList = colorStateList
                    binding.radioGroup2.visibility = View.GONE
                    binding.titleOptionProduct.visibility = View.GONE

//                    sharedPreferences.edit().apply{
//                        putBoolean("HAVE_PRODUCT", false)
//                        putString("PRODUCT", "Do not have the product")
//                        apply()
//                    }
                }
            }
        }

    }

    private fun validateForm(
        firstName: String,
        lastName: String,
        birthDate: String,
        phoneNumber: String,
        companyName: String,
        companyAddress: String,
        jobDesc: String,
        haveProduct: Boolean,
        optionProduct: String
    ): Boolean {
        return firstName.isNotEmpty() &&
                lastName.isNotEmpty() &&
                birthDate.isNotEmpty() &&
                phoneNumber.isNotEmpty() &&
                companyName.isNotEmpty() &&
                companyAddress.isNotEmpty() &&
                jobDesc.isNotEmpty() &&

                (haveProduct || !haveProduct) &&
                // Validasi untuk pilihan produk
                (haveProduct && optionProduct != "Produk tidak ditemukan") || // Jika sudah punya produk, pilih produk
                (!haveProduct && optionProduct == "Belum memiliki Produk")
    }

    // Fungsi untuk menyimpan data pengguna ke SharedPreferences
    private fun saveUserData(
        firstName: String,
        lastName: String,
        birthDate: String,
        phoneNumber: String,
        companyName: String,
        companyAddress: String,
        jobDesc: String,
        haveProduct: Boolean,
        productOption: String
    ) {
        val db = Firebase.firestore

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId != null) {

            val userData = hashMapOf(
                "first_name" to firstName,
                "last_name" to lastName,
                "born" to birthDate,
                "phone_number" to phoneNumber,
                "company_name" to companyName,
                "company_address" to companyAddress,
                "jobdesc" to jobDesc,
                "have_product" to haveProduct,
                "product_name" to productOption
            )
            db.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener {
                    Log.d(TAG, "User Data successfully saved with Id : $userId")
                }

                .addOnFailureListener { e ->
                    Log.w(TAG, "Error saving user data", e)
                }
        } else {
            Log.w(TAG, "No user is currently logged in")
        }

    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"

                binding.edtBirthOfDate.setText(selectedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    companion object {
        private const val TAG = "FormActivity"
    }


    }
