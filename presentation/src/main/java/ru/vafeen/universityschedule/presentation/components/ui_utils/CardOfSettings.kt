package ru.vafeen.universityschedule.presentation.components.ui_utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.vafeen.universityschedule.data.R
import ru.vafeen.universityschedule.presentation.theme.FontSize
import ru.vafeen.universityschedule.presentation.theme.Theme
import ru.vafeen.universityschedule.presentation.utils.generateRandomColor

@Composable
fun CardOfSettings(
    text: String,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit,
    additionalContentIsVisible: Boolean? = null,
    additionalContent: @Composable (() -> Unit)? = null
) {
    val color = generateRandomColor()
    Card(
        modifier = Modifier.padding(vertical = 15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Theme.colors.buttonColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.clickable(onClick = onClick)) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(color, CircleShape)
                            .padding(3.dp)
                    ) {
                        icon(color)
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    TextForThisTheme(
                        modifier = Modifier.padding(10.dp),
                        fontSize = FontSize.small17,
                        text = text,//stringResource(R.string.link_to_table),
                    )
                }

                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward),
                    contentDescription = "open section",
                    tint = Theme.colors.oppositeTheme
                )
            }
            if (additionalContentIsVisible == true)
                additionalContent?.let { it() }
        }
    }
}