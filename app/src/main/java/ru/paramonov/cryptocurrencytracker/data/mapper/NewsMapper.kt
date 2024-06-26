package ru.paramonov.cryptocurrencytracker.data.mapper

import ru.paramonov.cryptocurrencytracker.data.database.model.NewsDBO
import ru.paramonov.cryptocurrencytracker.data.network.model.news.NewsContainerDto
import ru.paramonov.cryptocurrencytracker.data.network.model.news.NewsInfoDto
import ru.paramonov.cryptocurrencytracker.data.network.model.news.TitleNewsDto
import ru.paramonov.cryptocurrencytracker.domain.entity.NewsInfo
import javax.inject.Inject

class NewsMapper @Inject constructor() {

    fun mapDtoToDbModel(dto: NewsInfoDto) = NewsDBO(
        id = dto.id,
        guid = dto.guid,
        imageUrl = dto.imageUrl,
        title = dto.title,
        body = dto.body,
        nameNews = mapDtoTitleNewsToString(TitleNewsDto(dto.titleNews.name))
    )

    private fun mapDtoTitleNewsToString(dto: TitleNewsDto): String {
        return dto.name
    }

    fun mapDbModelToEntity(dbModel: NewsDBO) = NewsInfo(
        id = dbModel.id,
        guid = dbModel.guid,
        imageUrl = dbModel.imageUrl,
        title = dbModel.title,
        body = dbModel.body,
        titleNews = dbModel.nameNews
    )

    fun mapNewsContainerToListNews(news: NewsContainerDto): List<NewsInfoDto> {
        return news.data ?: throw RuntimeException("Empty List")
    }
}