package com.hooware.allowancetracker.transactions

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.AuthActivity
import com.hooware.allowancetracker.auth.FirebaseUserLiveData
import com.hooware.allowancetracker.base.BaseFragment
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.databinding.FragmentTransactionsBinding
import com.hooware.allowancetracker.utils.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class TransactionsFragment : BaseFragment() {

    override val viewModel by sharedViewModel<TransactionsViewModel>()
    private lateinit var binding: FragmentTransactionsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_transactions, container, false)
        setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        setTitle(getString(R.string.transactions_title))
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.child.value = TransactionsFragmentArgs.fromBundle(requireArguments()).child
        viewModel.savingsOwedUpdated.observe(viewLifecycleOwner, { isUpdated ->
            if (isUpdated) {
                binding.itSavings.text = viewModel.child.value?.savingsOwed?.toCurrency
                viewModel.resetSavingsOwedUpdated()
            }
        })
        viewModel.totalSpendingUpdated.observe(viewLifecycleOwner, { isUpdated ->
            if (isUpdated) {
                binding.itBalance.text = viewModel.child.value?.totalSpending?.toCurrency
                viewModel.resetTotalSpendingUpdated()
            }
        })
        binding.addTransactionFAB.setOnClickListener {
            viewModel.disableSavings.value = false
            viewModel.saveTransactionTotalHolder.value = ""
            navigateToAddTransaction()
        }

        viewModel.showLoading.observe(viewLifecycleOwner, { showLoading ->
            binding.progressBar.isVisible = showLoading
        })

        FirebaseUserLiveData().observe(viewLifecycleOwner, { user ->
            if (user != null) {
                viewModel.setFirebaseUID(user.uid)
                binding.addTransactionFAB.isVisible = viewModel.child.value?.id != user.uid
                binding.resetSavings.isVisible = viewModel.child.value?.id != user.uid
            } else {
                Timber.i("Not authenticated. Authenticating...")
                val intent = Intent(requireActivity(), AuthActivity::class.java)
                startActivity(intent)
                this.activity?.finish()
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = TransactionsListAdapter { selectedTransaction, view: View ->
            if (viewModel.child.value?.id == viewModel.firebaseUID.value) {
                Timber.i("Not navigating, child")
            } else {
                val endLocation = IntArray(2)
                val startLocation = IntArray(2)
                binding.transactionsRelativeLayout.getLocationInWindow(endLocation)
                view.getLocationInWindow(startLocation)

                binding.transactionsRecyclerView.removeView(view)

                val params = view.layoutParams as ViewGroup.MarginLayoutParams
                params.topMargin = startLocation[1] - endLocation[1]
                view.layoutParams = params

                binding.transactionsRelativeLayoutPlaceholder.addView(view)


                binding.transactionsRelativeLayoutPlaceholder.fadeIn()
                binding.transactionsRecyclerView.fadeOutInvisible()
                binding.resetSavings.fadeOutInvisible()
                binding.reminderCardView.fadeOutInvisible()

                val margin = 10F * (context?.resources?.displayMetrics?.density ?: 0F)
                val animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, endLocation[1].toFloat() - startLocation[1].toFloat() + margin)
                animator.duration = 1000
                animator.addListener(onEnd = {
                    viewModel.navigationCommand.postValue(
                        NavigationCommand.To(
                            TransactionsFragmentDirections.actionShowTransactionDetail(selectedTransaction)
                        )
                    )
                })

                animator.start()
            }
        }
        binding.transactionsRecyclerView.setup(adapter)
    }

    private fun navigateToAddTransaction() {
        viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                TransactionsFragmentDirections.actionAddTransaction()
            )
        )
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

    override fun onResume() {
        super.onResume()
        viewModel.loadTransactions()
    }

    override fun onPause() {
        super.onPause()
        viewModel.resetTransactionsLoaded()
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseUserLiveData().removeObservers(this)
    }
}
