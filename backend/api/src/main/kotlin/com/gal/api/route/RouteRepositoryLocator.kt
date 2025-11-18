package com.gal.api.route

import com.gal.persistence.routes.RouteRepository
import com.gal.persistence.routes.RouteRepositoryExposed

/**
 * Provides access to the RouteRepository instance.
 * This is a simple service locator pattern for dependency injection.
 */
object RouteRepositoryLocator {
    private var repository: RouteRepository? = null

    fun initialize() {
        repository = RouteRepositoryExposed()
    }

    fun getRouteRepository(): RouteRepository? = repository

    fun setRouteRepository(repo: RouteRepository) {
        repository = repo
    }
    
    fun clear() {
        repository = null
    }
}
