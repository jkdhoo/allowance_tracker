package com.hooware.allowancetracker.notifications

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseFragment
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.databinding.FragmentNotificationHistoryBinding
import com.hooware.allowancetracker.databinding.FragmentTransactionsBinding
import com.hooware.allowancetracker.overview.OverviewViewModel
import com.hooware.allowancetracker.utils.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class NotificationHistoryFragment : BaseFragment() {

    override val viewModel by sharedViewModel<OverviewViewModel>()
    private lateinit var binding: FragmentNotificationHistoryBinding
    private lateinit var app: AllowanceApp

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        app = requireActivity().application as AllowanceApp
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification_history, container, false)
        setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(false)
        setTitle(getString(R.string.notification_history_title))
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.notificationHistoryLoaded.observe(viewLifecycleOwner, { showLoading ->
            binding.progressBar.isVisible = showLoading
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = NotificationHistoryListAdapter()
        binding.notificationHistoryRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {

            Timber.i("Navigate back to OverviewFragment")
            binding.notificationHistoryRelativeLayout.fadeOut()
            viewModel.navigationCommand.value = NavigationCommand.Back
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadNotificationHistory()
    }

    override fun onPause() {
        super.onPause()
        viewModel.resetNotificationHistoryLoaded()
    }
}
