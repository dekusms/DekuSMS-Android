package com.afkanerd.smswithoutborders_libsmsmms.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.window.layout.WindowLayoutInfo
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.ui.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.ui.SearchThreadsMain
import com.afkanerd.smswithoutborders_libsmsmms.ui.ThreadConversationLayout
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.ComposeNewMessageNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.ContactDetailsNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.ConversationsScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.HomeScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.SearchScreenNav
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.SearchViewModel
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ThreadsViewModel
import kotlin.reflect.KType

@Composable
fun NavHostControllerInstance(
    newLayoutInfo: WindowLayoutInfo,
    navController: NavHostController,
    threadsViewModel: ThreadsViewModel,
    searchViewModel: SearchViewModel,
    builder: NavGraphBuilder.() -> Unit
) {
    val isFolded by remember {
        mutableStateOf(newLayoutInfo.displayFeatures.isNotEmpty())
    }
    NavHost(
        modifier = Modifier,
        navController = navController,
        startDestination = HomeScreenNav(),
    ) {
        builder()

        if(!isFolded) {
            composable<HomeScreenNav>{ backStackEntry ->
                ThreadConversationLayout(
                    threadsViewModel = threadsViewModel,
                    navController = navController,
                )
            }
            composable<ConversationsScreenNav> { backStackEntry ->
                val convScreen: ConversationsScreenNav = backStackEntry.toRoute()
                Conversations(
                    address = convScreen.address,
                    searchQuery = convScreen.query,
                    navController = navController
                )
            }
            composable<SearchScreenNav> { backStackEntry ->
                val searchScreen: SearchScreenNav = backStackEntry.toRoute()
                SearchThreadsMain(
                    address = searchScreen.address,
                    searchViewModel = searchViewModel,
                    navController = navController
                )
            }
            composable<ContactDetailsNav>{
                TODO("Contacts details navigation")
            }

            composable<ComposeNewMessageNav>{
                TODO("Compose new navigation")
            }
        }
        else {
            composable<HomeScreenNav>{ backStackEntry ->
                val homeScreenNav: HomeScreenNav = backStackEntry.toRoute()
                Folded(
                    threadsViewModel = threadsViewModel,
                    homeScreenNav = homeScreenNav,
                    navController = navController,
                )
            }
        }

    }
}

@Composable
private fun Folded(
    threadsViewModel: ThreadsViewModel,
    homeScreenNav: HomeScreenNav,
    navController: NavHostController,
) {
    Row {
        Column(modifier = Modifier.fillMaxWidth(0.5f)){
            ThreadConversationLayout(
                threadsViewModel = threadsViewModel,
                navController = navController,
            )
        }

        if(homeScreenNav.address?.isBlank() == false) {
            Column {
                Conversations(
                    address = homeScreenNav.address,
                    searchQuery = homeScreenNav.query,
                    navController = navController
                )
            }
        }
        else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NoMessageSelected()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoMessageSelected() {
    Text(
        stringResource(
            R.string.select_a_conversation_from_the_list_on_the_left),
        fontSize = 12.sp,
        textAlign = TextAlign.Center
    )
}
