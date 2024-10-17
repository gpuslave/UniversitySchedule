package ru.vafeen.universityschedule.presentation.components.screens

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import org.koin.androidx.compose.koinViewModel
import ru.vafeen.universityschedule.data.R
import ru.vafeen.universityschedule.data.utils.GSheetsServiceRequestStatus
import ru.vafeen.universityschedule.data.utils.getIconByRequestStatus
import ru.vafeen.universityschedule.data.utils.openLink
import ru.vafeen.universityschedule.domain.utils.getMainColorForThisTheme
import ru.vafeen.universityschedule.domain.utils.getVersionName
import ru.vafeen.universityschedule.domain.utils.save
import ru.vafeen.universityschedule.presentation.components.bottom_bar.BottomBar
import ru.vafeen.universityschedule.presentation.components.ui_utils.ColorPickerDialog
import ru.vafeen.universityschedule.presentation.components.ui_utils.EditLinkDialog
import ru.vafeen.universityschedule.presentation.components.ui_utils.TextForThisTheme
import ru.vafeen.universityschedule.presentation.components.viewModels.SettingsScreenViewModel
import ru.vafeen.universityschedule.presentation.navigation.Screen
import ru.vafeen.universityschedule.presentation.theme.FontSize
import ru.vafeen.universityschedule.presentation.theme.Theme
import ru.vafeen.universityschedule.presentation.utils.Link

/**
 * Screen with settings for application:
 *
 * General:
 * - Link
 * - Table
 * - Interface color
 * - Subgroup
 *
 * Contacts:
 * - Project community
 * - Code
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    navController: NavController,
) {
    val viewModel: SettingsScreenViewModel = koinViewModel()
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    var subgroupList by remember { mutableStateOf(listOf<String>()) }
    val settings by viewModel.settings.collectAsState()
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
        viewModel.updateLocalDatabase { status ->
            networkState = status
        }
    }
    LaunchedEffect(key1 = null) {
        viewModel.databaseRepository.getAllAsFlowLessons().collect { lessons ->
            subgroupList = lessons.filter {
                it.subGroup != null
            }.map {
                it.subGroup.toString()
            }.distinct()
        }
    }
    BackHandler(onBack = gotoMainScreenCallBack)
    Scaffold(
        containerColor = Theme.colors.singleTheme,
        topBar = {
            TopAppBar(colors = TopAppBarColors(
                containerColor = Theme.colors.singleTheme,
                scrolledContainerColor = Theme.colors.singleTheme,
                navigationIconContentColor = Theme.colors.oppositeTheme,
                titleContentColor = Theme.colors.oppositeTheme,
                actionIconContentColor = Theme.colors.singleTheme
            ), modifier = Modifier.fillMaxWidth(), title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            id = getIconByRequestStatus(
                                networkState = networkState
                            )
                        ),
                        contentDescription = "data updating state",
                        tint = Theme.colors.oppositeTheme
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    TextForThisTheme(
                        text = stringResource(R.string.settings), fontSize = FontSize.big22
                    )
                }
            })
        },
        bottomBar = {
            BottomBar(
                containerColor = settings.getMainColorForThisTheme(isDark = dark)
                    ?: Theme.colors.mainColor,
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
                linkIsEditable = false
                viewModel.gSheetsService = settings.link?.let {
                    ru.vafeen.universityschedule.data.utils.createGSheetsService(
                        link = it
                    )
                }
                key = 3 - key
            }
            if (colorIsEditable) ColorPickerDialog(context = context,
                firstColor = settings.getMainColorForThisTheme(isDark = dark)
                    ?: Theme.colors.mainColor,
                onDismissRequest = { colorIsEditable = false }) {
                viewModel.sharedPreferences.save(
                    if (dark) settings.copy(
                        darkThemeColor = it
                    ) else settings.copy(lightThemeColor = it)
                )

            }

            Spacer(modifier = Modifier.height(30.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 40.dp),
            ) {

                val cardColors = CardDefaults.cardColors(
                    containerColor = Theme.colors.buttonColor,
                )

                // name of section
                TextForThisTheme(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally),
                    fontSize = FontSize.big22,
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
                            fontSize = FontSize.small17,
                            text = stringResource(R.string.link_to_table),
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.link),
                            contentDescription = "edit link",
                            tint = Theme.colors.oppositeTheme
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
                                fontSize = FontSize.small17,
                                text = stringResource(R.string.table),
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.table),
                                contentDescription = "edit link",
                                tint = Theme.colors.oppositeTheme
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
                            fontSize = FontSize.small17,
                            text = stringResource(R.string.interface_color)
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.palette),
                            contentDescription = "read license",
                            tint = Theme.colors.oppositeTheme
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
                                    fontSize = FontSize.small17
                                )
                                Icon(
                                    painter = painterResource(
                                        id = if (subGroupIsChanging) R.drawable.unfold_less
                                        else R.drawable.unfold_more
                                    ),
                                    contentDescription = "more",
                                    tint = Theme.colors.oppositeTheme
                                )
                            }
                            if (subGroupIsChanging) {
                                LazyRow(
                                    state = subGroupLazyRowState, modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp)

                                ) {
                                    items(subgroupList) { subgroup ->
                                        AssistChip(
                                            leadingIcon = {
                                                if (subgroup == settings.subgroup) Icon(
                                                    imageVector = Icons.Default.Done,
                                                    contentDescription = "this is user subgroup",
                                                    tint = Theme.colors.oppositeTheme
                                                )
                                            },
                                            modifier = Modifier.padding(horizontal = 3.dp),
                                            onClick = {
                                                viewModel.sharedPreferences.save(
                                                    settings.copy(
                                                        subgroup = if (settings.subgroup != subgroup) subgroup else null
                                                    )
                                                )
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
                    fontSize = FontSize.big22,
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
                            fontSize = FontSize.small17,
                            text = stringResource(R.string.project_community)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.group),
                            contentDescription = "read license",
                            tint = Theme.colors.oppositeTheme
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
                            fontSize = FontSize.small17,
                            text = stringResource(R.string.code)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.terminal),
                            contentDescription = "view code",
                            tint = Theme.colors.oppositeTheme
                        )
                    }
                }


            }

            // version
            TextForThisTheme(
                modifier = Modifier
                    .padding(10.dp)
                    .padding(bottom = 20.dp)
                    .align(Alignment.End),
                fontSize = FontSize.small17,
                text = "${stringResource(R.string.version)} ${LocalContext.current.getVersionName()}"
            )
        }
    }
}