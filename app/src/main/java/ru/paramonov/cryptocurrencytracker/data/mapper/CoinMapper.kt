package ru.paramonov.cryptocurrencytracker.data.mapper

import com.google.gson.Gson
import ru.paramonov.cryptocurrencytracker.data.database.model.CoinInfoDBO
import ru.paramonov.cryptocurrencytracker.data.network.model.coininfo.CoinInfoDto
import ru.paramonov.cryptocurrencytracker.data.network.model.coininfo.CoinInfoJsonContainerDto
import ru.paramonov.cryptocurrencytracker.data.network.model.coinname.CoinNamesListDto
import ru.paramonov.cryptocurrencytracker.domain.entity.CoinInfo
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class CoinMapper @Inject constructor() {

    fun mapDtoToDbModel(dto: CoinInfoDto) = CoinInfoDBO(
        fromSymbol = dto.fromSymbol,
        toSymbol = dto.toSymbol,
        price = dto.price,
        highDay = dto.highDay,
        lowDay = dto.lowDay,
        lastUpdate = dto.lastUpdate,
        lastMarket = dto.lastMarket,
        imageUrl = BASE_IMAGE_URL + dto.imageUrl
    )

    fun mapJsonContainerToDto(jsonContainer: CoinInfoJsonContainerDto): List<CoinInfoDto> {
        val listDto = mutableListOf<CoinInfoDto>()
        val jsonObject = jsonContainer.json ?: return listDto
        val coinKeySet = jsonObject.keySet()
        for(coinKey in coinKeySet) {
            val currencyJsonObject = jsonObject.getAsJsonObject(coinKey)
            val currencyKeySet = currencyJsonObject.keySet()
            for (currencyKey in currencyKeySet) {
                val coinInfo = Gson().fromJson(
                    currencyJsonObject.getAsJsonObject(currencyKey),
                    CoinInfoDto::class.java
                )
                listDto.add(coinInfo)
            }
        }
        return listDto
    }

    fun mapNamesListToString(namesListDto: CoinNamesListDto): String {
        return namesListDto.names?.map { nameDto ->
            nameDto.coinName?.name
        }?.joinToString(",") ?: ""
    }

    fun mapDbModelToEntity(dbModel: CoinInfoDBO) = CoinInfo(
        fromSymbol = dbModel.fromSymbol,
        toSymbol = dbModel.toSymbol,
        price = dbModel.price,
        highDay = dbModel.highDay,
        lowDay = dbModel.lowDay,
        lastUpdate = convertTimestampToTime(dbModel.lastUpdate),
        lastMarket = dbModel.lastMarket,
        imageUrl = dbModel.imageUrl
    )

    private fun convertTimestampToTime(timestamp: Long?): String {
        if (timestamp == null) return ""
        val stamp = Timestamp(timestamp * 1000)
        val date = Date(stamp.time)
        val pattern = "HH:mm:ss"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        simpleDateFormat.timeZone = TimeZone.getDefault()
        return simpleDateFormat.format(date)
    }

    companion object {
        const val BASE_IMAGE_URL = "https://cryptocompare.com"
    }
}