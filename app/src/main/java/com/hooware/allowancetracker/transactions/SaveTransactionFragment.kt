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
import com.hooware.allowancetracker.children.ChildDetailsFragmentArgs
import com.hooware.allowancetracker.databinding.FragmentSaveChildBinding
import com.hooware.allowancetracker.databinding.FragmentSaveTransactionBinding
import com.hooware.allowancetracker.overview.OverviewViewModel
import com.hooware.allowancetracker.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SaveTransactionFragment : BaseFragment() {

    override val _viewModel: OverviewViewModel by viewModel()
    private lateinit var binding: FragmentSaveTransactionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_transaction, container, false)

        setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        val selectedChild = ChildDetailsFragmentArgs.fromBundle(requireArguments()).selectedChild
        binding.selectedChild = selectedChild
        binding.viewModel = _viewModel
        binding.saveChild.setOnClickListener {
            val name = _viewModel.childName.value
            val age = _viewModel.childAge.value
            val birthday = _viewModel.childBirthday.value
            _viewModel.validateAndSaveChild(
                ChildDataItem(
                    name,
                    age,
                    birthday
                )
            )
            Timber.i("Save Clicked")
        }
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            Timber.i("Navigate back to OverviewFragment")
            _viewModel.navigationCommand.value = NavigationCommand.Back
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
