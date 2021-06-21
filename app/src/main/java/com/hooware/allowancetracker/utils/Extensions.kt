package com.hooware.allowancetracker.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hooware.allowancetracker.base.BaseRecyclerViewAdapter
import com.hooware.allowancetracker.children.ChatListAdapter
import timber.log.Timber
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


fun <T> RecyclerView.setup(adapter: BaseRecyclerViewAdapter<T>) {
    this.apply {
        layoutManager = LinearLayoutManager(this.context)
        this.adapter = adapter
        isMotionEventSplittingEnabled = false
    }
}

fun RecyclerView.setupChat(adapter: ChatListAdapter) {
    this.apply {
        layoutManager = LinearLayoutManager(this.context)
        (layoutManager as LinearLayoutManager).stackFromEnd = true
        this.adapter = adapter
        isMotionEventSplittingEnabled = false
        viewTreeObserver.addOnGlobalLayoutListener {
            layoutManager?.smoothScrollToPosition(this, null, this.bottom)
        }
    }
}

fun Fragment.setTitle(title: String) {
    if (activity is AppCompatActivity) {
        (activity as AppCompatActivity).supportActionBar?.title = title
    }
}

fun Fragment.setDisplayHomeAsUpEnabled(bool: Boolean) {
    if (activity is AppCompatActivity) {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
            bool
        )
    }
}

//animate changing the view visibility
fun View.fadeIn() {
    this.visibility = View.VISIBLE
    this.alpha = 0f
    this.animate().alpha(1f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@fadeIn.alpha = 1f
        }
    })
}

//animate changing the view visibility
fun View.fadeOut() {
    this.animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@fadeOut.alpha = 1f
            this@fadeOut.visibility = View.GONE
        }
    })
}

fun View.fadeOutInvisible() {
    this.animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@fadeOutInvisible.alpha = 1f
            this@fadeOutInvisible.visibility = View.INVISIBLE
        }
    })
}

fun Double.currencyFormatter(): Double? {
    return try {
        val format = NumberFormat.getInstance()
        format.maximumFractionDigits = 2
        Timber.i(format.format(this))
        format.format(this).toDouble()
    } catch (ex: Exception) {
        Timber.i(ex)
        null
    }
}

val Date.age: String
    get() {
        val calendar = Calendar.getInstance()
        calendar.time = Date(time - Date().time)
        return (1970 - (calendar.get(Calendar.YEAR) + 1)).toString()
    }

val Long.toTimestamp: String
    get() {
        return SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault()).format(this)
    }

val Double.toCurrency: String
    get() {
        return try {
            "$${DecimalFormat("#,##0.00").format(this)}"
        } catch (ex: Exception) {
            "$0.00"
        }
    }

fun Int.dpToInt(context: Context?): Int {
    return this * (context?.resources?.displayMetrics?.density?.toInt() ?: 0)
}

fun Int.dpToFloat(context: Context?): Float {
    return this.toFloat() * (context?.resources?.displayMetrics?.density ?: 0F)
}