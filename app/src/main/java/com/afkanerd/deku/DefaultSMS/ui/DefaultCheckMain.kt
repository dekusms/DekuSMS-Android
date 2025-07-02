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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.example.compose.AppTheme


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
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            Image(
                painter= painterResource(R.drawable.set_default_sms_app),
                contentDescription = stringResource(R.string.welcome_image),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(350.dp)
            )
            Spacer(Modifier.size(32.dp))

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
                onClick = {
                    getDefaultPermission.launch(makeDefault(context))
                }
            ) {
                Text(
                    stringResource(R.string.default_check_btn_text),
                )
            }

        }
        TextButton(
            onClick = {
                val url = context.getString(R.string.privacy_policy_url) // Your existing URL string resource
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        ) {
            val annotatedString = buildAnnotatedString {
                append(stringResource(R.string.read_our_text_part))
                append(" ")
                withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append(stringResource(R.string.privacy_policy_text_part))
                }
            }
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.labelLarge
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

@Preview(showBackground = true, name = "DefaultCheckMain Light")
@Preview(showBackground = true, name = "DefaultCheckMain Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DefaultCheckMainPreview() {
    AppTheme {
        DefaultCheckMain()
    }
}


