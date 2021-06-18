package com.hooware.allowancetracker.network

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface QuoteService {
    @GET("qod")
    fun getQuoteAsync(@Query("language") language: String, @Query("category") category: String): Deferred<String>
}