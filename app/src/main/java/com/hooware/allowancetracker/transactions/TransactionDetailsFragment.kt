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
import com.hooware.allowancetracker.children.ChildrenListAdapter
import com.hooware.allowancetracker.databinding.FragmentChildDetailsBinding
import com.hooware.allowancetracker.databinding.FragmentTransactionDetailsBinding
import com.hooware.allowancetracker.overview.OverviewFragmentDirections
import com.hooware.allowancetracker.overview.OverviewViewModel
import com.hooware.allowancetracker.utils.setDisplayHomeAsUpEnabled
import com.hooware.allowancetracker.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class TransactionDetailsFragment : BaseFragment() {

    override val _viewModel: OverviewViewModel by viewModel()
    private lateinit var binding: FragmentTransactionDetailsBinding

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
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
//        val selectedTransaction = TransactionDetailsFragmentArgs.fromBundle(requireArguments()).selectedTransaction
//        binding.selectedTransaction = selectedTransaction
//        binding.saveChild.setOnClickListener {
//            val child = binding.selectedChild!!
//            val name = child.name
//            val age = child.age
//            val birthday = child.birthday
//            val id = child.id
//            _viewModel.validateAndUpdateChild(
//                ChildDataItem(
//                    name,
//                    age,
//                    birthday,
//                    id
//                )
//            )
//            Timber.i("Save Clicked")
//        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
    }

//    private fun setupRecyclerView() {
//        val adapter = TransactionsListAdapter { selectedTransaction ->
//            _viewModel.navigationCommand.postValue(NavigationCommand.To(ChildDetailsFragmentDirections.actionShowDetail(selectedTransaction)))
//        }
//        binding.transactionsRecyclerView.setup(adapter)
//    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            Timber.i("Navigate back to OverviewFragment")
            _viewModel.navigationCommand.value = NavigationCommand.Back
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
