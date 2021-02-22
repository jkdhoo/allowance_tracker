package com.hooware.allowancetracker.overview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.databinding.ActivityOverviewBinding

class OverviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityOverviewBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_overview
        )
    }
}