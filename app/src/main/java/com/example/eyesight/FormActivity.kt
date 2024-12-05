package com.example.eyesight

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Calendar

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sessionManager: SessionManager

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()

        sessionManager = SessionManager(this)

        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

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

            // Ambil status apakah pengguna memiliki produk atau tidak
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

                // Tampilkan Toast
                Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show()

                // Navigasi ke MainActivity
                startActivity(Intent(this@FormActivity, MainActivity::class.java))
                finish()  // Menutup FormActivity jika diperlukan
            } else {
                // Jika ada input yang kosong
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

                    sharedPreferences.edit().apply{
                        putBoolean("HAVE_PRODUCT", true)
                        putString("PRODUCT", "Already have product")
                        apply()
                    }
                }
                R.id.radioButtonOptionNotYet -> {
                    val colorStateList = ContextCompat.getColorStateList(this, R.color.secondary)
                    binding.radioButtonOptionNotYet.buttonTintList = colorStateList
                    binding.radioGroup2.visibility = View.GONE
                    binding.titleOptionProduct.visibility = View.GONE

                    sharedPreferences.edit().apply{
                        putBoolean("HAVE_PRODUCT", false)
                        putString("PRODUCT", "Do not have the product")
                        apply()
                    }
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
        val editor = sharedPreferences.edit()
        editor.putString("FIRST_NAME", firstName)
        editor.putString("LAST_NAME", lastName)
        editor.putString("BIRTH_DATE", birthDate)
        editor.putString("PHONE_NUMBER", phoneNumber)
        editor.putString("COMPANY_NAME", companyName)
        editor.putString("COMPANY_ADDRESS", companyAddress)
        editor.putString("JOBDESC", jobDesc)
        editor.putBoolean("HAVE_PRODUCT", haveProduct)
        editor.putString("PRODUCT_NAME", productOption)
        editor.apply()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Menyusun tanggal yang dipilih ke format yang lebih user-friendly
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"

                // Menampilkan tanggal yang dipilih di EditText
                binding.edtBirthOfDate.setText(selectedDate)
            }, year, month, day)

        datePickerDialog.show()
    }
    }
