package com.samyak.repostore.util

import android.util.LruCache
import android.widget.TextView
import com.samyak.repostore.R
import com.samyak.repostore.data.model.GitHubRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Global LRU Cache for parsed real app names. Keep last 300 apps to prevent repeated parsing.
 */
private val appNameCache = LruCache<String, String>(300)

/**
 * Lazily loads the Real Application Name into the TextView.
 * - Instantly sets the GitHub repo name as a fallback.
 * - Checks memory cache for instant UI pop-in.
 * - Spawns a dedicated coroutine tied to the exact list item rendering.
 * - Cancels previous jobs if the view gets recycled quickly during scrolling.
 */
fun TextView.loadRealAppName(repo: GitHubRepo) {
    // 1. Instantly set the fallback
    this.text = repo.name

    val cacheKey = repo.fullName
    
    // 2. Check instant memory cache
    val cachedName = appNameCache.get(cacheKey)
    if (cachedName != null) {
        this.text = cachedName
        return
    }

    // 3. Cancel any lingering jobs on this exact recycled view
    val oldJob = this.getTag(R.id.tag_app_name_job) as? Job
    oldJob?.cancel()

    // 4. Tag the view with the current repo ID to prevent race conditions
    this.setTag(R.id.tag_app_name_id, repo.id)

    // 5. Fire background worker
    // We use a detached scope since the view might detach/recycle but we still want the cache populated.
    val job = CoroutineScope(Dispatchers.Main).launch {
        val realName = AppNameFetcher.fetchRealName(repo)
        
        if (realName != null) {
            appNameCache.put(cacheKey, realName)
            
            // Only update UI if this view wasn't recycled to a different repo
            val currentAttachedId = this@loadRealAppName.getTag(R.id.tag_app_name_id) as? Long
            if (currentAttachedId == repo.id) {
                this@loadRealAppName.text = realName
            }
        }
    }

    // Store the job to cancel it if view recycles
    this.setTag(R.id.tag_app_name_job, job)
}

/**
 * Overload for FavoriteApp offline entities without full GitHubRepo models.
 */
fun TextView.loadRealAppName(fullName: String, owner: String, name: String, language: String?, id: Long) {
    // 1. Instantly set the fallback
    this.text = name

    // 2. Check instant memory cache
    val cachedName = appNameCache.get(fullName)
    if (cachedName != null) {
        this.text = cachedName
        return
    }

    // 3. Cancel any lingering jobs on this exact recycled view
    val oldJob = this.getTag(R.id.tag_app_name_job) as? Job
    oldJob?.cancel()

    // 4. Tag the view with the current repo ID to prevent race conditions
    this.setTag(R.id.tag_app_name_id, id)

    // 5. Fire background worker
    val job = CoroutineScope(Dispatchers.Main).launch {
        // Fallback to "main" branch since FavoriteApp doesn't track default branch
        val realName = AppNameFetcher.fetchRealName(owner, name, "main", language)
        
        if (realName != null) {
            appNameCache.put(fullName, realName)
            
            val currentAttachedId = this@loadRealAppName.getTag(R.id.tag_app_name_id) as? Long
            if (currentAttachedId == id) {
                this@loadRealAppName.text = realName
            }
        }
    }

    this.setTag(R.id.tag_app_name_job, job)
}
