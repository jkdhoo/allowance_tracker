package com.hooware.allowancetracker.auth

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import timber.log.Timber

/**
 * This class observes the current FirebaseUser. If there is no logged in user, FirebaseUser will
 * be null.
 *
 * Note that onActive() and onInactive() will get triggered when the configuration changes (for
 * example when the device is rotated). This may be undesirable or expensive depending on the
 * nature of your LiveData object.
 */
class FirebaseRealtimeDatabase : LiveData<DatabaseReference?>() {

    private val realtimeDatabase = Firebase.database

    private val transactions = realtimeDatabase.getReference("transactions")

    init {
        transactions.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue<String>()
                Timber.i("Value is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Timber.i("Failed to read value: ${error.toException()}")
            }
        })
    }
}