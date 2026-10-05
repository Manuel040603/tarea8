package com.example.flightsearch.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightsearch.R
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.Favorite

private val FavoriteGold = Color(0xFFFFB300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchApp(
    viewModel: FlightSearchViewModel = viewModel(factory = FlightSearchViewModel.Factory)
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedAirport = uiState.selectedAirport

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            SearchField(
                query = searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
            when {
                searchQuery.isBlank() -> FavoritesList(
                    favorites = uiState.favorites,
                    onRemove = viewModel::removeFavorite
                )
                selectedAirport != null -> FlightResults(
                    departure = selectedAirport,
                    flights = uiState.flights,
                    onToggleFavorite = viewModel::toggleFavorite
                )
                else -> SuggestionList(
                    suggestions = uiState.suggestions,
                    onSelect = viewModel::selectAirport
                )
            }
        }
    }
}

@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.clear_search))
                }
            }
        }
    )
}

@Composable
fun SuggestionList(
    suggestions: List<Airport>,
    onSelect: (Airport) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) {
        Text(
            text = stringResource(R.string.no_results),
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.padding(vertical = 8.dp)
        )
        return
    }
    LazyColumn(modifier = modifier) {
        items(suggestions, key = { it.id }) { airport ->
            AirportRow(
                code = airport.iataCode,
                name = airport.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(airport) }
                    .padding(vertical = 12.dp)
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun FlightResults(
    departure: Airport,
    flights: List<FlightRoute>,
    onToggleFavorite: (FlightRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.flights_from, departure.iataCode),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(flights, key = { it.destination.id }) { route ->
                FlightCard(route = route, onToggleFavorite = onToggleFavorite)
            }
        }
    }
}

@Composable
fun FlightCard(
    route: FlightRoute,
    onToggleFavorite: (FlightRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.depart), style = MaterialTheme.typography.labelSmall)
                AirportRow(code = route.departure.iataCode, name = route.departure.name)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.arrive), style = MaterialTheme.typography.labelSmall)
                AirportRow(code = route.destination.iataCode, name = route.destination.name)
            }
            IconButton(onClick = { onToggleFavorite(route) }) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = stringResource(R.string.favorite_toggle),
                    tint = if (route.isFavorite) FavoriteGold else MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
fun FavoritesList(
    favorites: List<Favorite>,
    onRemove: (Favorite) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.favorite_routes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (favorites.isEmpty()) {
            Text(stringResource(R.string.no_favorites), style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(favorites, key = { it.id }) { favorite ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.depart), style = MaterialTheme.typography.labelSmall)
                                Text(favorite.departureCode, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text(stringResource(R.string.arrive), style = MaterialTheme.typography.labelSmall)
                                Text(favorite.destinationCode, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { onRemove(favorite) }) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = stringResource(R.string.remove_favorite),
                                    tint = FavoriteGold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AirportRow(code: String, name: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = code, fontWeight = FontWeight.Bold, modifier = Modifier.width(52.dp))
        Text(text = name, style = MaterialTheme.typography.bodyMedium)
    }
}
