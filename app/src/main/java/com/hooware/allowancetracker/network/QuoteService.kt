package com.hooware.allowancetracker.network

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Deferred
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@Parcelize
@Entity(tableName = "quote")
data class QuoteResponseTO(
    @PrimaryKey @ColumnInfo(name = "quote") var quote: String,
    @ColumnInfo(name = "author") var author: String,
    @ColumnInfo(name = "backgroundImage") var backgroundImage: String
): Parcelable

fun parseQuoteJsonResult(jsonResult: JSONObject): QuoteResponseTO {
    val resultContents = jsonResult.getJSONObject("contents")
        .getJSONArray("quotes")
        .getJSONObject(0)
    val resultQuote = resultContents.getString("quote")
    val resultAuthor = resultContents.getString("author")
    val resultBackground = resultContents.getString("background")
    return QuoteResponseTO(resultQuote, resultAuthor, resultBackground)
}

interface QuoteService {

    @GET("qod")
    fun getQuoteAsync(
        @Query("language") language: String,
        @Query("category") category: String
    ): Deferred<String>
}

object Network {

    private const val BASE_URL = "https://quotes.rest/"

    private val retrofitQuote = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val quote: QuoteService = retrofitQuote.create(QuoteService::class.java)
}