package com.afkanerd.smswithoutborders_libsmsmms.ui

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.afkanerd.smswithoutborders_libsmsmms.R
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.rememberNavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getUriForDrawable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewMain(
    contentUri: Uri,
    address: String,
    date: String,
    navController: NavController,
) {
    val navBarVisible = rememberSaveable { mutableStateOf(true) }
    Scaffold(
        topBar = {
            AnimatedVisibility(visible = navBarVisible.value) {
                TopAppBar(
                    title = {
                        Column {
                            Text(address)
                            Text(
                                date,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                stringResource(R.string.go_back)
                            )
                        }
                    },
                    actions = {}
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = navBarVisible.value) {
                BottomAppBar(
                    actions = {
                        Row {
                            IconButton(onClick = {
                                TODO("Implement Download offline")
                            }) {
                                Icon(
                                    Icons.Outlined.DownloadForOffline,
                                    stringResource(R.string.download_for_offline),
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(40.dp)
                                )
                            }

                            IconButton(onClick = {
                                TODO("Implement share")
                            }) {
                                Icon(
                                    Icons.Outlined.Share,
                                    "Share mms content",
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(40.dp)
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            AsyncImage(
                model = contentUri,
                contentDescription = stringResource(R.string.mms_image),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clickable {
                        navBarVisible.value = !navBarVisible.value
                    }
            )
        }
    }
}


@Preview
@Composable
fun ImageViewMain_Preview() {
    val context = LocalContext.current
    context.getUriForDrawable(R.drawable.github_mark)?.let { uri ->
        ImageViewMain(
            uri,
            "Elliot",
            "10:51 AM",
            rememberNavController()
        )
    }
}
