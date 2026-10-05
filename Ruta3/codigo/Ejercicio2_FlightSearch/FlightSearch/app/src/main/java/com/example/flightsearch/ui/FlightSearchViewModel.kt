package com.example.flightsearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearch.FlightSearchApplication
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FlightRepository
import com.example.flightsearch.data.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FlightRoute(
    val departure: Airport,
    val destination: Airport,
    val isFavorite: Boolean
)

data class FlightSearchUiState(
    val suggestions: List<Airport> = emptyList(),
    val selectedAirport: Airport? = null,
    val flights: List<FlightRoute> = emptyList(),
    val favorites: List<Favorite> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class FlightSearchViewModel(
    private val flightRepository: FlightRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {

        viewModelScope.launch {
            _searchQuery.value = userPreferencesRepository.searchQuery.first()
        }
    }

    private val suggestions: Flow<List<Airport>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) flowOf(emptyList())
        else flightRepository.searchAirportsStream(query.trim())
    }

    private val selectedAirport: Flow<Airport?> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) flowOf(null)
        else flightRepository.getAirportByCodeStream(query.trim())
    }

    private val favorites: Flow<List<Favorite>> = flightRepository.getAllFavoritesStream()

    private val flights: Flow<List<FlightRoute>> = selectedAirport.flatMapLatest { departure ->
        if (departure == null) {
            flowOf(emptyList())
        } else {
            combine(
                flightRepository.getDestinationsStream(departure.iataCode),
                favorites
            ) { destinations, favoriteList ->
                destinations.map { destination ->
                    FlightRoute(
                        departure = departure,
                        destination = destination,
                        isFavorite = favoriteList.any {
                            it.departureCode == departure.iataCode &&
                                it.destinationCode == destination.iataCode
                        }
                    )
                }
            }
        }
    }

    val uiState: StateFlow<FlightSearchUiState> =
        combine(suggestions, selectedAirport, flights, favorites) { s, a, f, fav ->
            FlightSearchUiState(suggestions = s, selectedAirport = a, flights = f, favorites = fav)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FlightSearchUiState()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            userPreferencesRepository.saveSearchQuery(query)
        }
    }

    fun selectAirport(airport: Airport) {
        updateSearchQuery(airport.iataCode)
    }

    fun toggleFavorite(route: FlightRoute) {
        viewModelScope.launch {
            if (route.isFavorite) {
                flightRepository.deleteFavorite(route.departure.iataCode, route.destination.iataCode)
            } else {
                flightRepository.insertFavorite(
                    Favorite(
                        departureCode = route.departure.iataCode,
                        destinationCode = route.destination.iataCode
                    )
                )
            }
        }
    }

    fun removeFavorite(favorite: Favorite) {
        viewModelScope.launch {
            flightRepository.deleteFavorite(favorite.departureCode, favorite.destinationCode)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FlightSearchApplication)
                FlightSearchViewModel(
                    application.container.flightRepository,
                    application.container.userPreferencesRepository
                )
            }
        }
    }
}
