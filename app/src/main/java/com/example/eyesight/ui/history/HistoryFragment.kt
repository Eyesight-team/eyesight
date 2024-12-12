package com.example.eyesight.ui.history

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eyesight.R
import com.example.eyesight.adapter.HistoryMainAdapter
import com.example.eyesight.data.response.HistoryResponse
import com.example.eyesight.data.response.HistoryResponseItem
import com.example.eyesight.databinding.FragmentHistoryBinding
import com.example.eyesight.ui.MainActivity
import com.example.eyesight.ui.home.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null

    private val binding get() = _binding!!

    private lateinit var historyViewModel: HistoryViewModel

    private lateinit var items: List<HistoryResponseItem>


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val notificationsViewModel =
//            ViewModelProvider(this).get(HistoryViewModel::class.java)

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        historyViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        (activity as? MainActivity)?.setToolbarTitle("Riwayat")

//        binding.btnPlayNow.setOnClickListener {
//            val fragmentHome = HomeFragment()
//            val transaction = requireActivity().supportFragmentManager.beginTransaction()
//            transaction.replace(R.id.fragment_container_home, fragmentHome) // pastikan `fragment_container` adalah ID dari container fragment di layout
//            transaction.addToBackStack(null)
//
//            transaction.commit()
//        }

        val db = Firebase.firestore

        val userId = FirebaseAuth.getInstance().uid
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val history = document.getBoolean("history")

                        if (!history!!) {

                            binding.rvReview.visibility = View.INVISIBLE
                            binding.btnPlayNow.visibility = View.VISIBLE
                            binding.notyet.visibility = View.VISIBLE

                        } else {

                            binding.rvReview.visibility = View.VISIBLE
                            binding.btnPlayNow.visibility = View.INVISIBLE
                            binding.notyet.visibility = View.INVISIBLE

                            loadData()

                            resultTotalData()
                        }

                }
        }

            return root
    }

    private fun loadData() {
        val recyclerView: RecyclerView = binding.rvReview
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = HistoryMainAdapter(emptyList(), onItemClicked = { id, feasibilityTest, desc, confidence, date ->
            showToast("ID: ${id} FeasibilityTest: ${feasibilityTest} DescFt: ${desc} Confidence: ${confidence} Date: $date")
        })

        recyclerView.adapter =  adapter

        binding.resultTotalMain.text
        historyViewModel.historyItems.observe(viewLifecycleOwner, Observer { historyItems ->

            adapter.updateData(historyItems)

        })
        historyViewModel.loadHistoryData(requireContext())
    }

    private fun resultTotalData() {
        val jsonString = loadJsonFromAssets("mock_data.json")
        val type = object : TypeToken<List<HistoryResponseItem>>() {}.type
        items = Gson().fromJson(jsonString, type)
        binding.resultTotalMain.text = items.size.toString()

        val lulus = items.filter {
            val feasibilityTest = it.feasibilityTest?.toIntOrNull()

            feasibilityTest != null && feasibilityTest >= 50
        }.size

        val gagal = items.filter {
            val feasibilityTest = it.feasibilityTest?.toIntOrNull()

            feasibilityTest != null && feasibilityTest < 50
        }.size

        binding.resultFailed.text = gagal.toString()
        binding.resultLulus.text = lulus.toString()
    }

    private fun loadJsonFromAssets(fileName: String): String {
        val inputStream = requireContext().assets.open(fileName)
        val reader = InputStreamReader(inputStream)
        return reader.readText()
    }

    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}