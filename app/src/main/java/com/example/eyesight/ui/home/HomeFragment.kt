package com.example.eyesight.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.eyesight.ProductSelectionBottomSheetFragment
import com.example.eyesight.R
import com.example.eyesight.databinding.FragmentHomeBinding
import com.example.eyesight.ui.MainActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!  // Pastikan binding hanya diakses setelah _binding diinisialisasi

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val root: View = binding.root

        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)


        try {
            (activity as? MainActivity)?.setToolbarTitle("Vision")
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error setting toolbar title: ${e.message}")
        }

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        homeViewModel.hasProduct.observe(viewLifecycleOwner, Observer { hasProduct ->
            if (hasProduct) {
                binding.btnPower.visibility = View.VISIBLE
                binding.titleActivate.visibility = View.VISIBLE
                binding.modeOff.visibility = View.VISIBLE
                binding.textHome.visibility = View.GONE
                binding.buyProduct.visibility = View.GONE
                binding.btnSelectProduct.visibility = View.GONE
            } else {
                binding.btnPower.visibility = View.GONE
                binding.titleActivate.visibility = View.GONE
                binding.modeOff.visibility = View.GONE
                binding.textHome.visibility = View.VISIBLE  // Tampilkan pesan "Belum Memiliki Produk"
                binding.buyProduct.visibility = View.VISIBLE  // Tampilkan tombol "Beli Produk"
                binding.btnSelectProduct.visibility = View.VISIBLE
            }
        })

//        checkProductStatus()

        binding.btnSelectProduct.setOnClickListener {
            Toast.makeText(context, "Ditekan", Toast.LENGTH_SHORT).show()
            val bottomSheetFragment = ProductSelectionBottomSheetFragment()
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }

        return root
    }

//    private fun updateUIBasedOnProductStatus(hasProduct: Boolean) {
//        val hasProduct = sharedPreferences.getBoolean("HAVE_PRODUCT", false)
//
//        if (hasProduct) {
//            // Jika user sudah memiliki produk, tampilkan tombol power dan informasi mode
//            binding.btnPower.visibility = View.VISIBLE
//            binding.modeOff.visibility = View.VISIBLE
//            binding.titleActivate.visibility = View.VISIBLE
//            binding.textHome.visibility = View.GONE  // Sembunyikan pesan "Belum Memiliki Produk"
//            binding.buyProduct.visibility = View.GONE  // Sembunyikan tombol "Beli Produk"
//            binding.btnSelectProduct.visibility = View.GONE  // Sembunyikan tombol "Pilih Produk Series"
//        } else {
//            // Jika user belum memiliki produk, sembunyikan tombol power dan informasi mode
//            binding.btnPower.visibility = View.GONE
//            binding.modeOff.visibility = View.GONE
//            binding.titleActivate.visibility = View.GONE
//            binding.textHome.visibility = View.VISIBLE  // Tampilkan pesan "Belum Memiliki Produk"
//            binding.buyProduct.visibility = View.VISIBLE  // Tampilkan tombol "Beli Produk"
//            binding.btnSelectProduct.visibility = View.VISIBLE  // Tampilkan tombol "Pilih Produk Series"
//        }
//    }

    private fun saveProductStatus(hasProduct: Boolean) {
        homeViewModel.setProductStatus(hasProduct)
    }

    private fun openProductSelectionDialog() {
        val productSelectionBottomSheet = ProductSelectionBottomSheetFragment()
        productSelectionBottomSheet.show(parentFragmentManager, "ProductSelection")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Null-kan _binding saat view dihancurkan untuk mencegah memory leaks
        _binding = null
    }
}