package com.samyak.repostore.util

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.samyak.repostore.R

/**
 * Loads an app icon by building an atomic chain of requests using Glide's .error() mechanism.
 * This ensures the fastest possible fallback between candidates while showing the avatar as a thumbnail.
 * 
 * @param urls List of prioritized "real" icon candidates.
 * @param fallbackUrl The ultimate fallback URL (owner avatar).
 * @param circleCrop Whether to apply a circle crop to the image.
 */
fun ImageView.loadIconWithFallback(
    urls: List<String>,
    fallbackUrl: String,
    circleCrop: Boolean = false
) {
    // Start by building the ultimate fallback (the avatar)
    val avatarRequest = Glide.with(this)
        .load(fallbackUrl)
        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
        .placeholder(R.drawable.ic_app_placeholder)
        .error(R.drawable.ic_app_placeholder)
        .let { if (circleCrop) it.circleCrop() else it }

    if (urls.isEmpty()) {
        avatarRequest.into(this)
        return
    }

    // Build the candidate request chain from back to front
    // This allows each request to specify the NEXT one in its .error() slot
    var currentRequest = avatarRequest

    // Reverse the list to build the chain: Fallback <- Candidate N <- ... <- Candidate 1
    urls.reversed().forEach { url ->
        currentRequest = Glide.with(this)
            .load(url)
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
            .timeout(3000) // Fast 3-second timeout for discovery transition
            .error(currentRequest)
            .let { if (circleCrop) it.circleCrop() else it }
    }

    // Use the avatar as a thumbnail for instant feedback while the chain is running
    currentRequest.thumbnail(
        Glide.with(this)
            .load(fallbackUrl)
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
            .let { if (circleCrop) it.circleCrop() else it }
    ).into(this)
}
