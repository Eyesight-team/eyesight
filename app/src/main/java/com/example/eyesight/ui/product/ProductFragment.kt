package com.example.eyesight.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.eyesight.databinding.FragmentProductBinding
import com.example.eyesight.ui.MainActivity

class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(ProductViewModel::class.java)

        _binding = FragmentProductBinding.inflate(inflater, container, false)
        val root: View = binding.root

            (activity as? MainActivity)?.setToolbarTitle("Produk")

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}