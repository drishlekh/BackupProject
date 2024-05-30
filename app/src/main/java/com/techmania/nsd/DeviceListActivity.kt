package com.techmania.nsd

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class DeviceListActivity : AppCompatActivity() {

    private lateinit var discoveredDevicesRecyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var stopDiscovery: Button
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 5000L

    private lateinit var reloadDiscovery: Button
    private lateinit var nsdManager: NsdManager
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private val discoveredServices = mutableSetOf<NsdServiceInfo>()
    private val serviceType = "_myapp_service._tcp."


    private lateinit var deviceCircleView: DeviceCircleView


    private val selectedDeviceAddresses = mutableListOf<String>()



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)


        discoveredDevicesRecyclerView = findViewById(R.id.discoveredDevicesRecyclerView)
        deviceAdapter = DeviceAdapter(mutableListOf())
        discoveredDevicesRecyclerView.adapter = deviceAdapter
        discoveredDevicesRecyclerView.layoutManager = LinearLayoutManager(this)

        deviceCircleView = findViewById(R.id.deviceCircleView)

        stopDiscovery = findViewById(R.id.stopDiscovery)

        deviceCircleView = findViewById(R.id.deviceCircleView)
        deviceCircleView.setDevices(listOf("Device 1", "Device 2", "Device 3"))


        reloadDiscovery = findViewById(R.id.reloadDiscovery)
        val deviceNames = intent.getStringArrayListExtra("deviceNames") ?: mutableListOf()
        deviceAdapter.updateDevices(deviceNames)



        nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager

        stopDiscovery.setOnClickListener {
            stopDiscovery(true)
        }


        reloadDiscovery.setOnClickListener {
            restartDiscovery()
        }



        startDiscovery()
        startPeriodicUpdates()
    }

    override fun onResume() {
        super.onResume()
        startPeriodicUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopPeriodicUpdates()
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateDeviceList()
            handler.postDelayed(this, updateInterval)
        }
    }

    private fun startPeriodicUpdates() {
        handler.post(updateRunnable)
    }

    private fun stopPeriodicUpdates() {
        handler.removeCallbacks(updateRunnable)
    }

    private fun updateDeviceList() {

        val deviceNames = discoveredServices.map { it.serviceName }.toList()
        deviceAdapter.updateDevices(deviceNames)
        deviceCircleView.setDevices(deviceNames)


    }


    @SuppressLint("ServiceCast")
    private fun stopDiscovery(finishActivity: Boolean) {

        discoveryListener?.let {
            nsdManager.stopServiceDiscovery(it)
        }
        discoveryListener = null
        if (finishActivity) {
            finish()
        }
    }

    private fun restartDiscovery() {

        stopDiscovery(false)
        discoveredServices.clear()
        updateDeviceList()
        startDiscovery()
    }
    private fun startDiscovery() {
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d("NSD", "Service discovery started")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d("NSD", "Service found: ${serviceInfo.serviceName}")
                handler.post {
                    if (discoveredServices.add(serviceInfo)) {
                        updateDeviceList()
                    }
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d("NSD", "Service lost: ${serviceInfo.serviceName}")
                handler.post {
                    if (discoveredServices.remove(serviceInfo)) {
                        updateDeviceList()
                    }
                }
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i("NSD", "Discovery stopped: $serviceType")
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




}




