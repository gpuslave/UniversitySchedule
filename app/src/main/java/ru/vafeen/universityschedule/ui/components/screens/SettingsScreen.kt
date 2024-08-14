package ru.vafeen.universityschedule.ui.components.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.vafeen.universityschedule.R
import ru.vafeen.universityschedule.ui.components.bottom_bar.BottomBar
import ru.vafeen.universityschedule.ui.components.ui_utils.ColorPickerDialog
import ru.vafeen.universityschedule.ui.components.ui_utils.EditLinkDialog
import ru.vafeen.universityschedule.ui.components.ui_utils.TextForThisTheme
import ru.vafeen.universityschedule.ui.components.viewModels.SettingsScreenViewModel
import ru.vafeen.universityschedule.ui.navigation.Screen
import ru.vafeen.universityschedule.ui.theme.FontSize
import ru.vafeen.universityschedule.ui.theme.ScheduleTheme
import ru.vafeen.universityschedule.utils.GSheetsServiceRequestStatus
import ru.vafeen.universityschedule.utils.Link
import ru.vafeen.universityschedule.utils.createGSheetsService
import ru.vafeen.universityschedule.utils.getIconByRequestStatus
import ru.vafeen.universityschedule.utils.getMainColorForThisTheme
import ru.vafeen.universityschedule.utils.getSettingsOrCreateIfNull
import ru.vafeen.universityschedule.utils.getVersionName
import ru.vafeen.universityschedule.utils.openLink
import ru.vafeen.universityschedule.utils.save

/**
 * Screen with settings for application:
 * 1. Link (opening by tap and opening editing dialog by tap on pencil near
 * 2. License
 * 3. Version
 * 4. Page in RUStore
 */
@Composable
fun SettingsScreen(
    context: Context,
    navController: NavController,
    viewModel: SettingsScreenViewModel,
) {
    val dark = isSystemInDarkTheme()
    var subgroupList by remember { mutableStateOf(listOf<String>()) }
    var settings by remember {
        mutableStateOf(viewModel.sharedPreferences.getSettingsOrCreateIfNull())
    }
    var linkIsEditable by remember {
        mutableStateOf(false)
    }
    var colorIsEditable by remember {
        mutableStateOf(false)
    }
    val gotoMainScreenCallBack = {
        navController.popBackStack()
        navController.popBackStack()
        navController.navigate(Screen.Main.route)
    }

    var subGroupIsChanging by remember {
        mutableStateOf(false)
    }
    val subGroupLazyRowState = rememberLazyListState()
    var networkState by remember {
        mutableStateOf(GSheetsServiceRequestStatus.Waiting)
    }
    var key by remember {
        mutableIntStateOf(1)
    }
    LaunchedEffect(key1 = key) {
        viewModel.updateLocalDatabase { newLessons, status ->
            subgroupList = newLessons.filter {
                it.subGroup != null
            }.map {
                it.subGroup.toString()
            }.distinct()

            networkState = status
        }
    }
    BackHandler(onBack = gotoMainScreenCallBack)
    Scaffold(containerColor = ScheduleTheme.colors.singleTheme, bottomBar = {
        BottomBar(
            containerColor = settings.getMainColorForThisTheme(isDark = dark)
                ?: ScheduleTheme.colors.mainColor,
            clickToScreen1 = gotoMainScreenCallBack,
            selected2 = true
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (linkIsEditable) EditLinkDialog(
                context = context, sharedPreferences = viewModel.sharedPreferences
            ) {
                settings = viewModel.sharedPreferences.getSettingsOrCreateIfNull()
                viewModel.settings = settings
                linkIsEditable = false
                viewModel.gSheetsService = settings.link?.let { createGSheetsService(link = it) }
                key = 3 - key
            }
            if (colorIsEditable) ColorPickerDialog(context = context,
                firstColor = settings.getMainColorForThisTheme(isDark = dark)
                    ?: ScheduleTheme.colors.mainColor,
                onDismissRequest = { colorIsEditable = false }) {
                settings = (if (dark) settings.copy(
                    darkThemeColor = it
                ) else settings.copy(lightThemeColor = it)).save(sharedPreferences = viewModel.sharedPreferences)
            }
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    painter = painterResource(
                        id = getIconByRequestStatus(networkState = networkState)
                    ),
                    contentDescription = "data updating state",
                    tint = ScheduleTheme.colors.oppositeTheme
                )
                Spacer(modifier = Modifier.width(15.dp))
                TextForThisTheme(
                    text = stringResource(R.string.settings), fontSize = FontSize.medium
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 40.dp),
            ) {

                val cardColors = CardDefaults.cardColors(
                    containerColor = ScheduleTheme.colors.buttonColor,
                )

                // name of section
                TextForThisTheme(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally),
                    fontSize = FontSize.medium,
                    text = stringResource(R.string.general)
                )

                // Edit link
                Card(colors = cardColors) {
                    Row(modifier = Modifier
                        .clickable { linkIsEditable = true }
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        TextForThisTheme(
                            modifier = Modifier.padding(10.dp),
                            fontSize = FontSize.small,
                            text = stringResource(R.string.link_to_table),
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "edit link",
                            tint = ScheduleTheme.colors.oppositeTheme
                        )
                    }
                }
                Spacer(modifier = Modifier.height(viewModel.spaceBetweenCards))

                // View table
                if (settings.link != null) {
                    Card(colors = cardColors) {
                        Row(modifier = Modifier
                            .clickable {
                                settings.link?.let { openLink(context = context, link = it) }
                            }
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            TextForThisTheme(
                                modifier = Modifier.padding(10.dp),
                                fontSize = FontSize.small,
                                text = stringResource(R.string.table),
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.table),
                                contentDescription = "edit link",
                                tint = ScheduleTheme.colors.oppositeTheme
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(viewModel.spaceBetweenCards))
                }

                // Color
                Card(colors = cardColors) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            colorIsEditable = true
                        }
                        .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        TextForThisTheme(
                            modifier = Modifier.padding(10.dp),
                            fontSize = FontSize.small,
                            text = stringResource(R.string.interface_color)
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.palette),
                            contentDescription = "read license",
                            tint = ScheduleTheme.colors.oppositeTheme
                        )
                    }
                }
                Spacer(modifier = Modifier.height(viewModel.spaceBetweenCards))

                // Subgroup
                if (subgroupList.isNotEmpty()) {
                    Card(colors = cardColors) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                subGroupIsChanging = !subGroupIsChanging
                            }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextForThisTheme(
                                    modifier = Modifier.padding(10.dp),
                                    text = stringResource(R.string.subgroup),
                                    fontSize = FontSize.small
                                )
                                Icon(
                                    painter = painterResource(
                                        id = if (subGroupIsChanging) R.drawable.unfold_less
                                        else R.drawable.unfold_more
                                    ),
                                    contentDescription = "more",
                                    tint = ScheduleTheme.colors.oppositeTheme
                                )
                            }
                            if (subGroupIsChanging) {
//                            LaunchedEffect(key1 = null) {
//                                subGroupRowState.scrollToItem(subgroupList.indexOf(userSubgroup))
//                            }

                                LazyRow(
                                    state = subGroupLazyRowState, modifier = Modifier
                                        .fillMaxWidth()
//                                    .height(50.dp)
                                        .padding(horizontal = 10.dp)

                                ) {
                                    items(subgroupList) { subgroup ->
                                        AssistChip(
                                            leadingIcon = {
                                                if (subgroup == settings.subgroup) Icon(
                                                    imageVector = Icons.Default.Done,
                                                    contentDescription = "this is user subgroup",
                                                    tint = ScheduleTheme.colors.oppositeTheme
                                                )
                                            },
                                            modifier = Modifier.padding(horizontal = 3.dp),
//                                        enabled = subgroup != settings.subgroup,
                                            onClick = {
                                                settings =
                                                    settings.copy(subgroup = if (settings.subgroup != subgroup) subgroup else null)
                                                        .save(sharedPreferences = viewModel.sharedPreferences)

                                            },
                                            label = { TextForThisTheme(text = subgroup) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(viewModel.spaceBetweenCards))
                }

                // name of section
                TextForThisTheme(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally),
                    fontSize = FontSize.medium,
                    text = stringResource(R.string.contacts)
                )

                // VK Group
                Card(colors = cardColors) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            openLink(
                                context = context, link = Link.VK_GROUP
                            )
                        }
                        .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        TextForThisTheme(
                            modifier = Modifier.padding(10.dp),
                            fontSize = FontSize.small,
                            text = stringResource(R.string.project_community)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.group),
                            contentDescription = "read license",
                            tint = ScheduleTheme.colors.oppositeTheme
                        )
                    }
                }
                Spacer(modifier = Modifier.height(viewModel.spaceBetweenCards))

                // CODE
                Card(colors = cardColors) {
                    Row(modifier = Modifier
                        .clickable {
                            openLink(
                                context = context, link = Link.CODE
                            )
                        }
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        TextForThisTheme(
                            modifier = Modifier.padding(10.dp),
                            fontSize = FontSize.small,
                            text = stringResource(R.string.code)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.terminal),
                            contentDescription = "view code",
                            tint = ScheduleTheme.colors.oppositeTheme
                        )
                    }
                }


            }

            TextForThisTheme(
                modifier = Modifier
                    .padding(10.dp)
                    .padding(bottom = 20.dp)
                    .align(Alignment.End),
                fontSize = FontSize.small,
                text = "${stringResource(R.string.version)} ${getVersionName(context = LocalContext.current)}"
            )
        }
    }
}