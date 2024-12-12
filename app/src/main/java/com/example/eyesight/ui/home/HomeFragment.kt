package com.example.eyesight.ui.home

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.eyesight.R
import com.example.eyesight.ui.sheet.ProductSelectionBottomSheetFragment
import com.example.eyesight.databinding.FragmentHomeBinding
import com.example.eyesight.ui.MainActivity
import com.example.eyesight.ui.product.ProductFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!  // Pastikan binding hanya diakses setelah _binding diinisialisasi

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val REQUEST_ENABLE_BT = 1
    private var scanning = false

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 101

    private val enableBtLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                startScanning()
            } else {
                Toast.makeText(context, "Bluetooth tidak diaktifkan", Toast.LENGTH_SHORT).show()
            }
        }

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

        val user = FirebaseAuth.getInstance()
        val userId = user.uid
        val db = Firebase.firestore

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val hasProduct = document.getBoolean("have_product")
                    if (hasProduct == true) {
                        binding.btnPower.visibility = View.VISIBLE
                        binding.titleActivate.visibility = View.VISIBLE
                        binding.mode.visibility = View.VISIBLE
                        binding.textHome.visibility = View.GONE
                        binding.btnBuyProduct.visibility = View.GONE
                        binding.btnSelectProduct.visibility = View.GONE
                    } else {
                        binding.btnPower.visibility = View.GONE
                        binding.titleActivate.visibility = View.GONE
                        binding.mode.visibility = View.GONE
                        binding.textHome.visibility = View.VISIBLE
                        binding.btnBuyProduct.visibility = View.VISIBLE
                        binding.btnSelectProduct.visibility = View.VISIBLE
                    }
                }

            var isOn = false

            binding.btnPower.setOnClickListener {
                if (isOn) {
                    binding.mode.text = "Mode : OFF"
                    binding.btnPower.setBackgroundResource(R.drawable.power_off)
                    binding.imgViewPower.setImageResource(R.drawable.ic_power)
                } else {
                    // Tombol dalam keadaan OFF, ubah ke ON
                    binding.mode.text = "Mode : ON"
                    binding.btnPower.setBackgroundResource(R.drawable.power_on)
                    binding.imgViewPower.setImageResource(R.drawable.ic_power_white)
                }

                isOn = !isOn
            }

        }

//        checkProductStatus()

        binding.btnSelectProduct.setOnClickListener {
            Toast.makeText(context, "Ditekan", Toast.LENGTH_SHORT).show()
            val bottomSheetFragment = ProductSelectionBottomSheetFragment()
            bottomSheetFragment.show(requireActivity().supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.readGuide.setOnClickListener {
            showToast("Maaf, panduan belum ada !")
        }

        binding.btnBuyProduct.setOnClickListener {
            findNavController().navigate(
                R.id.navigation_profile,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.navigation_home, false) // Agar HomeFragment tidak dihapus dari back stack
                    .build()
            )
        }

        initBluetooth()

        return root
    }

    private fun initBluetooth() {
        Toast.makeText(context, "Init Bluetooth..", Toast.LENGTH_SHORT).show()

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Jika izin belum diberikan, minta izin
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Memastikan Bluetooth tersedia
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
            return
        }

        // Memeriksa apakah Bluetooth sudah diaktifkan
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(enableBtIntent)
//            startActivityForResult
        } else {
            startScanning()
        }
    }

    private fun startScanning() {
        showToast("Start Scanning...")
        if (scanning) return  // Jangan mulai pemindaian jika sudah berjalan

        // Periksa izin lokasi
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Jika izin belum diberikan, minta izin
            showToast("Memulai perizinan...")
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        bluetoothLeScanner.startScan(scanCallback)
        scanning = true
        Toast.makeText(context, "Memulai pemindaian BLE...", Toast.LENGTH_SHORT).show()
    }


    private fun stopScanning() {
        if (!scanning) return

        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothLeScanner.stopScan(scanCallback)
        scanning = false
        Toast.makeText(context, "Pemindaian BLE dihentikan", Toast.LENGTH_SHORT).show()
    }

    // Callback untuk menangani perangkat yang ditemukan selama pemindaian
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let { scanResult ->
                val device: BluetoothDevice = scanResult.device

                val deviceName = if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED) {
                    Log.d("BLE_NAME", "Nama Perangkat: ${device.name}")
                    showToast("BLE NAME: ${device.name}")
                    device.name ?: "Perangkat Tidak Dikenal" // Nama perangkat jika diizinkan
                } else {
                    // Jika izin belum diberikan
                    Log.w("BLE", "Izin Bluetooth CONNECT belum diberikan, tidak dapat membaca nama perangkat")
                    return
                }

                val deviceAddress = device.address
                Log.d("BLE", "Perangkat ditemukan: $deviceName, Alamat: $deviceAddress")

                // Misalnya, coba sambungkan ke perangkat ini
                // Kamu bisa menyaring perangkat berdasarkan nama atau alamat jika diperlukan


                if (deviceName == "MYBOOKHYPE") {
                    connectToDevice(device)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            showToast("on Scan Failed: $errorCode")
            super.onScanFailed(errorCode)
            Log.e("BLE", "Pemindaian gagal dengan kode error: $errorCode")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, bisa lanjut menggunakan Bluetooth
                startScanning()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                ) {
                    // Izin Bluetooth ditolak, beri penjelasan
                    showToast("Izin Bluetooth diperlukan untuk memindai perangkat")
                } else {
                    // Izin ditolak secara permanen, arahkan pengguna ke pengaturan
                    showBluetoothPermissionDeniedDialog()
                }
            }
        }
    }

    private fun showBluetoothPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Peringatan")
        builder.setMessage("Perangkat menggunakan akses Bluetooth. Jika Anda menolak izin, aplikasi akan keluar. Apakah Anda ingin melanjutkan ke pengaturan Bluetooth?")
        builder.setPositiveButton("Ya") { _, _ ->
            // Arahkan pengguna ke pengaturan Bluetooth
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        }
        builder.setNegativeButton("Tidak") { _, _ ->
            activity?.finish()
        }
        builder.setCancelable(false)  // Mencegah pengguna menutup dialog dengan menekan di luar
        builder.show()
    }

    private fun connectToDevice(device: BluetoothDevice) {
        // Lakukan koneksi dengan perangkat BLE yang ditemukan
        // Kode untuk menghubungkan ke perangkat dapat ditambahkan di sini
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        showToast("Connect to Device: ${device.name}")
        Toast.makeText(context, "Mencoba untuk menghubungkan ke perangkat: ${device.name}", Toast.LENGTH_SHORT).show()
        Log.d("BLE", "Mencoba menghubungkan ke perangkat: ${device.name}")
    }

    private fun checkBluetoothPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Jika izin belum diberikan, minta izin
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
        } else {
            // Izin sudah diberikan, bisa akses Bluetooth
            startScanning()
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Null-kan _binding saat view dihancurkan untuk mencegah memory leaks
        _binding = null
    }
}