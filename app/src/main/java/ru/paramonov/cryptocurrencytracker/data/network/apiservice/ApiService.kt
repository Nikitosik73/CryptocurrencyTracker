package ru.paramonov.cryptocurrencytracker.data.network.apiservice

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.paramonov.cryptocurrencytracker.data.network.model.coininfo.CoinInfoJsonContainerDto
import ru.paramonov.cryptocurrencytracker.data.network.model.coinname.CoinNamesListDto
import ru.paramonov.cryptocurrencytracker.data.network.model.graphic.Result

interface ApiService {

    @GET("top/totalvolfull")
    suspend fun getTopCoinsInfo(
        @Query(QUERY_PARAM_API_KEY) apiKey: String = "7180c5be373263271be28be7dffa06f6d9b3e8f5129d7904cc93f5b1c423a9ea",
        @Query(QUERY_PARAM_LIMIT) limit: Int = 10,
        @Query(QUERY_PARAM_TO_SYMBOL) tSum: String = CURRENCY
    ): CoinNamesListDto

    @GET("pricemultifull")
    suspend fun getFullPriceList(
        @Query(QUERY_PARAM_API_KEY) apiKey: String = "7180c5be373263271be28be7dffa06f6d9b3e8f5129d7904cc93f5b1c423a9ea",
        @Query(QUERY_PARAM_FROM_SYMBOLS) fSyms: String,
        @Query(QUERY_PARAM_TO_SYMBOLS) tSyms: String = CURRENCY
    ): CoinInfoJsonContainerDto

    @GET("aggs/ticker/AAPL/range/{$TIME_FRAME}/2023-01-01/2024-06-20?adjusted=true&sort=desc&limit=50000&apiKey=fRrpu3FjJixOINdzFcgmIKJCNZWfFaPr")
    suspend fun loadBars(
        @Path(TIME_FRAME) timeFrame: String
    ): Result

    companion object {
        private const val QUERY_PARAM_LIMIT = "limit"
        private const val QUERY_PARAM_API_KEY = "api_key"
        private const val QUERY_PARAM_TO_SYMBOL = "tsym"
        private const val QUERY_PARAM_TO_SYMBOLS = "tsyms"
        private const val QUERY_PARAM_FROM_SYMBOLS = "fsyms"
        private const val TIME_FRAME = "timeFrame"

        private const val CURRENCY = "USD"

        private const val LANGUAGE = "EN"
    }
}