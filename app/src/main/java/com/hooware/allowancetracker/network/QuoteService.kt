package com.hooware.allowancetracker.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface QuoteService {

    @GET("qod")
    fun getQuoteAsync(
        @Query("language") language: String,
        @Query("category") category: String
    ): Deferred<String>
}

object Network {

    const val BASE_URL = "https://quotes.rest/"

    private val retrofitQuote = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val quote: QuoteService = retrofitQuote.create(QuoteService::class.java)
}