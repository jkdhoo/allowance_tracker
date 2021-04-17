package com.hooware.allowancetracker.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.FirebaseUserLiveData
import com.hooware.allowancetracker.base.BaseFragment
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.to.ChildTO
import com.hooware.allowancetracker.databinding.FragmentTransactionsBinding
import com.hooware.allowancetracker.utils.setDisplayHomeAsUpEnabled
import com.hooware.allowancetracker.utils.setTitle
import com.hooware.allowancetracker.utils.setup
import com.hooware.allowancetracker.utils.setupTransactionsList
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class TransactionsFragment : BaseFragment() {

    override val viewModel by sharedViewModel<TransactionsViewModel>()
    private lateinit var binding: FragmentTransactionsBinding
    lateinit var selectedChild: ChildTO

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_transactions, container, false)
        setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        setTitle(getString(R.string.transactions_title))
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.child.value = TransactionsFragmentArgs.fromBundle(requireArguments()).child
        selectedChild = TransactionsFragmentArgs.fromBundle(requireArguments()).child
        binding.child = viewModel.child.value
//        binding.saveEditChild.setOnClickListener {
//            val child = binding.selectedChild!!
//            val name = child.name
//            val age = child.age
//            val birthday = child.birthday
//            val id = child.id
//            val newChild = ChildTO(
//                name,
//                age,
//                birthday,
//                id
//            )
//            viewModel.validateAndUpdateChild(newChild)
//            binding.selectedChild = newChild
//            Timber.i("Save Clicked")
//        }
//        binding.refreshLayout.setOnRefreshListener {
//            viewModel.loadTransactions(selectedChild.id)
//            if (binding.refreshLayout.isRefreshing) {
//                binding.refreshLayout.isRefreshing = false
//            }
//        }
//        binding.initiateEditChild.setOnClickListener {
//            viewModel.editChildDetails.value = true
//        }
        binding.addTransactionFAB.setOnClickListener {
            navigateToAddTransaction()
        }

        viewModel.showLoading.observe(viewLifecycleOwner, { showLoading ->
            binding.progressBar.isVisible = showLoading
        })

        FirebaseUserLiveData().observe(viewLifecycleOwner, { user ->
            if (user != null) {
                viewModel.firebaseUID.value = user.uid
                Timber.i("${viewModel.firebaseUID.value}")
                binding.addTransactionFAB.isVisible = viewModel.child.value!!.id != user.uid
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = TransactionsListAdapter<Any> { selectedTransaction ->
            viewModel.navigationCommand.postValue(
                NavigationCommand.To(
                    TransactionsFragmentDirections.actionShowDetail(
                        selectedTransaction,
                        selectedChild
                    )
                )
            )
        }
        binding.transactionsRecyclerView.setupTransactionsList(adapter)
    }

    private fun navigateToAddTransaction() {
        viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                TransactionsFragmentDirections.actionAddTransaction(
                    selectedChild
                )
            )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            Timber.i("Navigate back to OverviewFragment")
            viewModel.navigationCommand.value = NavigationCommand.Back
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        viewModel.resetTransactions()
        viewModel.loadTransactions(selectedChild.id)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetTransactions()
        FirebaseUserLiveData().removeObservers(this)
    }
}
