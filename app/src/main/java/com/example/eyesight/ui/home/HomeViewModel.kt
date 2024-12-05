package com.example.eyesight.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _hasProduct = MutableLiveData<Boolean>()
    val hasProduct: LiveData<Boolean> get() = _hasProduct

    init {
        val sharedPreferences = application.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val hasProduct = sharedPreferences.getBoolean("HAVE_PRODUCT", false)
        _hasProduct.value = hasProduct // Update LiveData
    }

    fun setProductStatus(hasProduct: Boolean) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("HAVE_PRODUCT", hasProduct)
            apply()
        }
        _hasProduct.value = hasProduct
    }

    private fun loadProductStatus() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val productStatus = sharedPreferences.getBoolean("HAVE_PRODUCT", false)
        _hasProduct.value = productStatus
    }
}
