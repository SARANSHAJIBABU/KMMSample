package com.alpha.kmmnetworkdbsample.androidApp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.alpha.kmmnetworkdbsample.shared.Greeting
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alpha.kmmnetworkdbsample.shared.SpaceXSDK
import com.alpha.kmmnetworkdbsample.shared.cache.DatabaseDriverFactory
import com.alpha.kmmnetworkdbsample.shared.entity.RocketLaunch
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var launchesRecyclerView : RecyclerView
    private lateinit var progressBarView: FrameLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val launchesRvAdapter = LaunchesRvAdapter(listOf())
    private val sdk = lazy {
        SpaceXSDK(DatabaseDriverFactory(this))
    }
    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "SpaceX Launches"

        launchesRecyclerView = findViewById(R.id.launchesListRv)
        progressBarView = findViewById(R.id.progressBar)
        swipeRefreshLayout = findViewById(R.id.swipeContainer)

        launchesRecyclerView.adapter = launchesRvAdapter
        launchesRecyclerView.layoutManager = LinearLayoutManager(this)

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            displayLaunches(true)
        }

        displayLaunches(false)

    }

    class LaunchesRvAdapter(var launches: List<RocketLaunch>): RecyclerView.Adapter<LaunchesRvAdapter.LaunchesViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaunchesViewHolder {
            return LayoutInflater.from(parent.context)
                .inflate(R.layout.item_launch,parent,false)
                .run{
                    LaunchesViewHolder(this)
                }
        }

        override fun onBindViewHolder(holder: LaunchesViewHolder, position: Int) {
           holder.bindData(launches[position])
        }

        override fun getItemCount() = launches.size

        inner class LaunchesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            private val missionNameTextView = itemView.findViewById<TextView>(R.id.missionName)
            private val launchYearTextView = itemView.findViewById<TextView>(R.id.launchYear)
            private val launchSuccessTextView = itemView.findViewById<TextView>(R.id.launchSuccess)
            private val missionDetailsTextView = itemView.findViewById<TextView>(R.id.details)

            fun bindData(launch: RocketLaunch){
                val ctx = itemView.context
                missionNameTextView.text = ctx.getString(R.string.mission_name_field, launch.missionName)
                launchYearTextView.text = ctx.getString(R.string.launch_year_field, launch.launchYear.toString())
                missionDetailsTextView.text = ctx.getString(R.string.details_field, launch.details ?: "")

                val launchSuccess = launch.launchSuccess
                if (launchSuccess != null ) {
                    if (launchSuccess) {
                        launchSuccessTextView.text = ctx.getString(R.string.successful)
                        launchSuccessTextView.setTextColor((ContextCompat.getColor(itemView.context, R.color.colorSuccessful)))
                    } else {
                        launchSuccessTextView.text = ctx.getString(R.string.unsuccessful)
                        launchSuccessTextView.setTextColor((ContextCompat.getColor(itemView.context, R.color.colorUnsuccessful)))
                    }
                } else {
                    launchSuccessTextView.text = ctx.getString(R.string.no_data)
                    launchSuccessTextView.setTextColor((ContextCompat.getColor(itemView.context, R.color.colorNoData)))
                }
            }
        }

    }

    private fun displayLaunches(needReload: Boolean){
        progressBarView.isVisible = true
        mainScope.launch {
            kotlin.runCatching {
                sdk.value.getLaunches(needReload)
            }.onSuccess {
                launchesRvAdapter.launches = it
                launchesRvAdapter.notifyDataSetChanged()
            }.onFailure {
                Toast.makeText(this@MainActivity,it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
            progressBarView.isVisible = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }


}
