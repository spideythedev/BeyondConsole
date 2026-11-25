package com.spideythedev.beyondconsole

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import android.widget.ScrollView
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
import java.net.HttpURLConnection
import java.net.URL
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
        findViewById<TextView>(R.id.menuButton).setOnClickListener { showMenu(it) }

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


    private fun showMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        val items = listOf(
            "Documentation" to "documentation",
            "User guide" to "guide",
            "FAQs" to "faq",
            "Dependencies" to "dependencies",
            "License" to "license",
            "About" to "about",
            "Credits" to "credits",
            "Tech stack" to "stack",
            "Copyright" to "copyright",
            "Privacy & data" to "privacy",
            "Support" to "support"
        )
        items.forEach { (label, _) -> popup.menu.add(label) }
        popup.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                "Documentation" -> showInfo("BeyondConsole documentation", "BeyondConsole is an early Android companion project.\\n\\nUse this menu to find setup instructions, feature notes, technical details, licensing, contributors, and support.\\n\\nRepository: github.com/spideythedev/BeyondConsole")
                "User guide" -> showInfo("User guide", "SAVE A CONSOLE\\n1. Find your console's local IPv4 address.\\n2. Enter a name and IP address on the home screen.\\n3. Tap Save console address.\\n4. Tap Select to choose a saved profile.\\n\\nSCAN YOUR WI-FI\\n1. Connect your phone and console to the same Wi-Fi network.\\n2. Tap Scan local network.\\n3. Treat matches as potential devices only.\\n\\nREMOVE A PROFILE\\nTap Remove beside the saved profile.\\n\\nThe current app does not pair with a console or stream gameplay yet.")
                "FAQs" -> showInfo("Frequently asked questions", "DOES IT STREAM PS5 GAMES?\\nNot yet. Session negotiation, video, audio and controller streaming are not implemented.\\n\\nWHY WASN'T MY CONSOLE FOUND?\\nThe console may be asleep, on another subnet, or blocked by router isolation. You can add its IP manually.\\n\\nDOES A SCAN RESULT VERIFY A PLAYSTATION?\\nNo. It only checks TCP port 9295.\\n\\nWHERE ARE PROFILES STORED?\\nLocally on this Android device.\\n\\nWHICH ANDROID VERSIONS?\\nAndroid 8.0 (API 26) and newer.")
                "Dependencies" -> showInfo("Dependencies", "APP RUNTIME\\nNo third-party runtime libraries are currently included.\\n\\nBUILD TOOLS\\nAndroid Gradle Plugin 8.7.3\\nKotlin Gradle Plugin 2.0.21\\nGradle 8.9 (CI provisioned)\\nJDK 17\\nAndroid compile SDK 35\\nMinimum SDK 26\\n\\nPLATFORM APIS\\nAndroid Views, SharedPreferences, ConnectivityManager, org.json and Java networking APIs.\\n\\nFull inventory: docs/DEPENDENCIES.md")
                "License" -> showInfo("Apache License 2.0", "BeyondConsole is distributed under the Apache License, Version 2.0.\\n\\nYou may use, modify and redistribute the project under the license terms. Keep copyright and license notices, include the license with distributions, and mark changed files. The license also includes an express patent grant.\\n\\nFull text: https://www.apache.org/licenses/LICENSE-2.0\\n\\nSee the LICENSE file in the repository.")
                "About" -> showInfo("About BeyondConsole", "BeyondConsole is an independent open-source project exploring ways to make console gaming more accessible across devices, including Android.\\n\\nThe current build is an early companion app with local console profiles and basic LAN port discovery. Remote Play compatibility is not implemented yet.\\n\\nBuilt by Fahad Malik (SpydeY).")
                "Credits" -> loadContributors()
                "Tech stack" -> showInfo("Technology", "KOTLIN\\nAndroid application logic.\\n\\nANDROID SDK\\nNative Views, XML layouts, ConnectivityManager and SharedPreferences.\\n\\nJAVA STANDARD LIBRARY\\nSockets, HTTP connections, executors and JSON parsing.\\n\\nGRADLE\\nBuild and dependency management.\\n\\nGITHUB ACTIONS\\nAutomated Android debug builds.")
                "Copyright" -> showInfo("Copyright & trademarks", "Copyright © 2026 Fahad Malik and BeyondConsole contributors.\\n\\nLicensed under the Apache License, Version 2.0. You may obtain a copy at https://www.apache.org/licenses/LICENSE-2.0.\\n\\nPlayStation and PS5 are trademarks of Sony Interactive Entertainment. BeyondConsole is an independent community project and is not affiliated with, endorsed by, or sponsored by Sony Interactive Entertainment.")
                "Privacy & data" -> showInfo("Privacy & data", "Console names and local IP addresses are stored on this device using SharedPreferences.\\n\\nLAN discovery sends TCP connection attempts to local addresses on port 9295.\\n\\nThe Credits screen contacts the public GitHub API to retrieve repository contributor names and contribution counts.\\n\\nThe current app has no BeyondConsole account, analytics SDK, or project backend. Do not share private network details in public support requests.")
                "Support" -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("mailto:hellospideydev@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "BeyondConsole support")
                    }
                    startActivity(intent)
                }
            }
            true
        }
        popup.show()
    }

    private fun showInfo(title: String, message: String) {
        val content = TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(getColor(R.color.text_primary))
            setPadding(dp(20), dp(18), dp(20), dp(18))
            setTextIsSelectable(true)
            setLineSpacing(dp(4).toFloat(), 1f)
        }
        val scroll = ScrollView(this).apply { addView(content) }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(scroll)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun loadContributors() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Project credits")
            .setMessage("Loading contributors from GitHub…")
            .setNegativeButton("Close", null)
            .create()
        dialog.show()

        scanExecutor.execute {
            val message = try {
                val connection = URL(
                    "https://api.github.com/repos/spideythedev/BeyondConsole/contributors?per_page=100"
                ).openConnection() as HttpURLConnection
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                try {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val contributors = JSONArray(response)
                    val lines = mutableListOf(
                        "PROJECT FOUNDER",
                        "Fahad Malik (SpydeY) — project creator and maintainer",
                        "",
                        "GITHUB CONTRIBUTORS"
                    )
                    if (contributors.length() == 0) {
                        lines.add("No additional GitHub contributors are listed yet.")
                    } else {
                        for (index in 0 until contributors.length()) {
                            val person = contributors.getJSONObject(index)
                            val login = person.optString("login", "Unknown")
                            val count = person.optInt("contributions", 0)
                            if (login.equals("spideythedev", true)) continue
                            lines.add("$login — $count contributions")
                        }
                        if (lines.size == 4) lines.add("No additional contributors are listed yet.")
                    }
                    lines.add("")
                    lines.add("Contributor data is retrieved from the public GitHub Contributors API.")
                    lines.add("https://github.com/spideythedev/BeyondConsole/graphs/contributors")
                    lines.joinToString("\\n")
                } finally {
                    connection.disconnect()
                }
            } catch (_: Exception) {
                "PROJECT FOUNDER\\nFahad Malik (SpydeY) — project creator and maintainer\\n\\nCould not load the live contributor list. Check your connection or visit:\\nhttps://github.com/spideythedev/BeyondConsole/graphs/contributors"
            }
            mainHandler.post {
                if (!isFinishing && dialog.isShowing) {
                    dialog.dismiss()
                    showInfo("Project credits", message)
                }
            }
        }
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
