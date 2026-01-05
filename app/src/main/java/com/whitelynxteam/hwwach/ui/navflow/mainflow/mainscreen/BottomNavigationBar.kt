package com.whitelynxteam.hwwach.ui.navflow.mainflow.mainscreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.whitelynxteam.hwwach.R
import com.whitelynxteam.hwwach.ui.theme.Grey50

@Preview
@Composable
fun NavigationBarSample(
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Техника", "Добавить", "Кабинет")
    val selectedIcons = listOf(R.drawable.ic_appliances_selected, R.drawable.ic_add_selected, R.drawable.ic_personal_account_selected)
    val unselectedIcons =
        listOf(R.drawable.ic_appliances_unselected, R.drawable.ic_add_unselected, R.drawable.ic_personal_account_unselected)

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = Grey50
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = (if (selectedItem == index) selectedIcons[index] else unselectedIcons[index]) as Int) ,
                        contentDescription = item,
                    )
                },
                label = { Text(text = item) },
                selected = selectedItem == index,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                ),
                onClick = { selectedItem = index },
            )
        }
    }
}