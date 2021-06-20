package com.hooware.allowancetracker.overview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.AuthActivity
import com.hooware.allowancetracker.auth.FirebaseUserLiveData
import com.hooware.allowancetracker.databinding.ActivityOverviewBinding
import timber.log.Timber

class OverviewActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, OverviewActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityOverviewBinding = DataBindingUtil.setContentView(this, R.layout.activity_overview)
        binding.lifecycleOwner = this
        FirebaseUserLiveData().observe(this, { user ->
            if (user == null) {
                val app = application as AllowanceApp
                app.firebaseUID.value = null
                Timber.i("Not authenticated. Authenticating...")
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }
}