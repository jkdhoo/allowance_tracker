package com.hooware.allowancetracker.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseFragment
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.children.ChildDataItem
import com.hooware.allowancetracker.databinding.FragmentTransactionDetailsBinding
import com.hooware.allowancetracker.overview.OverviewViewModel
import com.hooware.allowancetracker.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class TransactionDetailsFragment : BaseFragment() {

    override val viewModel: OverviewViewModel by viewModel()
    private lateinit var binding: FragmentTransactionDetailsBinding
    lateinit var selectedChild: ChildDataItem
    lateinit var selectedTransaction: TransactionDataItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_transaction_details,
                container,
                false
            )

        setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        selectedChild = TransactionDetailsFragmentArgs.fromBundle(requireArguments()).selectedChild
        binding.selectedChild = selectedChild

        selectedTransaction =
            TransactionDetailsFragmentArgs.fromBundle(requireArguments()).selectedTransaction
        binding.selectedTransaction = selectedTransaction
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
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
