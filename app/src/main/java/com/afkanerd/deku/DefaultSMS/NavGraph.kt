package com.afkanerd.deku.DefaultSMS

import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.afkanerd.deku.DefaultSMS.ui.Screens
@Composable
fun SetupNavGraph(
    navController: NavHostController
) {
    NavHost(
        modifier = Modifier,
        navController = navController,
        startDestination = Screens.ThreadConversations.route,
    ) {
        activity(route=Screens.ThreadConversations.route) {
            activityClass = ThreadsConversationActivity::class
        }

        activity(Screens.Conversation.route) {
            activityClass = ConversationsActivity::class
            argument("threadId") {type = NavType.StringType}
        }
    }
}
