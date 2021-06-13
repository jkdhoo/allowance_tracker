package com.hooware.allowancetracker.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseFragment
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.databinding.FragmentNotificationsBinding
import com.hooware.allowancetracker.to.FCMRequestInputTO
import com.hooware.allowancetracker.transactions.TransactionsViewModel
import com.hooware.allowancetracker.utils.SendFCMNotification
import com.hooware.allowancetracker.utils.fadeOut
import com.hooware.allowancetracker.utils.setDisplayHomeAsUpEnabled
import com.hooware.allowancetracker.utils.setTitle
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class NotificationsFragment : BaseFragment() {

    override val viewModel by sharedViewModel<TransactionsViewModel>()
    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var app: AllowanceApp

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        app = requireActivity().application as AllowanceApp
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notifications, container, false)
        setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        setTitle(getString(R.string.transactions_title))
        binding.lifecycleOwner = this
        binding.requestInputTO = FCMRequestInputTO()
        binding.viewModel = viewModel

        binding.sendNotificationButton.setOnClickListener {
            val body = binding.body.text.toString()
            val title = "From ${app.authType.value?.simpleName}: ${binding.title.text}"
            val uid = viewModel.child.value?.id ?: ""
            viewModel.notificationDatabase.child(uid).get().addOnSuccessListener { snapshot ->
                val token = snapshot.value.toString()
                SendFCMNotification.execute(token = token, body = body, title = title)
                viewModel.navigationCommand.value = NavigationCommand.Back
            }
        }

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {

            Timber.i("Navigate back to OverviewFragment")
            binding.transactionsRelativeLayout.fadeOut()
            viewModel.navigationCommand.value = NavigationCommand.Back
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
