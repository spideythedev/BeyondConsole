package com.spideythedev.beyondconsole

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val preferences by lazy {
        getSharedPreferences("beyondconsole", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nameInput = findViewById<EditText>(R.id.consoleName)
        val addressInput = findViewById<EditText>(R.id.consoleAddress)
        val saveButton = findViewById<Button>(R.id.saveConsoleButton)
        val profileStatus = findViewById<TextView>(R.id.consoleProfileStatus)

        nameInput.setText(preferences.getString(KEY_CONSOLE_NAME, ""))
        addressInput.setText(preferences.getString(KEY_CONSOLE_ADDRESS, ""))
        updateProfileStatus(profileStatus)

        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val address = addressInput.text.toString().trim()

            if (name.isEmpty()) {
                nameInput.error = getString(R.string.console_name_required)
                return@setOnClickListener
            }

            if (address.isEmpty() || !Patterns.IP_ADDRESS.matcher(address).matches()) {
                addressInput.error = getString(R.string.invalid_ip)
                return@setOnClickListener
            }

            preferences.edit()
                .putString(KEY_CONSOLE_NAME, name)
                .putString(KEY_CONSOLE_ADDRESS, address)
                .apply()

            updateProfileStatus(profileStatus)
            Toast.makeText(this, R.string.console_saved, Toast.LENGTH_SHORT).show()
        }

        updateNetworkStatus()
    }

    override fun onResume() {
        super.onResume()
        updateNetworkStatus()
    }

    private fun updateProfileStatus(status: TextView) {
        val name = preferences.getString(KEY_CONSOLE_NAME, null)
        val address = preferences.getString(KEY_CONSOLE_ADDRESS, null)

        status.text = if (name != null && address != null) {
            getString(R.string.console_profile_saved, name, address)
        } else {
            getString(R.string.console_profile_empty)
        }
    }

    private fun updateNetworkStatus() {
        val status = findViewById<TextView>(R.id.networkStatus)
        val details = findViewById<TextView>(R.id.networkDetails)
        val manager = getSystemService(ConnectivityManager::class.java)
        val network = manager.activeNetwork
        val capabilities = network?.let(manager::getNetworkCapabilities)

        if (capabilities == null) {
            status.setText(R.string.network_offline)
            details.setText(R.string.network_offline_details)
            return
        }

        status.setText(R.string.network_available)
        details.text = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                getString(R.string.network_wifi)
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                getString(R.string.network_ethernet)
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                getString(R.string.network_cellular)
            else -> getString(R.string.network_connected)
        }
    }

    companion object {
        private const val KEY_CONSOLE_NAME = "console_name"
        private const val KEY_CONSOLE_ADDRESS = "console_address"
    }
}
