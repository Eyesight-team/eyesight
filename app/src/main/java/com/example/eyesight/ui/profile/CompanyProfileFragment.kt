package com.example.eyesight.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.eyesight.databinding.FragmentCompanyProfileBinding
import com.example.eyesight.ui.MainActivity
import com.example.eyesight.ui.notifications.HistoryViewModel


class CompanyProfileFragment : Fragment() {

    private var _binding: FragmentCompanyProfileBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(HistoryViewModel::class.java)

        _binding = FragmentCompanyProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textNotifications
//        notificationsViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        (activity as? MainActivity)?.setToolbarTitle("Eyesight")

        return root
    }

}