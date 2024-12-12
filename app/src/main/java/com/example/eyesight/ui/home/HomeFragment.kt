package com.example.eyesight.ui.home

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.eyesight.R
import com.example.eyesight.adapter.LeDeviceListAdapter
import com.example.eyesight.ui.sheet.ProductSelectionBottomSheetFragment
import com.example.eyesight.databinding.FragmentHomeBinding
import com.example.eyesight.ui.MainActivity
import com.example.eyesight.ui.product.ProductFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.bluetooth.BluetoothGattCallback
import android.net.Uri


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!  // Pastikan binding hanya diakses setelah _binding diinisialisasi

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val REQUEST_ENABLE_BT = 1

    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 101

    private var bluetoothGatt: BluetoothGatt? = null

    private val enableBtLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Bluetooth has been enabled, proceed with scanning
                startScanning()
            } else {
                // Bluetooth was not enabled, show message
//                Toast.makeText(context, "Bluetooth tidak diaktifkan", Toast.LENGTH_SHORT).show()
            }

        }

    @RequiresApi(Build.VERSION_CODES.S)
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

        binding.btnSelectProduct.setOnClickListener {
            Toast.makeText(context, "Ditekan", Toast.LENGTH_SHORT).show()
            val bottomSheetFragment = ProductSelectionBottomSheetFragment()
            bottomSheetFragment.show(requireActivity().supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.readGuide.setOnClickListener {
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

    @RequiresApi(Build.VERSION_CODES.S)
    private fun initBluetooth() {

        // Memeriksa apakah perangkat memiliki izin yang diperlukan berdasarkan versi Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 dan lebih tinggi memerlukan izin Bluetooth
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Jika izin belum diberikan, meminta izin
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    BLUETOOTH_PERMISSION_REQUEST_CODE
                )
                return // Keluarkan dari fungsi jika izin belum diberikan
            }
        } else {
            // Untuk perangkat dengan versi Android lebih rendah dari Android 12
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                // Meminta izin lokasi untuk perangkat dengan Android versi lebih lama
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    BLUETOOTH_PERMISSION_REQUEST_CODE
                )
                return
            }
        }

        // Setelah izin diberikan, lanjutkan untuk menginisialisasi Bluetooth
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        // Memeriksa apakah Bluetooth diaktifkan atau tidak
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(enableBtIntent)
        } else {
            // Jika Bluetooth sudah diaktifkan, mulai pemindaian perangkat
            startScanning()
        }
    }


    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val device = result.device
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            Log.d("BLE", "Device found: ${device.name}")

            // Pastikan nama perangkat Jetson sesuai
            if (device.name == device.name) {
                connectToDevice(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLE", "Scan failed with error: $errorCode")
        }
    }

    private fun startScanning() {

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
//            Toast.makeText(context, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
            return
        }

        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBluetoothScanPermission()
                return
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestLocationPermission()
                return
            }
        }

        // Jika izin sudah diberikan, mulai pemindaian
        bluetoothLeScanner.startScan(leScanCallback)
    }

    private fun connectToDevice(device: BluetoothDevice) {
        Log.d("BLE", "Connecting to device: ${device.name}")
        val gatt = if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {

        }
        device.connectGatt(requireContext(), false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("GATT", "Connected to GATT server.")
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                gatt.discoverServices()  // Mulai discovery layanan setelah terhubung
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("GATT", "Disconnected from GATT server.")
                gatt.close() // Tutup koneksi saat terputus
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("GATT", "Services discovered successfully.")
            } else {
                Log.e("GATT", "Failed to discover services.")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanning()
                } else {
                }
            }

            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanning()
                } else {
                    showPermissionDeniedDialog()
                }
            }
        }

    }

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Izin Diperlukan")
        builder.setMessage("Aplikasi membutuhkan izin Bluetooth untuk dapat melanjutkan. Apakah Anda ingin membuka pengaturan aplikasi untuk memberikan izin?")
        builder.setPositiveButton("Ya") { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton("Tidak") { _, _ ->
            System.exit(0)
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun requestBluetoothScanPermission() {
        // Minta izin Bluetooth Scan
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.BLUETOOTH_SCAN),
            BLUETOOTH_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestLocationPermission() {
        // Minta izin Location untuk Android 11 atau lebih rendah
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt?.close()  // Menutup koneksi GATT saat aktivitas atau fragment dihancurkan
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

        companion object {
            private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
            private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 1002
        }
}