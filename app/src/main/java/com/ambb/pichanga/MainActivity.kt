package com.ambb.pichanga

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.ambb.pichanga.screen.DetailScreen
import com.ambb.pichanga.screen.DetailScreen2
import com.ambb.pichanga.screen.HomeScreen
import com.ambb.pichanga.screen.MyTeamScreen
import com.ambb.pichanga.screen.PickUpGameScreen
import com.ambb.pichanga.ui.theme.PichangaTheme
import kotlinx.serialization.Serializable


sealed interface BottomNavItem {
    val icon: ImageVector
    val title: String
}

@Serializable
data object Home : NavKey, BottomNavItem {
    override val icon: ImageVector = Icons.Filled.Home
    override val title: String = "Home"
}

@Serializable
data object PickUpGame : NavKey, BottomNavItem {
    override val icon: ImageVector = Icons.Filled.PlayArrow
    override val title: String = "PickUpGame"
}

@Serializable
data object MyTeam : NavKey, BottomNavItem {
    override val icon: ImageVector = Icons.Filled.Person
    override val title: String = "My Team"
}

@Serializable
data class Detail(val name: String) : NavKey

@Serializable
data class Detail2(val name: String) : NavKey


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PichangaTheme {
                val bottomNavItems = listOf(Home, PickUpGame, MyTeam)
                val (currentTab, tabBackStacks) = rememberTabsBackStacks(bottomNavItems, Home)
                val tabHistory = remember { mutableStateListOf<BottomNavItem>() }

                val backStack by remember {
                    derivedStateOf { tabBackStacks.getValue(currentTab.value) }
                }

                val onBack = {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    } else if (tabHistory.isNotEmpty()) {
                        currentTab.value = tabHistory.removeAt(tabHistory.lastIndex)
                    } else {
                        finish()
                    }
                }

                fun popToRoot(stack: MutableList<NavKey>) {
                    if (stack.isEmpty()) return
                    stack.subList(1, stack.size).clear()
                }

                fun switchTab(newTab: BottomNavItem) {
                    if (currentTab.value == newTab) {
                        tabBackStacks[newTab]?.let { popToRoot(it) }
                    } else {
                        if (tabHistory.lastOrNull() != currentTab.value) {
                            tabHistory.add(currentTab.value)
                        }
                        currentTab.value = newTab
                    }
                }

                BackHandler {
                    onBack()
                }
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            bottomNavItems.forEach { item ->
                                val isSelected = currentTab.value == item
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { switchTab(item) },
                                    icon = {
                                        Icon(imageVector = item.icon, contentDescription = null)
                                    },
                                    label = {
                                        Text(text = item.title)
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    val screenModifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)

                    NavDisplay(
                        backStack = backStack,
                        onBack = { onBack() },
                        entryDecorators = listOf(
                            rememberSceneSetupNavEntryDecorator(),
                            rememberSavedStateNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        entryProvider = entryProvider {
                            entry<Home> {
                                HomeScreen(
                                    onDetailClick = { backStack.add(Detail(name = "Home Detail")) },
                                    modifier = screenModifier
                                )
                            }
                            entry<PickUpGame> {
                                PickUpGameScreen(
                                    onPickUpDetail = { backStack.add(Detail(name = "PickUpGameScreen Detail")) },
                                    modifier = screenModifier
                                )
                            }
                            entry<MyTeam> {
                                MyTeamScreen(
                                    onDetailClick = { backStack.add(Detail(name = "My Team Detail")) },
                                    modifier = screenModifier,

                                )
                            }
                            entry<Detail> { args ->
                                DetailScreen(
                                    onBackClick = { backStack.removeLastOrNull() },
                                    modifier = screenModifier,
                                    title = args.name
                                ) {
                                    backStack.add(Detail2(name = "My Home Detail2 screen"))
                                }
                            }
                            entry<Detail2> { args ->
                                DetailScreen2(
                                    onBackClick = { backStack.removeLastOrNull() },
                                    modifier = screenModifier,
                                    title = args.name
                                ) {
                                    switchTab(MyTeam)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun rememberTabsBackStacks(
    tabs: List<BottomNavItem>,
    startTab: BottomNavItem
): Pair<MutableState<BottomNavItem>, SnapshotStateMap<BottomNavItem, MutableList<NavKey>>> {
    val currentTab = remember { mutableStateOf(startTab) }
    val backStacks = remember {
        mutableStateMapOf<BottomNavItem, MutableList<NavKey>>().apply {
            tabs.forEach { tab ->
                this[tab] = mutableStateListOf(tab as NavKey)
            }
        }
    }
    return currentTab to backStacks
}
