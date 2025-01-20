package com.afkanerd.deku.DefaultSMS.ui

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


@Preview(showBackground = true)
@Composable
fun DefaultCheckMain() {
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

            }) {
                Text(stringResource(R.string.default_check_btn_text))
            }

        }
        TextButton(onClick = {

        }) {
            Text(
                stringResource(R.string.privacy_policy_url),
                fontSize = 12.sp
            )
        }
    }
}