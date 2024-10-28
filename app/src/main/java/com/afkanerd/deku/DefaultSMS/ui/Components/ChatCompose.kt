package com.afkanerd.deku.DefaultSMS.ui.Components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.deku.DefaultSMS.R

@Preview
@Composable
fun ChatCompose(userInput: String = "") {
    var userInput = userInput
    TextField(
        value = userInput,
        onValueChange = {
            userInput = it
        },
        maxLines = 7,
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(25.dp, 25.dp, 25.dp, 25.dp),
        trailingIcon = {
            IconButton(onClick = {}) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    "Send Message",
                    tint=colorResource(R.color.md_theme_outlineVariant)
                )
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = colorResource(R.color.md_theme_outline),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )

}
