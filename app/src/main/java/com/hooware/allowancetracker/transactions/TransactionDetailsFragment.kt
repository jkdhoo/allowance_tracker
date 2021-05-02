package com.hooware.allowancetracker.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.FirebaseUserLiveData
import com.hooware.allowancetracker.base.BaseFragment
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.databinding.FragmentTransactionDetailsBinding
import com.hooware.allowancetracker.utils.setDisplayHomeAsUpEnabled
import com.hooware.allowancetracker.utils.setTitle
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class TransactionDetailsFragment : BaseFragment() {

    override val viewModel by sharedViewModel<TransactionsViewModel>()
    private lateinit var binding: FragmentTransactionDetailsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_transaction_details, container, false)

        setDisplayHomeAsUpEnabled(true)
        setTitle(getString(R.string.transactions_title))
        setHasOptionsMenu(true)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.transaction = TransactionDetailsFragmentArgs.fromBundle(requireArguments()).transaction

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirebaseUserLiveData().observe(viewLifecycleOwner, { user ->
            if (user != null) {
                viewModel.setFirebaseUID(user.uid)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            Timber.i("Navigate back to OverviewFragment")
            viewModel.navigationCommand.value = NavigationCommand.Back
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        viewModel.resetTransactions()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetTransactions()
    }
}
