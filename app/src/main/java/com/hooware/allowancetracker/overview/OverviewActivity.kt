package com.hooware.allowancetracker.overview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.AuthActivity
import com.hooware.allowancetracker.children.ChildDataItem
import com.hooware.allowancetracker.databinding.ActivityOverviewBinding
import com.hooware.allowancetracker.transactions.TransactionDataItem
import timber.log.Timber
import org.koin.androidx.viewmodel.ext.android.viewModel

class OverviewActivity : AppCompatActivity() {

    private val viewModel: OverviewViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityOverviewBinding = DataBindingUtil.setContentView(this, R.layout.activity_overview)
        binding.viewModel = viewModel
        viewModel.loadChildren()

        val bundle = this.intent.extras
        if (bundle != null) {
            viewModel.processBundle(bundle)
        }

        viewModel.addQuoteImage(binding.quoteBackground, viewModel.quoteResponse.value!!.backgroundImage)
        viewModel.authenticationState.observe(this, { authenticationState ->
            when (authenticationState) {
                AllowanceApp.AuthenticationState.AUTHENTICATED -> Timber.i("Authenticated")
                else -> {
                    Timber.i("User logged out, returning to Auth Activity")
                    val authActivityIntent = Intent(this, AuthActivity::class.java)
                    authActivityIntent.putExtra("logging_out", true)
                    authActivityIntent.putExtra("quoteResponse", viewModel.quoteResponse.value)
                    startActivity(authActivityIntent)
                    finishAffinity()
                }
            }
            viewModel.logAuthState()
        })

        viewModel.logAuthState()
    }

    companion object {
        fun newIntent(context: Context, child: ChildDataItem, transaction: TransactionDataItem): Intent {
            val intent = Intent(context, OverviewActivity::class.java)
            intent.putExtra("ChildDataItem", child)
            intent.putExtra("TransactionDataItem", transaction)
            return intent
        }
    }
}