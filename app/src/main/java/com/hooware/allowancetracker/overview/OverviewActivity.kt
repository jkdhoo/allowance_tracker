package com.hooware.allowancetracker.overview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.databinding.ActivityOverviewBinding
import com.hooware.allowancetracker.to.ChildTO
import com.hooware.allowancetracker.to.TransactionTO

class OverviewActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context, child: ChildTO, transaction: TransactionTO): Intent {
            val intent = Intent(context, OverviewActivity::class.java)
            intent.putExtra("ChildTO", child)
            intent.putExtra("TransactionTO", transaction)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityOverviewBinding = DataBindingUtil.setContentView(this, R.layout.activity_overview)
        binding.lifecycleOwner = this
    }
}