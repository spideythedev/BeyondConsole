package com.spideythedev.beyondconsole

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.net.Inet4Address
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : Activity() {
    private val preferences by lazy {
        getSharedPreferences("beyondconsole", Context.MODE_PRIVATE)
    }
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scanExecutor = Executors.newFixedThreadPool(32)

    private lateinit var nameInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var profileList: LinearLayout
    private lateinit var profileStatus: TextView
    private lateinit var scanButton: Button
    private lateinit var scanResults: TextView

    private data class ConsoleProfile(val name: String, val address: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameInput = findViewById(R.id.consoleName)
        addressInput = findViewById(R.id.consoleAddress)
        profileList = findViewById(R.id.consoleProfileList)
        profileStatus = findViewById(R.id.consoleProfileStatus)
        scanButton = findViewById(R.id.scanConsoleButton)
        scanResults = findViewById(R.id.scanResults)

        migrateLegacyProfile()
        renderProfiles()

        findViewById<Button>(R.id.saveConsoleButton).setOnClickListener { saveProfile() }
        scanButton.setOnClickListener { scanLocalNetwork() }
        updateNetworkStatus()
    }

    override fun onResume() {
        super.onResume()
        updateNetworkStatus()
    }

    override fun onDestroy() {
        scanExecutor.shutdownNow()
        super.onDestroy()
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
                setBackgroundResource(R.drawable.button_secondary)
                setTextColor(getColor(R.color.accent))
                textAllCaps = false
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
                setBackgroundResource(R.drawable.button_secondary)
                setTextColor(getColor(R.color.text_secondary))
                textAllCaps = false
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
        writeProfiles(readProfiles().filterNot { it.address == profile.address })
        if (preferences.getString(KEY_SELECTED_ADDRESS, null) == profile.address) {
            preferences.edit().remove(KEY_SELECTED_ADDRESS).apply()
            nameInput.text.clear()
            addressInput.text.clear()
        }
        renderProfiles()
    }

    private fun scanLocalNetwork() {
        val manager = getSystemService(ConnectivityManager::class.java)
        val network = manager.activeNetwork
        val capabilities = network?.let(manager::getNetworkCapabilities)
        if (capabilities == null || !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            scanResults.setText(R.string.scan_wifi_required)
            return
        }

        val linkProperties = manager.getLinkProperties(network)
        if (linkProperties == null) {
            scanResults.setText(R.string.scan_network_unavailable)
            return
        }

        val localAddress = linkProperties.linkAddresses
            .mapNotNull { it.address as? Inet4Address }
            .firstOrNull { !it.isLoopbackAddress && !it.isLinkLocalAddress }

        if (localAddress == null) {
            scanResults.setText(R.string.scan_network_unavailable)
            return
        }

        val link = linkProperties.linkAddresses.first { it.address == localAddress }
        val prefix = link.prefixLength.coerceAtMost(24)
        val ip = ipv4ToLong(localAddress.address)
        val mask = if (prefix == 0) 0L else (0xffffffffL shl (32 - prefix)) and 0xffffffffL
        val networkAddress = ip and mask
        val broadcastAddress = networkAddress or (mask xor 0xffffffffL)
        val hosts = (networkAddress + 1 until broadcastAddress).filter { it != ip }

        scanButton.isEnabled = false
        scanResults.text = getString(R.string.scan_running, hosts.size)
        val found = java.util.Collections.synchronizedList(mutableListOf<String>())
        val remaining = AtomicInteger(hosts.size)

        if (hosts.isEmpty()) {
            scanResults.setText(R.string.scan_no_hosts)
            scanButton.isEnabled = true
            return
        }

        hosts.forEach { host ->
            scanExecutor.execute {
                val address = longToIpv4(host)
                val open = try {
                    Socket().use { socket ->
                        socket.connect(java.net.InetSocketAddress(address, REMOTE_PLAY_PORT), SCAN_TIMEOUT_MS)
                    }
                    true
                } catch (_: Exception) {
                    false
                }

                if (open) {
                    found.add(address)
                    mainHandler.post { showScanResults(found.toList(), false) }
                }

                if (remaining.decrementAndGet() == 0) {
                    mainHandler.post {
                        showScanResults(found.toList(), true)
                        scanButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun showScanResults(addresses: List<String>, finished: Boolean) {
        scanResults.text = when {
            addresses.isNotEmpty() -> getString(
                if (finished) R.string.scan_candidates_found else R.string.scan_candidate_found,
                addresses.sorted().joinToString("\n")
            )
            finished -> getString(R.string.scan_none_found)
            else -> getString(R.string.scan_running_progress)
        }
    }

    private fun ipv4ToLong(bytes: ByteArray): Long =
        bytes.fold(0L) { result, byte -> (result shl 8) or (byte.toLong() and 0xff) }

    private fun longToIpv4(value: Long): String =
        InetAddress.getByAddress(byteArrayOf(
            (value shr 24).toByte(),
            (value shr 16).toByte(),
            (value shr 8).toByte(),
            value.toByte()
        )).hostAddress ?: ""

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
        profiles.forEach { array.put(JSONObject().put("name", it.name).put("address", it.address)) }
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
        private const val REMOTE_PLAY_PORT = 9295
        private const val SCAN_TIMEOUT_MS = 450
    }
}
