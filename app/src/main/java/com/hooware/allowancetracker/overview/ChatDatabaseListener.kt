package com.hooware.allowancetracker.overview

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class ChatDatabaseListener(var viewModel: OverviewViewModel) : ValueEventListener {
    override fun onDataChange(dataSnapshot: DataSnapshot) {
        viewModel.chatLoaded.value = false
        viewModel.loadChat()
    }

    override fun onCancelled(databaseError: DatabaseError) {
        Timber.i("loadChat:onCancelled ${databaseError.toException()}")
    }
}