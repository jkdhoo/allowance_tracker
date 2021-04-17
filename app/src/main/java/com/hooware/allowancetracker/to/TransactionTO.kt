package com.hooware.allowancetracker.to

import android.os.Parcelable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import kotlinx.parcelize.Parcelize
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Immutable model class for a Reminder. In order to compile with Room
 *
 * @param _name         title of the reminder
 * @param _details   description of the reminder
 * @param _amount      location name of the reminder
 * @param _date      latitude of the reminder location
 * @param _id          id of the reminder
 */

@Parcelize
class TransactionTO(
    var name: String = "",
    var details: String = "",
    var amount: String = "",
    val date: String = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Calendar.getInstance().time),
    val id: String = UUID.randomUUID().toString()
) : Parcelable, BaseObservable()

// {
//
//    val name: String
//        @Bindable get() = _name
//
//    var details: String
//        @Bindable get() = _details
//        @Bindable set(value) {
//            _details = value
//            notifyPropertyChanged(BR.details)
//        }
//
//    var amount: String
//        @Bindable get() = _amount
//        @Bindable set(value) {
//            _amount = value
//            notifyPropertyChanged(BR.amount)
//        }
//
//    val date: String
//        get() = _date
//
//    val id: String
//        get() = _id
//}
//
//class Note : BaseObservable() {
//
//    var title = ""
//    private var category = ""
//    val date = getFormattedDate()
//    var details = ""
//    private var savable = false
//
//    @Bindable
//    fun getNoteTitle(): String {
//        return title
//    }
//
//    @Bindable
//    fun getNoteCategory(): String {
//        return category
//    }
//
//    @Bindable
//    fun getNoteDetails(): String {
//        return details
//    }
//
//    @Bindable
//    fun getNoteSavable(): Boolean {
//        return savable
//    }
//
//    fun setNoteTitle(title: String) {
//        if (this.title != title) {
//            this.title = title
//            notifyPropertyChanged(BR.noteTitle)
//        }
//    }
//
//    fun setNoteCategory(category: String) {
//        if (this.category != category) {
//            this.category = category
//            notifyPropertyChanged(BR.noteCategory)
//        }
//    }
//
//    fun setNoteDetails(details: String) {
//        if (this.details != details) {
//            this.details = details
//            notifyPropertyChanged(BR.noteDetails)
//        }
//        checkSavable(details)
//    }
//
//    private fun checkSavable(details: String) {
//        if (this.savable != details.isNotEmpty()) {
//            this.savable = details.isNotEmpty()
//            notifyPropertyChanged(BR.noteSavable)
//        }
//    }
//
//    fun getFinalNote(): Note {
//        return this
//    }
//}