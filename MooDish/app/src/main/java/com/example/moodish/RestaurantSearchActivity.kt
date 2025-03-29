package com.example.moodish

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moodish.adapter.RestaurantAdapter
import com.example.moodish.data.model.Restaurant
import com.example.moodish.databinding.ActivityRestaurantSearchBinding
import com.example.moodish.utils.NavigationUtils
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FetchPlaceRequest

class RestaurantSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRestaurantSearchBinding
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var placesClient: PlacesClient
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Places
        Places.initialize(applicationContext, "PLACES_API_KEY")
        placesClient = Places.createClient(this)

        userEmail = intent.getStringExtra("USER_EMAIL")
        setupRecyclerView()
        setupSearchButton()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        restaurantAdapter = RestaurantAdapter()
        binding.rvRestaurants.apply {
            layoutManager = LinearLayoutManager(this@RestaurantSearchActivity)
            adapter = restaurantAdapter
        }
    }

    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            val city = binding.etCitySearch.text.toString().trim()
            if (city.isNotEmpty()) {
                searchRestaurants(city)
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigation(
            activity = this,
            bottomNav = binding.bottomNavigation,
            userEmail = userEmail,
            currentDestination = R.id.nav_search
        )
    }

    private fun searchRestaurants(city: String) {
        // Show progress at the start of search
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSearch.isEnabled = false  // Disable search button while loading

        val token = AutocompleteSessionToken.newInstance()
        
        val cityRequest = FindAutocompletePredictionsRequest.builder()
            .setQuery("$city restaurants")
            .setSessionToken(token)
            .build()

        placesClient.findAutocompletePredictions(cityRequest)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions.take(3)
                fetchRestaurantDetails(predictions)
            }
            .addOnFailureListener { exception ->
                // Hide progress and show error
                binding.progressBar.visibility = View.GONE
                binding.btnSearch.isEnabled = true
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchRestaurantDetails(predictions: List<AutocompletePrediction>) {
        val restaurants = mutableListOf<Restaurant>()
        var completedRequests = 0

        // If no predictions found, hide progress and show message
        if (predictions.isEmpty()) {
            binding.progressBar.visibility = View.GONE
            binding.btnSearch.isEnabled = true
            Toast.makeText(this, "No restaurants found in this city", Toast.LENGTH_SHORT).show()
            return
        }

        predictions.forEach { prediction ->
            val placeFields = listOf(
                Place.Field.NAME,
                Place.Field.RATING,
                Place.Field.ADDRESS,
                Place.Field.PHOTO_METADATAS
            )

            val fetchPlaceRequest = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()

            placesClient.fetchPlace(fetchPlaceRequest)
                .addOnSuccessListener { response ->
                    val place = response.place
                    
                    restaurants.add(
                        Restaurant(
                            name = place.name ?: "",
                            rating = place.rating ?: 0.0,
                            address = place.address ?: "",
                            photoUrl = "" // We'll handle photos later
                        )
                    )

                    completedRequests++
                    if (completedRequests == predictions.size) {
                        // All requests completed, update UI
                        val sortedRestaurants = restaurants.sortedByDescending { it.rating }
                        restaurantAdapter.updateRestaurants(sortedRestaurants)
                        binding.progressBar.visibility = View.GONE
                        binding.btnSearch.isEnabled = true
                        
                        // Show success message
                        Toast.makeText(this, 
                            "Found ${sortedRestaurants.size} restaurants", 
                            Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    completedRequests++
                    if (completedRequests == predictions.size) {
                        // All requests completed (even with failures)
                        binding.progressBar.visibility = View.GONE
                        binding.btnSearch.isEnabled = true
                        
                        // If we have some results, show them
                        if (restaurants.isNotEmpty()) {
                            val sortedRestaurants = restaurants.sortedByDescending { it.rating }
                            restaurantAdapter.updateRestaurants(sortedRestaurants)
                        }
                    }
                    Toast.makeText(this, 
                        "Error fetching place details: ${exception.message}", 
                        Toast.LENGTH_SHORT).show()
                }
        }
    }
}