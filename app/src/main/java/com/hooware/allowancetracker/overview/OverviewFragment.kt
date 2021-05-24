package com.hooware.allowancetracker.overview

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.animation.addListener
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.AuthActivity
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

    @RequiresApi(Build.VERSION_CODES.O)
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
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                viewModel.loadKids()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.i("loadKids:onCancelled ${databaseError.toException()}")
            }
        })

        viewModel.chatDatabase.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                viewModel.loadChat()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.i("loadChat:onCancelled ${databaseError.toException()}")
            }
        })

        viewModel.quoteLoaded.observe(viewLifecycleOwner, { quoteLoaded ->
            if (quoteLoaded) {
                viewModel.displayQuoteImage(binding.quoteBackground)
            }
        })

        viewModel.showLoading.observe(viewLifecycleOwner, { showLoading ->
            binding.progressBar.isVisible = showLoading
        })

        viewModel.chatList.observe(viewLifecycleOwner, { chatList ->
            if (!chatList.isNullOrEmpty()) {
                insertChatContent()
                binding.chatItemLayout.invalidate()
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

        FirebaseUserLiveData().observe(viewLifecycleOwner, { user ->
            if (user == null) {
                Timber.i("Not authenticated. Authenticating...")
                val intent = Intent(requireActivity(), AuthActivity::class.java)
                viewModel.reset()
                startActivity(intent)
                this.activity?.finish()
            } else {
                Timber.i("Authenticated - ${user.uid}")
                viewModel.setFirebaseUID(user.uid)
            }
        })

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.i("Fetching FCM registration token failed: ${task.exception}")
                return@OnCompleteListener
            }

            viewModel.saveFCMToken(task.result)
        })

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        binding.overviewRelativeLayout.fadeIn()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecyclerView() {
        val adapter = ChildrenListAdapter { selectedChild: ChildTO, view: View ->
            val endLocation = IntArray(2)
            val startLocation = IntArray(2)
            binding.quoteBackground.getLocationInWindow(endLocation)
            view.getLocationInWindow(startLocation)

            binding.childrenRecyclerView.removeView(view)
            binding.overviewRelativeLayout.addView(view)

            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = startLocation[1] - endLocation[1]
            view.layoutParams = params

            val margin = 10F * (context?.resources?.displayMetrics?.density ?: 0F)
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

    override fun onDestroy() {
        super.onDestroy()
        FirebaseUserLiveData().removeObservers(this)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun insertChatContent() {
        binding.chatItemLayout.removeAllViews()
        viewModel.chatList.value?.forEach { chatItem ->
            val view = layoutInflater.inflate(R.layout.it_chat, null)
            val name = view.findViewById(R.id.name) as TextView
            name.text = "${chatItem.second}: ${chatItem.first.toLong().toTimestamp}"
            name.setTextColor(chooseColorByName(name, chatItem.second))

            val chat = view.findViewById(R.id.chat) as TextView
            chat.text = chatItem.third
            binding.chatItemLayout.addView(view)
        }
        binding.chatItemLayout.gravity = Gravity.TOP
        binding.chatItemLayout.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun chooseColorByName(view: TextView, name: String) : Int {
        return when (name) {
            "Laa" -> view.context.resources.getColor(R.color.laa, view.context.resources.newTheme())
            "Levi" -> view.context.resources.getColor(R.color.levi, view.context.resources.newTheme())
            "Mom" -> view.context.resources.getColor(R.color.mom, view.context.resources.newTheme())
            "Dad" -> view.context.resources.getColor(R.color.dad, view.context.resources.newTheme())
            "System" -> view.context.resources.getColor(R.color.black, view.context.resources.newTheme())
            else -> 0
        }
    }
}
