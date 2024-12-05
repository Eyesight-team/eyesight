package com.example.eyesight

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
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
import com.example.eyesight.ui.home.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProductSelectionBottomSheetFragment : DialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_selection_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupProduct)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        homeViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        btnConfirm.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val radioButton = view.findViewById<RadioButton>(selectedId)
                val selectedProduct = radioButton.text.toString()

                selectedProduct(selectedProduct)
                homeViewModel.setProductStatus(true)
                Toast.makeText(context, "Pilihan: $selectedProduct", Toast.LENGTH_SHORT).show()
                dismiss()
                showExitDialog()
            } else {
                Toast.makeText(context, "Silakan pilih produk", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showExitDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Perubahan Produk Tersimpan")
        builder.setMessage("Untuk melihat perubahan tampilan, anda harus keluar dari aplikasi.")

        builder.setPositiveButton("OK") { _, _ ->
            Toast.makeText(context, "Keluar aplikasi...", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun selectedProduct(selectedProduct: String) {
        val editor = sharedPreferences.edit()
        editor.putString("PRODUCT_NAME", selectedProduct)
//        homeViewModel.saveProductStatus(true)
        editor.apply()
    }
}
