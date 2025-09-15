package com.afkanerd.deku.Router.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.GatewayClientsListScreen
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.ConversationsCard
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.SearchScreenNav
import com.example.compose.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutedMessagesMainView(
    navController: NavController,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                },
                title = {Text(stringResource(R.string.routed_messages))},
                actions = {
                    IconButton(onClick = {
                        navController.navigate(GatewayClientsListScreen)
                    }) {
                        Icon(Icons.AutoMirrored.Default.List,
                            stringResource(R.string.list_gateway_clients)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            LazyColumn(
//                count = 0,
//                key = ""
            ) {
//                ConversationsCard(
//                    text = "",
//                    timestamp = TODO(),
//                    date = TODO(),
//                    type = TODO(),
//                    showDate = TODO(),
//                    position = TODO(),
//                    status = TODO(),
//                    isSelected = TODO(),
//                    mmsContentUri = TODO(),
//                    mmsMimeType = TODO(),
//                    mmsFilename = TODO(),
//                    onClickCallback = TODO(),
//                    onLongClickCallback = TODO()
//                )
            }
        }
    }
}

@Preview
@Composable
fun RoutedMessagesMainViewPreview() {
    AppTheme {
        RoutedMessagesMainView(rememberNavController())
    }
}
