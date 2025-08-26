package com.afkanerd.smswithoutborders_libsmsmms.ui

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getCurrentLocale
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.setLocale
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsGetEnable24HourFormat
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsGetEnableContextReplies
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsGetEnableSwipeBehaviour
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsGetGetDeliveryReports
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsGetKeepMessagesArchived
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsGetStoreTelephonyDb
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsSetCanSwipeBehaviour
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsSetEnable24HourFormat
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsSetEnableContextReplies
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsSetGetDeliveryReports
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsSetKeepMessagesArchived
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.settingsSetStoreTelephonyDb

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMain(
    navController: NavController,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val inPreviewMode = LocalInspectionMode.current
    val localeArraysValues = stringArrayResource(R.array.language_values)
    val localeArraysOptions= stringArrayResource(R.array.language_options)

    val currentNightMode = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK

    var localeExpanded by remember { mutableStateOf(inPreviewMode) }
    var themeExpanded by remember { mutableStateOf(inPreviewMode) }

    var storeTelephonyDb by remember {
        mutableStateOf(context.settingsGetStoreTelephonyDb)
    }

    var getDeliveryReports by remember {
        mutableStateOf(context.settingsGetGetDeliveryReports)
    }

    var enableSwipeActions by remember {
        mutableStateOf(context.settingsGetEnableSwipeBehaviour)
    }

    var keepMessagesArchived by remember {
        mutableStateOf(context.settingsGetKeepMessagesArchived)
    }

    var enableContextReplies by remember {
        mutableStateOf(context.settingsGetEnableContextReplies)
    }

    var enable24HoursFormat by remember {
        mutableStateOf(context.settingsGetEnable24HourFormat)
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
                title = {
                    Text(stringResource(R.string.general_settings))
                },
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .verticalScroll(scrollState)
            .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            SettingsItem(
                itemTitle = stringResource(R.string.language),
                itemDescription = context.getCurrentLocale()?.displayName ?: "English",
                checked = null,
            ) {
                localeExpanded = true
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                DropdownMenu(
                    expanded = localeExpanded,
                    onDismissRequest = { localeExpanded = false }
                ) {
                    localeArraysOptions.forEachIndexed { i, item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                context.setLocale(localeArraysValues[i])
                                localeExpanded = false
                            }
                        )
                    }
                }
            }

            SettingsItem(
                itemTitle = stringResource(R.string.theme),
                itemDescription = when(currentNightMode) {
                    Configuration.UI_MODE_NIGHT_YES -> stringResource(R.string.dark)
                    Configuration.UI_MODE_NIGHT_NO -> stringResource(R.string.light)
                    else -> stringResource(R.string.system_default)
                },
                checked = null,
            ) {
                themeExpanded = true
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                DropdownMenu(
                    expanded = themeExpanded,
                    onDismissRequest = { themeExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.light)) },
                        onClick = {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            themeExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.dark)) },
                        onClick = {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            themeExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.system_default)) },
                        onClick = {
                            AppCompatDelegate
                                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            themeExpanded = false
                        }
                    )
                }
            }

            SettingsItem(
                itemTitle = stringResource(R.string.save_messages_to_system_s_database),
                itemDescription = stringResource(R.string.other_messaging_apps_would_have_access_to_the_system_s_database),
                checked = storeTelephonyDb,
            ) {
                context.settingsSetStoreTelephonyDb(it ?: storeTelephonyDb)
                storeTelephonyDb = it ?: storeTelephonyDb
            }

            SettingsItem(
                itemTitle = stringResource(R.string.get_sms_delivery_reports),
                itemDescription = stringResource(R.string.find_out_when_an_sms_message_is_delivered),
                checked = getDeliveryReports,
            ) {
                context.settingsSetGetDeliveryReports(it ?: getDeliveryReports)
                getDeliveryReports = it ?: getDeliveryReports
            }

            SettingsItem(
                itemTitle = stringResource(R.string.enable_swipe),
                itemDescription = stringResource(R.string.messages_can_be_deleted_or_archived_by_swiping),
                checked = enableSwipeActions,
            ) {
                context.settingsSetCanSwipeBehaviour(it ?: enableSwipeActions)
                enableSwipeActions = it ?: enableSwipeActions
            }

            SettingsItem(
                itemTitle = stringResource(R.string.keep_messages_archived),
                itemDescription = stringResource(R.string.messages_remain_in_archive_even_when_new_ones_are_sent_or_received),
                checked = keepMessagesArchived,
            ) {
                context.settingsSetKeepMessagesArchived(it ?: keepMessagesArchived)
                keepMessagesArchived = it ?: keepMessagesArchived
            }

            SettingsItem(
                itemTitle = stringResource(R.string.enable_notification_context_replies),
                itemDescription = stringResource(R.string.your_phone_would_suggest_smart_replies_in_notifications_if_enabled),
                checked = enableContextReplies,
            ) {
                context.settingsSetEnableContextReplies(it ?: enableContextReplies)
                enableContextReplies = it ?: enableContextReplies
            }

            SettingsItem(
                itemTitle = stringResource(R.string.enable_24_hours_time_format),
                itemDescription = stringResource(R.string.when_turned_off_would_use_your_system_s_default_time_format),
                checked = enable24HoursFormat,
            ) {
                context.settingsSetEnable24HourFormat(it ?: enable24HoursFormat)
                enable24HoursFormat = it ?: enable24HoursFormat
            }

        }
    }
}

@Composable
fun SettingsItem(
    itemTitle: String,
    itemDescription: String? = null,
    checked: Boolean? = null,
    onClickCallback: (Boolean?) -> Unit,
) {
    val inPreviewMode = LocalInspectionMode.current
    Card(
        onClick = {
            if(!inPreviewMode) {
                if(checked != null)
                    onClickCallback(!checked)
                else onClickCallback(null)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    itemTitle,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom=8.dp),
                    style = MaterialTheme.typography.titleSmall
                )
                itemDescription?.let {
                    Text(
                        it,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))

            if(checked != null) {
                Column {
                    Switch(
                        checked = checked,
                        onCheckedChange = {
//                        checked = it
                            onClickCallback(it)
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SettingsMain_Preview() {
    SettingsMain(
        rememberNavController()
    )
}
