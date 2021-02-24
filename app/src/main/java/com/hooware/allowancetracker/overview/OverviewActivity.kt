package com.hooware.allowancetracker.overview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.children.ChildDataItem
import com.hooware.allowancetracker.databinding.ActivityOverviewBinding
import com.hooware.allowancetracker.transactions.TransactionDataItem
import com.hooware.allowancetracker.transactions.TransactionDetailsFragment
import com.hooware.allowancetracker.transactions.TransactionDetailsFragmentArgs
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class OverviewActivity : AppCompatActivity() {

    var bundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityOverviewBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_overview
        )
        val intent = this.intent
        bundle = intent.extras
    }

    fun getIntentData(): Bundle? {
        return bundle
    }

    fun clearBundle() {
        bundle = null
    }

    companion object {
        fun newIntent(
            context: Context,
            child: ChildDataItem,
            transaction: TransactionDataItem
        ): Intent {
            val intent = Intent(context, OverviewActivity::class.java)
            intent.putExtra("ChildDataItem", child)
            intent.putExtra("TransactionDataItem", transaction)
            return intent
        }
    }
}