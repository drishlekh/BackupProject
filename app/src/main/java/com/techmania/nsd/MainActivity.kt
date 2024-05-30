package com.techmania.nsd

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.UUID

class MainActivity : AppCompatActivity() {

    companion object {
        var discoveryListener: NsdManager.DiscoveryListener? = null
    }

    private lateinit var nsdManager: NsdManager
    private val serviceType = "_myapp_service._tcp."
    private var discoveredServices = mutableSetOf<NsdServiceInfo>()
    private lateinit var discoveredDevicesRecyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var availableDevicesTextView: TextView
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiToggleButton: Button


    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var registrationListener: NsdManager.RegistrationListener? = null


    private val discoveredIPAddress = ArrayList<String>()




    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager


        deviceAdapter = DeviceAdapter(mutableListOf())


        val discoverButton = findViewById<Button>(R.id.discoverButton)
        val advertiseButton = findViewById<Button>(R.id.advertiseButton)
        //availableDevicesTextView = findViewById(R.id.availableDevicesTextView)

        wifiToggleButton = findViewById(R.id.wifiToggleButton)



        discoverButton.setOnClickListener {
            Toast.makeText(this,"Discovering nearby devices...",Toast.LENGTH_LONG).show()
            discoverServices()
        }
        advertiseButton.setOnClickListener {
            Toast.makeText(this,"advertising your device...",Toast.LENGTH_LONG).show()
            showAdvertiseDialog()
            advertiseService()
        }

        wifiToggleButton.setOnClickListener {
            openWifiSettings()
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun openWifiSettings() {
        val intent = Intent(Settings.Panel.ACTION_WIFI)
        startActivity(intent)
        Toast.makeText(this, "Opening Wi-Fi Settings", Toast.LENGTH_SHORT).show()
    }






    private fun discoverServices() {
        discoveryListener?.let {
            nsdManager.stopServiceDiscovery(it)
        }
        discoveredServices.clear()
        updateDiscoveredDevicesUI()
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d("NSD", "Service discovery started````````````````````````````")
            }

            @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d("NSD", "Service found: ${serviceInfo.serviceName}") //-  ${serviceInfo.getHostAddresses()}")



            //                mainHandler.post {
//                    if (discoveredServices.add(serviceInfo)) {
//                        val deviceNames = discoveredServices.map { it.serviceName }.toList()
//                        val intent = Intent(this@MainActivity, DeviceListActivity::class.java)
//                        intent.putStringArrayListExtra("deviceNames", ArrayList(deviceNames))
//                        startActivity(intent)
//                        updateDiscoveredDevicesUI()
//
//                    }
//                }

                nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.e("NSD", "Resolve failed: $errorCode")
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        val host = serviceInfo.host
                        val port = serviceInfo.port
                        val address = host.hostAddress
                        Log.d(
                            "NSD",
                            "Resolved service: ${serviceInfo.serviceName} \n - IP Address: $address \n - Host: $host \n - Port: $port"
                        )
                        if (address != null) {
                            discoveredIPAddress.add(address)
                        }
                        Log.d("NSD", "Discovered IP Addresses: $discoveredIPAddress ")
                        mainHandler.post {
                            if (discoveredServices.add(serviceInfo)) {
                                val deviceNames = discoveredServices.map { it.serviceName }.toList()
                                val intent =
                                    Intent(this@MainActivity, DeviceListActivity::class.java)
                                intent.putStringArrayListExtra(
                                    "deviceNames",
                                    ArrayList(deviceNames)
                                )
                                startActivity(intent)
                                updateDiscoveredDevicesUI()
                            }
                        }
                    }
                })


            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d("NSD", "Service lost: ${serviceInfo.serviceName}")
                mainHandler.post {
                    discoveredServices.remove(serviceInfo)
                    updateDiscoveredDevicesUI()
                }
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i("NSD", "Discovery stopped-----------------------------: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("NSD", "Discovery failed: $errorCode")
                nsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("NSD", "Stop discovery failed: $errorCode")
                nsdManager.stopServiceDiscovery(this)
            }
        }

        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

    }


    @SuppressLint("SetTextI18n")
    private fun updateDiscoveredDevicesUI() {
        val deviceNames = discoveredServices.map { it.serviceName }
        deviceAdapter.updateDevices(deviceNames)
    }







    private fun advertiseService() {
        val port = 8080
        val deviceName = Build.BRAND + "_" + Build.MODEL + "_"
        val uniqueServiceName = "$deviceName${UUID.randomUUID().toString().substring(0, 5)}"

        val nsdServiceInfo = NsdServiceInfo().apply {
            serviceName = uniqueServiceName  //"MyAndroidDevice"
            serviceType = "_myapp_service._tcp."
            setPort(port)
        }
        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                Log.d("NSD", "Service registered: ${serviceInfo.serviceName} ")
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e("NSD", "Registration failed: $errorCode")
            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                Log.d("NSD", "Service unregistered: ${serviceInfo.serviceName}")
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e("NSD", "Unregistration failed: $errorCode")
            }
        }

        nsdManager.registerService(nsdServiceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    private fun showAdvertiseDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to stop advertising?")
            .setPositiveButton("Stop") { _, _ ->
                stopAdvertisingService()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun stopAdvertisingService() {
        registrationListener?.let {
            nsdManager.unregisterService(it)
            registrationListener = null
            Toast.makeText(this, "Advertising stopped", Toast.LENGTH_SHORT).show()
        }
    }





    override fun onPause() {
        super.onPause()
        discoveryListener?.let {
            nsdManager.stopServiceDiscovery(it)
        }
        registrationListener?.let {
            nsdManager.unregisterService(it)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        discoveryListener?.let {
            nsdManager.stopServiceDiscovery(it)
        }
        registrationListener?.let {
            nsdManager.unregisterService(it)
        }

    }
}







