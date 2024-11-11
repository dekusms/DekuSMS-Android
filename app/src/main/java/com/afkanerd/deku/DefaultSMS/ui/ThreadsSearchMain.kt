package com.afkanerd.deku.DefaultSMS.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import com.afkanerd.deku.DefaultSMS.R
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun SearchThreadsMain() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var searchInput by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.padding(8.dp),
        topBar = {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query= searchInput,
                        onQueryChange = {},
                        onSearch = { expanded = false },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        placeholder = {
                            Text(stringResource(R.string.search_messages_text))
                        },
                        leadingIcon = {
                            IconButton(onClick = {

                            }) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = {

                            }) {
                                Icon(Icons.Default.Cancel, contentDescription = null)
                            }
                        },
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it},
                modifier = Modifier
                    .fillMaxWidth()
            ) {

            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            state = listState
        )  {

        }
    }

}

