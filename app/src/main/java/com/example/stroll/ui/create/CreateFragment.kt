package com.example.stroll.ui.create

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.stroll.R
import com.example.stroll.data.AppDatabase
import com.example.stroll.data.Quest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class AddressSuggestion(val displayName: String, val lat: Double, val lon: Double) {
    override fun toString(): String = displayName
}

class CreateFragment : Fragment() {

    private lateinit var map: MapView
    private lateinit var etStartPoint: AutoCompleteTextView
    private lateinit var etDestination: AutoCompleteTextView
    private lateinit var etQuestName: EditText
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var ivQuestImage: ImageView
    private lateinit var btnPickImage: FrameLayout
    private lateinit var btnCreateQuest: Button
    private lateinit var database: AppDatabase

    private var startMarker: Marker? = null
    private var destMarker: Marker? = null
    private var isSettingStartPoint = true
    private var selectedImageUri: Uri? = null
    private var calculatedDistanceKm: Double = 0.0

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            requireActivity().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedImageUri = uri
            ivQuestImage.setImageURI(uri)
            ivQuestImage.visibility = View.VISIBLE
            view?.findViewById<TextView>(R.id.tv_image_hint)?.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())

        map = view.findViewById(R.id.map_view)
        etStartPoint = view.findViewById(R.id.et_start_point)
        etDestination = view.findViewById(R.id.et_destination)
        etQuestName = view.findViewById(R.id.et_quest_name)
        spinnerDifficulty = view.findViewById(R.id.spinner_difficulty)
        ivQuestImage = view.findViewById(R.id.iv_quest_image)
        btnPickImage = view.findViewById(R.id.btn_pick_image)
        btnCreateQuest = view.findViewById(R.id.btn_create_quest)

        setupUI()
        setupMap()
        setupAutocomplete(etStartPoint, true)
        setupAutocomplete(etDestination, false)
    }

    private fun setupUI() {
        val difficulties = arrayOf("Easy (1-3 km)", "Medium (3-7 km)", "Hard (7+ km)", "Extreme")
        spinnerDifficulty.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, difficulties)
        spinnerDifficulty.isEnabled = false // Робимо його неактивним, бо він обирається автоматично

        btnPickImage.setOnClickListener { pickImageLauncher.launch("image/*") }

        etStartPoint.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) isSettingStartPoint = true }
        etDestination.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) isSettingStartPoint = false }

        btnCreateQuest.setOnClickListener { saveQuest() }
    }

    private fun setupMap() {
        map.setMultiTouchControls(true)
        map.setOnTouchListener { v, _ -> v.parent.requestDisallowInterceptTouchEvent(true); false }

        val mapController = map.controller
        mapController.setZoom(13.0)
        mapController.setCenter(GeoPoint(50.4501, 30.5234))

        map.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { processMapClick(it) }
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        }))
    }

    private fun processMapClick(geoPoint: GeoPoint, customAddress: String? = null) {
        lifecycleScope.launch(Dispatchers.IO) {
            val addressName = customAddress ?: getAddressFromGeoPoint(geoPoint)
            
            withContext(Dispatchers.Main) {
                if (isSettingStartPoint) {
                    if (startMarker != null) map.overlays.remove(startMarker)
                    startMarker = Marker(map).apply { position = geoPoint; title = addressName }
                    map.overlays.add(startMarker)
                    etStartPoint.setText(addressName)
                    isSettingStartPoint = false
                } else {
                    if (destMarker != null) map.overlays.remove(destMarker)
                    destMarker = Marker(map).apply { position = geoPoint; title = addressName }
                    map.overlays.add(destMarker)
                    etDestination.setText(addressName)
                    isSettingStartPoint = true
                }
                map.controller.animateTo(geoPoint)
                map.invalidate()
                
                calculateDistanceAndDifficulty()
            }
        }
    }

    private fun calculateDistanceAndDifficulty() {
        if (startMarker != null && destMarker != null) {
            val startPoint = startMarker!!.position
            val destPoint = destMarker!!.position
            
            val distanceMeters = startPoint.distanceToAsDouble(destPoint)
            calculatedDistanceKm = distanceMeters / 1000.0

            val difficultyIndex = when {
                calculatedDistanceKm < 3.0 -> 0 // Easy
                calculatedDistanceKm in 3.0..7.0 -> 1 // Medium
                calculatedDistanceKm in 7.0..15.0 -> 2 // Hard
                else -> 3 // Extreme
            }
            spinnerDifficulty.setSelection(difficultyIndex)
            Toast.makeText(requireContext(), "Distance: ${String.format("%.1f", calculatedDistanceKm)} km", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAddressFromGeoPoint(geoPoint: GeoPoint): String {
        return try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val a = addresses[0]
                val street = a.thoroughfare ?: ""
                val house = a.subThoroughfare ?: "" 
                val city = a.locality ?: ""
                if (street.isNotEmpty()) "$street $house, $city".trim() else a.getAddressLine(0)
            } else "Lat: ${String.format("%.3f", geoPoint.latitude)}"
        } catch (e: Exception) {
            "Custom Point"
        }
    }

    private fun setupAutocomplete(autoCompleteView: AutoCompleteTextView, isStart: Boolean) {
        var searchJob: Job? = null

        autoCompleteView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.length < 3 || autoCompleteView.isPerformingCompletion) return

                searchJob?.cancel()
                searchJob = lifecycleScope.launch(Dispatchers.IO) {
                    delay(600)
                    val suggestions = fetchAddressSuggestions(query)
                    withContext(Dispatchers.Main) {
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
                        autoCompleteView.setAdapter(adapter)
                        autoCompleteView.showDropDown()
                    }
                }
            }
        })

        autoCompleteView.setOnItemClickListener { parent, _, position, _ ->
            val suggestion = parent.getItemAtPosition(position) as AddressSuggestion
            isSettingStartPoint = isStart
            processMapClick(GeoPoint(suggestion.lat, suggestion.lon), suggestion.displayName)
        }
    }

    private fun fetchAddressSuggestions(query: String): List<AddressSuggestion> {
        val resultList = mutableListOf<AddressSuggestion>()
        try {
            val url = URL("https://photon.komoot.io/api/?q=${URLEncoder.encode(query, "UTF-8")}&limit=5")
            val connection = url.openConnection() as HttpURLConnection
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val jsonResponse = JSONObject(reader.readText())
            reader.close()

            val features = jsonResponse.getJSONArray("features")
            for (i in 0 until features.length()) {
                val geometry = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates")
                val lon = geometry.getDouble(0)
                val lat = geometry.getDouble(1)

                val props = features.getJSONObject(i).getJSONObject("properties")
                val name = props.optString("name", "")
                val street = props.optString("street", "")
                val housenumber = props.optString("housenumber", "")
                val city = props.optString("city", "")

                val addressParts = mutableListOf<String>()
                if (name.isNotEmpty()) addressParts.add(name)
                if (street.isNotEmpty()) {
                    val fullStreet = if (housenumber.isNotEmpty()) "$street $housenumber" else street
                    if (fullStreet != name) addressParts.add(fullStreet)
                }
                if (city.isNotEmpty() && city != name) addressParts.add(city)

                if (addressParts.isNotEmpty()) {
                    resultList.add(AddressSuggestion(addressParts.joinToString(", "), lat, lon))
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return resultList
    }

    private fun saveQuest() {
        val name = etQuestName.text.toString()
        if (name.isEmpty() || startMarker == null || destMarker == null || selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please fill all fields and select an image!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val quest = Quest(
                name = name,
                startLocationName = etStartPoint.text.toString(),
                destLocationName = etDestination.text.toString(),
                distanceKm = calculatedDistanceKm,
                difficulty = spinnerDifficulty.selectedItem.toString(),
                imageUri = selectedImageUri.toString()
            )
            database.questDao().insertQuest(quest)
            
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Quest Saved!", Toast.LENGTH_LONG).show()
                etQuestName.text.clear()
                etStartPoint.text.clear()
                etDestination.text.clear()
                ivQuestImage.visibility = View.GONE
                selectedImageUri = null
                map.overlays.removeAll { it is Marker }
                map.invalidate()
            }
        }
    }

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause() { super.onPause(); map.onPause() }
}