package com.dev.lokeshkalal.todomvi.view.statistics

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dev.lokeshkalal.todomvi.R
import com.dev.lokeshkalal.todomvi.util.replaceFragmentInActivity
import com.dev.lokeshkalal.todomvi.util.setupActionBar

/**
 * Activity houses the Toolbar, the nav UI, the FAB and the fragment for stats.
 */
class StatisticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics_act)

        // Set up the toolbar.
        setupActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        supportFragmentManager.findFragmentById(R.id.contentFrame) as StatisticsFragment?
            ?: StatisticsFragment().also {
                replaceFragmentInActivity(it as Fragment, R.id.contentFrame)
            }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}