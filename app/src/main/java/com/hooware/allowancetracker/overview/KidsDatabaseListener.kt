package com.hooware.allowancetracker.overview

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class KidsDatabaseListener(var viewModel: OverviewViewModel) : ValueEventListener {
    override fun onDataChange(dataSnapshot: DataSnapshot) {
        viewModel.kidsLoaded.value = false
        Timber.i("loading Kids")
        viewModel.loadKids()
    }

    override fun onCancelled(databaseError: DatabaseError) {
        Timber.i("loadKids:onCancelled ${databaseError.toException()}")
    }
}