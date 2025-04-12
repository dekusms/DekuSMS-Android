package com.afkanerd.deku.DefaultSMS.ui

import android.app.Activity.RESULT_OK
import android.app.role.RoleManager
import android.content.Context
import android.content.Context.ROLE_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.afkanerd.deku.DefaultSMS.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit


@Preview(showBackground = true)
@Composable
fun DefaultCheckMain(permissionGrantedCallback: (()->Unit)? = null) {
    val context = LocalContext.current

    val getDefaultPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(context)
                sharedPreferences.edit() {
                    putBoolean(context.getString(R.string.configs_load_natives), true)
                }
                permissionGrantedCallback?.invoke()
            }
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().weight(1f)
        ) {
            Image(
                painter= painterResource(R.drawable.undraw_team_work_i1f3),
                contentDescription = stringResource(R.string.welcome_image),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(250.dp)
            )
            Spacer(Modifier.size(32.dp))

            Button(onClick = {
                getDefaultPermission.launch(makeDefault(context))
            }) {
                Text(stringResource(R.string.default_check_btn_text))
            }

        }
        TextButton(onClick = {
            val url = context.getString(R.string.privacy_policy_url)
            val shareIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(shareIntent)
        }) {
            Text(
                stringResource(R.string.privacy_policy_url),
                fontSize = 12.sp
            )
        }
    }
}

fun makeDefault(context: Context): Intent {
    // TODO: replace this with checking other permissions - since this gives null in level 35
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(ROLE_SERVICE) as RoleManager
        roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS).apply {
            putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
        }
    } else {
        Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
            putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
        }
    }
}


