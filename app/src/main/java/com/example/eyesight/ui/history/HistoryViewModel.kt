package com.example.eyesight.ui.history

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eyesight.data.response.HistoryResponseItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewModel : ViewModel() {

    private val _historyItems = MutableLiveData<List<HistoryResponseItem>>()
    val historyItems: LiveData<List<HistoryResponseItem>> get() = _historyItems

    fun loadHistoryData(context: Context) {
        // Misalnya, menggantikan loadMockData dengan pemanggilan API atau pengambilan dari sumber lain
        val data = loadMockData(context)
        _historyItems.value = data
    }

    private fun loadMockData(context: Context): List<HistoryResponseItem> {
        val companyName = "Eyesight"
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return try {
            val inputStream = context.assets.open("mock_data.json")
            val reader = InputStreamReader(inputStream)
            val gson = Gson()
            val historyResponse = object : TypeToken<List<HistoryResponseItem>>() {}.type
            val items: List<HistoryResponseItem> = gson.fromJson(reader, historyResponse)

            items.mapIndexed { index, item ->
                val generatedId = generateId(index + 1, companyName, currentDate)
                item.copy(id = generatedId)  // Gantilah ID yang lama dengan ID yang baru
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun generateId(index: Int, companyName: String, date: String): String {
        val companyPrefix = companyName.take(1).lowercase()
        val dateFormat = SimpleDateFormat("MMyy", Locale.getDefault())
        val formattedDate = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date))

        val formattedIndex = String.format("%03d", index)
        return "$companyPrefix$formattedDate$formattedIndex"
    }
}