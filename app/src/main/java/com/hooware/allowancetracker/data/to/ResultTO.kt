package com.hooware.allowancetracker.data.to

/**
 * A sealed class that encapsulates successful outcome with a value of type [T]
 * or a failure with message and statusCode
 */
sealed class ResultTO<out T : Any> {
    data class Success<out T : Any>(val data: T) : ResultTO<T>()
    data class Error(val message: String?, val statusCode: Int? = null) :
        ResultTO<Nothing>()
}