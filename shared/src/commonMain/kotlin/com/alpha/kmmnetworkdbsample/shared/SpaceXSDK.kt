package com.alpha.kmmnetworkdbsample.shared

import com.alpha.kmmnetworkdbsample.shared.cache.Database
import com.alpha.kmmnetworkdbsample.shared.cache.DatabaseDriverFactory
import com.alpha.kmmnetworkdbsample.shared.entity.RocketLaunch
import com.alpha.kmmnetworkdbsample.shared.network.SpaceApi

class SpaceXSDK(databaseDriverFactory: DatabaseDriverFactory){
    private val database = Database(databaseDriverFactory)
    private val api = SpaceApi()

    @Throws(Exception::class)
    suspend fun getLaunches(forceReload: Boolean): List<RocketLaunch>{
        val cachedLaunches = database.getAllLaunches()
        return if(cachedLaunches.isNotEmpty() && !forceReload){
            cachedLaunches
        }else{
            api.getAllLaunches().also {
                database.clearDatabase()
                database.createLaunches(it)
            }
        }
    }
}
