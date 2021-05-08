package com.hooware.allowancetracker.transactions

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillValue
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseFragment
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.databinding.FragmentSaveTransactionBinding
import com.hooware.allowancetracker.to.TransactionTO
import com.hooware.allowancetracker.utils.setDisplayHomeAsUpEnabled
import com.hooware.allowancetracker.utils.setTitle
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class SaveTransactionFragment : BaseFragment() {

    override val viewModel by sharedViewModel<TransactionsViewModel>()
    private lateinit var binding: FragmentSaveTransactionBinding
    private var animationEndLocation: String? = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_save_transaction, container, false)
        setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        binding.savingTransaction = TransactionTO()
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            Timber.i("Navigate back to OverviewFragment")
            viewModel.navigationCommand.value = NavigationCommand.Back
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
