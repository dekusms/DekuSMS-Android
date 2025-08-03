package com.afkanerd.deku.DefaultSMS.ui

import android.graphics.drawable.Icon
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.Extensions.exportRawWithColumnGuesses
import com.afkanerd.deku.DefaultSMS.Extensions.importRawColumnGuesses
import com.afkanerd.deku.DefaultSMS.R
import com.example.compose.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperModeMain(
    navController: NavController,
) {
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")) { uri ->
        println(uri)
        uri?.let {
            CoroutineScope(Dispatchers.IO).launch {
                with(context.contentResolver.openFileDescriptor(uri, "w")) {
                    this?.fileDescriptor.let { fd ->
                        val fileOutputStream = FileOutputStream(fd);
                        fileOutputStream.write(context
                            .exportRawWithColumnGuesses().encodeToByteArray());
                        // Let the document provider know you're done by closing the stream.
                        fileOutputStream.close();
                    }
                    this?.close();

                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context,
                            context.getString(R.string.conversations_exported_complete),
                            Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val stringBuilder = StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            stringBuilder.append(line)
                            line = reader.readLine()
                        }
                    }
                }

                val details = context.importRawColumnGuesses(stringBuilder.toString())
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context,
                        "Mms: ${details.mmsCount}, MmsPart: ${details.mmsPartCount}, " +
                                "MmsAddr: ${details.mmsAddrCount}",
                        Toast.LENGTH_LONG).show();
                }
            }
        }
    }


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
                title = {Text(stringResource(R.string.developer_options))},
            )
        }
    ) { innerPadding ->
        Column(modifier =
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.sms_mms_import_export),
                    style= MaterialTheme.typography.titleSmall,
                    modifier=Modifier.padding(bottom=18.dp)
                )

                // Export SMS/MMS
                DevModeMenuItem(
                    iconImage = Icons.Filled.Download,
                    iconDescription = "Export native SMS database",
                    itemTitle = stringResource(R.string.export_sms_mms_database),
                    itemDescription = stringResource(R.string.this_exports_all_sms_and_mms_databases_can_be_useful_if_you_need_to_capture_all_fields_as_stored_by_the_default_sms_apps),
                ) {
                    val filename = context.getString(
                        R.string.deku_sms_dev_mode_export,
                        System.currentTimeMillis()
                    );
                    exportLauncher.launch(filename)
                }

                Spacer(Modifier.height(16.dp))

                // Import SMS/MMS
                DevModeMenuItem(
                    iconImage = Icons.Filled.UploadFile,
                    iconDescription = "Import native SMS database",
                    itemTitle = "Import SMS/MMS database",
                    itemDescription = "This imports all SMS and MMS databases. Can be useful if you need to capture all fields as stored by the default SMS apps.",
                ) {
                    importLauncher.launch("application/json")
                }
            }
        }
    }
}

@Composable
fun DevModeMenuItem(
    iconImage: ImageVector,
    iconDescription: String? = null,
    itemTitle: String,
    itemDescription: String? = null,
    onClickCallback: () -> Unit,
) {
    Row {
        Icon(
            iconImage,
            iconDescription
        )

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onClickCallback() }
        ) {
            Text(
                itemTitle,
                modifier = Modifier.padding(bottom=8.dp)
            )
            itemDescription?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Preview
@Composable
fun DeveloperModeMainPreview() {
    AppTheme(darkTheme = true) {
        Surface(Modifier.safeDrawingPadding()) {
            DeveloperModeMain(rememberNavController())
        }
    }
}

