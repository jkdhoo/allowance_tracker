package com.hooware.allowancetracker.overview

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.addListener
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseFragment
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.children.ChildrenListAdapter
import com.hooware.allowancetracker.databinding.FragmentOverviewBinding
import com.hooware.allowancetracker.to.ChildTO
import com.hooware.allowancetracker.utils.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import kotlin.math.absoluteValue

class OverviewFragment : BaseFragment() {

    override val viewModel: OverviewViewModel by sharedViewModel()
    private lateinit var binding: FragmentOverviewBinding
    private lateinit var kidsListener: KidsDatabaseListener
    private lateinit var chatListener: ChatDatabaseListener
    private var lastClickTime = System.currentTimeMillis()

    private companion object {
        private const val CLICK_TIME_INTERVAL = 300L
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_overview, container, false)
        binding.viewModel = viewModel
        kidsListener = KidsDatabaseListener(viewModel)
        chatListener = ChatDatabaseListener(viewModel)
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.overview))

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity(requireActivity())
            }
        })

        binding.chatSubmitButton.setOnClickListener {
            val inputManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(binding.editTextChat.windowToken, 0)
            val message = binding.editTextChat.text.toString()
            if (message.isNotEmpty()) {
                viewModel.saveChatItem(message)
                binding.editTextChat.text.clear()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        binding.overviewConstraintLayout.fadeIn()
    }

    private fun setupRecyclerView() {
        val adapter = ChildrenListAdapter { selectedChild: ChildTO, view: View ->
            view.isEnabled = false
            val endLocation = IntArray(2)
            val startLocation = IntArray(2)
            binding.root.getLocationInWindow(endLocation)
            view.getLocationInWindow(startLocation)

            binding.childrenRecyclerView.removeView(view)
            binding.overviewRelativeLayout.addView(view)


            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = startLocation[1] - endLocation[1]
            params.marginEnd = 20.dpToInt(context)
            params.marginStart = 20.dpToInt(context)
            view.layoutParams = params

            val margin = 16.dpToFloat(context)
            val animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, endLocation[1].toFloat() - startLocation[1].toFloat() + margin)
            animator.duration = 1000
            animator.addListener(onEnd = {
                viewModel.navigationCommand.postValue(
                    NavigationCommand.To(
                        OverviewFragmentDirections.actionShowDetail(selectedChild, (endLocation[1].toFloat() - startLocation[1].toFloat() + margin).absoluteValue.toString())
                    )
                )
            })

            binding.overviewConstraintLayout.fadeOutInvisible()

            val now = System.currentTimeMillis()
            if (now - lastClickTime < CLICK_TIME_INTERVAL) {
                return@ChildrenListAdapter
            }
            lastClickTime = now
            animator.start()
        }

        binding.childrenRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance().signOut(requireContext())
            }
            R.id.notification_history -> {
                viewModel.navigationCommand.postValue(
                    NavigationCommand.To(
                        OverviewFragmentDirections.actionShowNotificationHistory()
                    )
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onPause() {
        super.onPause()
        viewModel.kidsDatabase.removeEventListener(kidsListener)
        viewModel.chatDatabase.removeEventListener(chatListener)
        viewModel.insertChatContent.removeObservers(this)
        viewModel.displayQuoteImage.removeObservers(this)
        viewModel.showLoading.removeObservers(this)
        viewModel.isOverviewShowing(false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.isOverviewShowing(true)
        viewModel.kidsDatabase.addValueEventListener(kidsListener)
        viewModel.chatDatabase.addValueEventListener(chatListener)
        viewModel.insertChatContent.observe(viewLifecycleOwner, { insertContent ->
            if (insertContent) {
                viewModel.insertChatContent(binding.chatItemLayout, this)
            }
        })
        viewModel.displayQuoteImage.observe(viewLifecycleOwner, { displayImage ->
            if (displayImage) {
                viewModel.displayQuoteImage(binding.quoteBackground)
            }
        })
        viewModel.showLoading.observe(viewLifecycleOwner, { showLoading ->
            binding.progressBar.isVisible = showLoading
        })
    }
}
