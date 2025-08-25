package com.afkanerd.smswithoutborders_libsmsmms.ui

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat.startActivity
import coil3.compose.AsyncImage
import com.afkanerd.smswithoutborders_libsmsmms.R
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.rememberNavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.exportRawWithColumnGuesses
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getBytesFromUri
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getUriForDrawable
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.shareItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewMain(
    contentUri: Uri,
    address: String,
    date: String,
    navController: NavController,
    filename: String,
    mimeType: String,
) {
    val navBarVisible = rememberSaveable { mutableStateOf(true) }

    val context = LocalContext.current

    val downloadLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(mimeType)) { uri ->
        println(uri)
        uri?.let {
            CoroutineScope(Dispatchers.IO).launch {
                with(context.contentResolver.openFileDescriptor(uri, "w")) {
                    this?.fileDescriptor.let { fd ->
                        val fileOutputStream = FileOutputStream(fd);
                        fileOutputStream.write(context.getBytesFromUri(contentUri))
                        fileOutputStream.close();
                    }
                    this?.close();
                }
            }
        }
    }

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
                                downloadLauncher.launch(filename)
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
                                context.shareItem(contentUri, mimeType)
                            }) {
                                Icon(
                                    Icons.Outlined.Share,
                                    stringResource(R.string.share_mms_content),
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
            rememberNavController(),
            "filename.jpg",
            "image/jpeg"
        )
    }
}
