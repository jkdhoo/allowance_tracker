package com.hooware.allowancetracker.overview

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.AuthActivity
import com.hooware.allowancetracker.base.BaseFragment
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.children.ChildDataItem
import com.hooware.allowancetracker.children.ChildrenListAdapter
import com.hooware.allowancetracker.databinding.FragmentOverviewBinding
import com.hooware.allowancetracker.transactions.TransactionDataItem
import com.hooware.allowancetracker.transactions.TransactionDetailsFragmentArgs
import com.hooware.allowancetracker.utils.setDisplayHomeAsUpEnabled
import com.hooware.allowancetracker.utils.setTitle
import com.hooware.allowancetracker.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class OverviewFragment : BaseFragment() {

    override val _viewModel: OverviewViewModel by viewModel()
    private lateinit var binding: FragmentOverviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_overview, container, false)
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)

        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        Timber.i("Observe Authentication State")
        _viewModel.authenticationState.observe(this.requireActivity(), { authenticationState ->
            when (authenticationState) {
                OverviewViewModel.AuthenticationState.AUTHENTICATED -> Timber.i("Authenticated")
                else -> {
                    Timber.i("User logged out, returning to Auth Activity")
                    val authActivityIntent = Intent(this.requireContext(), AuthActivity::class.java)
                    authActivityIntent.putExtra("fresh_auth_start", false)
                    startActivity(authActivityIntent)
                }
            }
        })
        _viewModel.refreshQuotes(binding.quoteBackground)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addChildFAB.setOnClickListener { navigateToAddChild() }
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishAffinity(requireActivity())
                }
            })

        val activity = this.activity as OverviewActivity
        val bundle = activity.getIntentData()
        if (bundle != null) {
            Timber.i("Bundle is not null, $bundle")
            val child = bundle.get("ChildDataItem") as ChildDataItem
            Timber.i("$child")
            _viewModel.navigationCommand.postValue(NavigationCommand.To(
                OverviewFragmentDirections.actionShowDetail(
                    child
                )
            ))
        }
    }


    override fun onResume() {
        super.onResume()
        _viewModel.loadChildren()
    }

    private fun navigateToAddChild() {
        _viewModel.navigationCommand.postValue(NavigationCommand.To(OverviewFragmentDirections.actionAddChild()))
    }

    private fun setupRecyclerView() {
        val adapter = ChildrenListAdapter { selectedChild ->
            _viewModel.editChildDetails.value = false
            _viewModel.navigationCommand.postValue(
                NavigationCommand.To(
                    OverviewFragmentDirections.actionShowDetail(selectedChild)
                )
            )
        }

        binding.childrenRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance().signOut(requireContext())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }
}
