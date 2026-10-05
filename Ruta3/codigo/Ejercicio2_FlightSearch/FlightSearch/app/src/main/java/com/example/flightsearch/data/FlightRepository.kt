package com.example.flightsearch.data

import kotlinx.coroutines.flow.Flow

interface FlightRepository {
    fun searchAirportsStream(query: String): Flow<List<Airport>>
    fun getAirportByCodeStream(code: String): Flow<Airport?>
    fun getDestinationsStream(departureCode: String): Flow<List<Airport>>
    fun getAllFavoritesStream(): Flow<List<Favorite>>
    suspend fun insertFavorite(favorite: Favorite)
    suspend fun deleteFavorite(departureCode: String, destinationCode: String)
}

class OfflineFlightRepository(private val flightDao: FlightDao) : FlightRepository {
    override fun searchAirportsStream(query: String) = flightDao.searchAirports(query)
    override fun getAirportByCodeStream(code: String) = flightDao.getAirportByCode(code)
    override fun getDestinationsStream(departureCode: String) = flightDao.getDestinations(departureCode)
    override fun getAllFavoritesStream() = flightDao.getAllFavorites()
    override suspend fun insertFavorite(favorite: Favorite) = flightDao.insertFavorite(favorite)
    override suspend fun deleteFavorite(departureCode: String, destinationCode: String) =
        flightDao.deleteFavorite(departureCode, destinationCode)
}
