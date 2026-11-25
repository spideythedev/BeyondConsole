package com.spideythedev.beyondconsole

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {
    private val preferences by lazy {
        getSharedPreferences("beyondconsole", Context.MODE_PRIVATE)
    }

    private lateinit var nameInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var profileList: LinearLayout
    private lateinit var profileStatus: TextView

    private data class ConsoleProfile(val name: String, val address: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameInput = findViewById(R.id.consoleName)
        addressInput = findViewById(R.id.consoleAddress)
        profileList = findViewById(R.id.consoleProfileList)
        profileStatus = findViewById(R.id.consoleProfileStatus)

        migrateLegacyProfile()
        renderProfiles()

        findViewById<Button>(R.id.saveConsoleButton).setOnClickListener {
            saveProfile()
        }

        updateNetworkStatus()
    }

    override fun onResume() {
        super.onResume()
        updateNetworkStatus()
    }

    private fun saveProfile() {
        val name = nameInput.text.toString().trim()
        val address = addressInput.text.toString().trim()

        if (name.isEmpty()) {
            nameInput.error = getString(R.string.console_name_required)
            return
        }

        if (address.isEmpty() || !Patterns.IP_ADDRESS.matcher(address).matches()) {
            addressInput.error = getString(R.string.invalid_ip)
            return
        }

        val profiles = readProfiles().toMutableList()
        val existing = profiles.indexOfFirst { it.address == address }
        val profile = ConsoleProfile(name, address)

        if (existing >= 0) profiles[existing] = profile else profiles.add(profile)

        writeProfiles(profiles)
        preferences.edit().putString(KEY_SELECTED_ADDRESS, address).apply()
        renderProfiles()
        Toast.makeText(this, R.string.console_saved, Toast.LENGTH_SHORT).show()
    }

    private fun renderProfiles() {
        val profiles = readProfiles()
        val selectedAddress = preferences.getString(KEY_SELECTED_ADDRESS, null)
        profileList.removeAllViews()

        if (profiles.isEmpty()) {
            profileStatus.setText(R.string.console_profile_empty)
            return
        }

        profileStatus.text = getString(R.string.console_profiles_count, profiles.size)

        profiles.forEach { profile ->
            val selected = profile.address == selectedAddress
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(14, 12, 14, 12)
                setBackgroundResource(R.drawable.panel_background)
            }

            val title = TextView(this).apply {
                text = if (selected) getString(R.string.console_profile_selected, profile.name) else profile.name
                setTextColor(getColor(R.color.text_primary))
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
            }
            val address = TextView(this).apply {
                text = profile.address
                setTextColor(getColor(R.color.text_secondary))
                textSize = 13f
                setPadding(0, 4, 0, 8)
            }
            val actions = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
            }
            val select = Button(this).apply {
                text = getString(R.string.select_console)
                isEnabled = !selected
                setOnClickListener {
                    nameInput.setText(profile.name)
                    addressInput.setText(profile.address)
                    preferences.edit().putString(KEY_SELECTED_ADDRESS, profile.address).apply()
                    renderProfiles()
                }
            }
            val delete = Button(this).apply {
                text = getString(R.string.delete_console)
                setOnClickListener { deleteProfile(profile) }
            }

            actions.addView(select)
            actions.addView(delete)
            row.addView(title)
            row.addView(address)
            row.addView(actions)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10) }
            profileList.addView(row, params)
        }
    }

    private fun deleteProfile(profile: ConsoleProfile) {
        val profiles = readProfiles().filterNot { it.address == profile.address }
        writeProfiles(profiles)

        if (preferences.getString(KEY_SELECTED_ADDRESS, null) == profile.address) {
            preferences.edit().remove(KEY_SELECTED_ADDRESS).apply()
            nameInput.text.clear()
            addressInput.text.clear()
        }

        renderProfiles()
    }

    private fun readProfiles(): List<ConsoleProfile> {
        val raw = preferences.getString(KEY_PROFILES, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val name = item.optString("name").trim()
                val address = item.optString("address").trim()
                if (name.isNotEmpty() && Patterns.IP_ADDRESS.matcher(address).matches()) {
                    ConsoleProfile(name, address)
                } else null
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun writeProfiles(profiles: List<ConsoleProfile>) {
        val array = JSONArray()
        profiles.forEach { profile ->
            array.put(JSONObject().put("name", profile.name).put("address", profile.address))
        }
        preferences.edit().putString(KEY_PROFILES, array.toString()).apply()
    }

    private fun migrateLegacyProfile() {
        if (preferences.contains(KEY_PROFILES)) return
        val name = preferences.getString(KEY_CONSOLE_NAME, null)
        val address = preferences.getString(KEY_CONSOLE_ADDRESS, null)
        if (!name.isNullOrBlank() && !address.isNullOrBlank() &&
            Patterns.IP_ADDRESS.matcher(address).matches()
        ) {
            writeProfiles(listOf(ConsoleProfile(name, address)))
            preferences.edit().putString(KEY_SELECTED_ADDRESS, address).apply()
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
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> getString(R.string.network_wifi)
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> getString(R.string.network_ethernet)
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> getString(R.string.network_cellular)
            else -> getString(R.string.network_connected)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val KEY_CONSOLE_NAME = "console_name"
        private const val KEY_CONSOLE_ADDRESS = "console_address"
        private const val KEY_PROFILES = "console_profiles"
        private const val KEY_SELECTED_ADDRESS = "selected_console_address"
    }
}
