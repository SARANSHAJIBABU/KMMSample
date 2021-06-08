package com.alpha.kmmnetworkdbsample.shared.cache

import com.alpha.kmmnetworkdbsample.shared.entity.Links
import com.alpha.kmmnetworkdbsample.shared.entity.Rocket
import com.alpha.kmmnetworkdbsample.shared.entity.RocketLaunch

class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    internal fun clearDatabase() {
        dbQuery.transaction {
            dbQuery.removeAllRockets()
            dbQuery.removeAllLaunches()
        }
    }

    internal fun getAllLaunches(): List<RocketLaunch> {
        val query = dbQuery.selectAllLaunchesInfo { flightNumber: Long,
                                                    missionName: String,
                                                    launchYear: Int,
                                                    rocketId: String,
                                                    details: String?,
                                                    launchSuccess: Boolean?,
                                                    launchDateUTC: String,
                                                    missionPatchUrl: String?,
                                                    articleUrl: String?,
                                                    rocket_id: String?,
                                                    name: String?,
                                                    type: String? ->
            RocketLaunch(
                flightNumber = flightNumber.toInt(),
                missionName = missionName,
                launchYear = launchYear,
                details = details,
                launchDateUTC = launchDateUTC,
                launchSuccess = launchSuccess,
                rocket = Rocket(
                    id = rocketId,
                    name = name!!,
                    type = type!!
                ),
                links = Links(
                    missionPatchUrl = missionPatchUrl,
                    articleUrl = articleUrl
                )
            )
        }
        return query.executeAsList()
    }

    internal fun createLaunches(launches: List<RocketLaunch>){
        dbQuery.transaction {
            launches.forEach {
                launch ->
                val rocket = dbQuery.selectRocketById(launch.rocket.id).executeAsOneOrNull()
                if(rocket==null){
                    insertRocket(launch)
                }
                insertLaunch(launch)
            }
        }
    }

    private fun insertLaunch(launch: RocketLaunch) {
        with(launch){
            dbQuery.insertLaunch(
                flightNumber.toLong(),
                missionName,
                launchYear,
                rocket.id,
                details,
                launchSuccess?:false,
                launchDateUTC,
                links.missionPatchUrl,
                links.articleUrl
            )
        }
    }

    private fun insertRocket(launch: RocketLaunch){
        with(launch.rocket){
            dbQuery.insertRocket(
                id, name, type
            )
        }
    }

    private fun mapLaunchSetting(
        flightNumber: Long,
        missionName: String,
        launchYear: Int,
        rocketId: String,
        details: String?,
        launchSuccess: Boolean?,
        launchDateUTC: String,
        missionPatchUrl: String?,
        articleUrl: String?,
        rocket_id: String?,
        name: String?,
        type: String?
    ): RocketLaunch {
        return RocketLaunch(
            flightNumber = flightNumber.toInt(),
            missionName = missionName,
            launchYear = launchYear,
            details = details,
            launchDateUTC = launchDateUTC,
            launchSuccess = launchSuccess,
            rocket = Rocket(
                id = rocketId,
                name = name!!,
                type = type!!
            ),
            links = Links(
                missionPatchUrl = missionPatchUrl,
                articleUrl = articleUrl
            )
        )
    }
}
