package com.example.moodish.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moodish.data.model.Restaurant
import com.example.moodish.databinding.ItemRestaurantBinding
import com.squareup.picasso.Picasso

class RestaurantAdapter : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {
    private var restaurants = listOf<Restaurant>()

    fun updateRestaurants(newRestaurants: List<Restaurant>) {
        restaurants = newRestaurants
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val binding = ItemRestaurantBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RestaurantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.bind(restaurants[position])
    }

    override fun getItemCount() = restaurants.size

    inner class RestaurantViewHolder(private val binding: ItemRestaurantBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(restaurant: Restaurant) {
            binding.apply {
                tvRestaurantName.text = restaurant.name
                tvAddress.text = restaurant.address
                tvRating.text = "★ ${restaurant.rating}"
                
                // Load image if available, otherwise show placeholder
                if (restaurant.photoUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(restaurant.photoUrl)
                        .placeholder(com.example.moodish.R.drawable.ic_restaurant_placeholder)
                        .error(com.example.moodish.R.drawable.ic_restaurant_placeholder)
                        .into(ivRestaurant)
                } else {
                    ivRestaurant.setImageResource(com.example.moodish.R.drawable.ic_restaurant_placeholder)
                }
            }
        }
    }
} 