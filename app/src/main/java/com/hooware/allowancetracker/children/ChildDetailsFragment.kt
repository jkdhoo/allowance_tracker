package com.hooware.allowancetracker.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseFragment
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.databinding.FragmentChildDetailsBinding
import com.hooware.allowancetracker.overview.OverviewViewModel
import com.hooware.allowancetracker.transactions.TransactionsListAdapter
import com.hooware.allowancetracker.utils.setDisplayHomeAsUpEnabled
import com.hooware.allowancetracker.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ChildDetailsFragment : BaseFragment() {

    override val _viewModel: OverviewViewModel by viewModel()
    private lateinit var binding: FragmentChildDetailsBinding
    lateinit var selectedChild: ChildDataItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_child_details, container, false)
        setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        selectedChild = ChildDetailsFragmentArgs.fromBundle(requireArguments()).selectedChild
        binding.selectedChild = selectedChild
        binding.saveEditChild.setOnClickListener {
            val child = binding.selectedChild!!
            val name = child.name
            val age = child.age
            val birthday = child.birthday
            val id = child.id
            val newChild = ChildDataItem(
                name,
                age,
                birthday,
                id
            )
            _viewModel.validateAndUpdateChild(newChild)
            binding.selectedChild = newChild
            Timber.i("Save Clicked")
        }
        binding.refreshLayout.setOnRefreshListener {
            _viewModel.loadTransactions(selectedChild.id)
            if (binding.refreshLayout.isRefreshing) {
                binding.refreshLayout.isRefreshing = false
            }
        }
        binding.initiateEditChild.setOnClickListener {
            _viewModel.editChildDetails.value = true
        }
        binding.addTransactionFAB.setOnClickListener {
            navigateToAddTransaction()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = TransactionsListAdapter { selectedTransaction ->
            _viewModel.navigationCommand.postValue(
                NavigationCommand.To(
                    ChildDetailsFragmentDirections.actionShowDetail(
                        selectedTransaction,
                        selectedChild
                    )
                )
            )
        }
        binding.transactionsRecyclerView.setup(adapter)
    }

    private fun navigateToAddTransaction() {
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ChildDetailsFragmentDirections.actionAddTransaction(
                    selectedChild
                )
            )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            Timber.i("Navigate back to OverviewFragment")
            _viewModel.navigationCommand.value = NavigationCommand.Back
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        _viewModel.loadTransactions(selectedChild.id)
    }
}
