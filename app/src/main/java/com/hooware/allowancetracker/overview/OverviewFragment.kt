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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.FirebaseUserLiveData
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_overview, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.overview))

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity(requireActivity())
            }
        })

        viewModel.kidsDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                viewModel.loadKids()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.i("loadKids:onCancelled ${databaseError.toException()}")
            }
        })

        viewModel.chatDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                viewModel.loadChat()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.i("loadChat:onCancelled ${databaseError.toException()}")
            }
        })

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

        binding.chatSubmitButton.setOnClickListener {
            val inputManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(binding.editTextChat.windowToken, 0)
            val message = binding.editTextChat.text.toString()
            if (message.isNotEmpty()) {
                viewModel.saveChatItem(message)
                binding.editTextChat.text.clear()
            }
        }

        FirebaseUserLiveData().observe(viewLifecycleOwner, { user ->
            HandleFirebaseUserLiveDataChange.execute(user, activity, viewModel)
        })

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.i("Fetching FCM registration token failed: ${task.exception}")
                return@OnCompleteListener
            }

            HandleSaveFCMToken.execute(FirebaseUserLiveData().value, task.result, viewModel)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        binding.overviewConstraintLayout.fadeIn()
    }

    private fun setupRecyclerView() {
        val adapter = ChildrenListAdapter { selectedChild: ChildTO, view: View ->
            val endLocation = IntArray(2)
            val startLocation = IntArray(2)
            binding.root.getLocationInWindow(endLocation)
            view.getLocationInWindow(startLocation)

            binding.childrenRecyclerView.removeView(view)
            binding.overviewRelativeLayout.addView(view)


            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = startLocation[1] - endLocation[1]
            params.marginEnd = 22.dpToInt(context)
            params.marginStart = 22.dpToInt(context)
            view.layoutParams = params

            val margin = 16F.dpToFloat(context)
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

            animator.start()
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

    override fun onPause() {
        super.onPause()
        viewModel.reset()
    }

    override fun onResume() {
        super.onResume()
        viewModel.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseUserLiveData().removeObservers(this)
    }
}
