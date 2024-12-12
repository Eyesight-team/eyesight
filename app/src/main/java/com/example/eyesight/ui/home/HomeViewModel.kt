package com.example.eyesight.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _hasProduct = MutableLiveData<Boolean?>()
    val hasProduct: LiveData<Boolean?> get() = _hasProduct

    private val user = FirebaseAuth.getInstance()
    private val userId = user.uid
    private val db = Firebase.firestore

    init {
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val haveProduct = document.getBoolean("have_product")
                        _hasProduct.value = haveProduct
                }
        }
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
