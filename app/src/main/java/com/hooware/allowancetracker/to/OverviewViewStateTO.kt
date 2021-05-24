package com.hooware.allowancetracker.to

sealed class OverviewViewStateTO {
    class QuoteTO(var quote: QuoteTO?) : OverviewViewStateTO()
}