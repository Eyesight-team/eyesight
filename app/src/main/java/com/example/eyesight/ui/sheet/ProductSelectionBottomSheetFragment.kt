package com.example.eyesight.ui.sheet

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.eyesight.R
import com.example.eyesight.SplashScreen
import com.example.eyesight.ui.home.HomeViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class ProductSelectionBottomSheetFragment : DialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_selection_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupProduct)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)


        btnConfirm.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val radioButton = view.findViewById<RadioButton>(selectedId)
                val selectedProduct = radioButton.text.toString()

                selectedProduct(selectedProduct, true)
                Toast.makeText(context, "Pilihan: $selectedProduct", Toast.LENGTH_SHORT).show()
                if (isAdded && isResumed) {
                    showExitDialog()
                } else {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                    Log.e("ProductSelection", "Fragment belum aktif atau terpasang.")
                }
            } else {
                Toast.makeText(context, "Silakan pilih produk", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showExitDialog() {

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Perubahan Produk Tersimpan")
        builder.setMessage("Untuk melihat perubahan tampilan, anda harus keluar dari aplikasi.")

        builder.create()

            val intent = Intent(requireContext(), SplashScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)

    }

    private fun selectedProduct(selectedProduct: String, haveProduct: Any) {
        val user = FirebaseAuth.getInstance()
        val userId = user.uid
        val db = Firebase.firestore

        if (userId != null) {

            val updateData = hashMapOf(
                "product_name" to selectedProduct,
                "have_product" to haveProduct
            )

            db.collection("users")
                .document(userId)
                .update(updateData)
                .addOnSuccessListener { data ->
                    Log.w("ProductSelectionSheet", "Data Product successfully changed $data")
                }
                .addOnFailureListener { e ->
                    Log.w("ProductSelectionSheet", "Error saving user data", e)
                }
        }
    }
}
