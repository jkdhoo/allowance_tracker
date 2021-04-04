package com.hooware.allowancetracker.overview

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseFragment
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.children.ChildrenListAdapter
import com.hooware.allowancetracker.databinding.FragmentOverviewBinding
import com.hooware.allowancetracker.utils.setDisplayHomeAsUpEnabled
import com.hooware.allowancetracker.utils.setTitle
import com.hooware.allowancetracker.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class OverviewFragment : BaseFragment() {

    override val viewModel: OverviewViewModel by viewModel()
    private lateinit var binding: FragmentOverviewBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_overview, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.addChildFAB.setOnClickListener { navigateToAddChild() }

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.overview))

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity(requireActivity())
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadChildren()
    }

    private fun navigateToAddChild() {
        viewModel.navigationCommand.postValue(NavigationCommand.To(OverviewFragmentDirections.actionAddChild()))
    }

    private fun setupRecyclerView() {
        val adapter = ChildrenListAdapter { selectedChild ->
            viewModel.editChildDetails.value = false
            viewModel.navigationCommand.postValue(
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
