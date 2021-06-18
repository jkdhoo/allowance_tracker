package com.hooware.allowancetracker.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object QuoteNetworkService {

    private const val BASE_URL = "https://quotes.rest/"

    private val retrofitQuote = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val quote: QuoteService = retrofitQuote.create(QuoteService::class.java)
}