package com.example.eyesight.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.eyesight.FormActivity
import com.example.eyesight.R
import com.example.eyesight.databinding.ActivityLoginBinding
import com.example.eyesight.ui.MainActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class  LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val text = getString(R.string.welcome_desc)

        val start = text.indexOf("Eyesight")
        val end = start + "Eyesight".length

        val spannableString = SpannableString(text)
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvDescWelcome.text = spannableString

        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        } else {
            binding.btnGoogle.setOnClickListener {
                binding.progressBar.visibility = View.VISIBLE
                signInWithGoogle()
            }
            binding.btnFb.setOnClickListener {
                showToast("Maaf, Login fb masih dalam proses... silahkan menggunakan akun google terlebih dahulu !")
            }
        }
    }

    private fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(this)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Filter akun yang telah diotorisasi
            .setServerClientId(getString(R.string.your_web_client_id)) // Web Client ID Firebase
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val response: GetCredentialResponse = credentialManager.getCredential(
                    context = this@LoginActivity,
                    request = request
                )
                handleSignIn(response)
            } catch (e: GetCredentialException) {
                binding.progressBar.visibility = View.GONE
                Log.e("Error", "GetCredentialException: ${e.message}")
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                } else {
                    Log.e("Error", "Unexpected credential type")
                }
            }
            else -> Log.e("Error", "Unsupported credential type")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    binding.progressBar.visibility = View.GONE
                    val user = FirebaseAuth.getInstance().currentUser
                    updateUI(user)
                } else {
                    Log.e("Error", "Firebase sign-in failed: ${task.exception}")
                    updateUI(null)
                }
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {

        if (currentUser != null) {

            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        // Jika dokumen pengguna ada di Firestore, arahkan ke MainActivity
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    } else {
                        // Jika dokumen pengguna tidak ada, arahkan ke FormActivity untuk mengisi data
                        startActivity(Intent(this@LoginActivity, FormActivity::class.java))
                    }
                    finish()
                }
                .addOnFailureListener { e ->
                    finish()
                }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        updateUI(currentUser)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        binding.progressBar.visibility = View.GONE
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001 // This is a common value for the sign-in request code\
    }

}