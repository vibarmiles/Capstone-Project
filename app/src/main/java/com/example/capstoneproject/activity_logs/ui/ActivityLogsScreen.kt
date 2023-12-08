package com.example.capstoneproject.activity_logs.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.data.firebase.log.Log
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import kotlinx.coroutines.CoroutineScope
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityLogsScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    activityLogsViewModel: ActivityLogsViewModel,
    userViewModel: UserViewModel,
) {
    val logs = activityLogsViewModel.getLogs().observeAsState(listOf())
    val users = userViewModel.getAll()
    val activityLogsList = remember(logs.value) {
        logs.value.groupBy {
            val localDate = if (it.date != null) Instant.ofEpochMilli(it.date.time).atZone(ZoneId.systemDefault()).toLocalDate() else LocalDate.now()
            localDate!!
        }
    }

    Scaffold(
        topBar = {
            BaseTopAppBar(
                title = stringResource(id = R.string.pos),
                scope = scope,
                scaffoldState = scaffoldState
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier
            .padding(paddingValues)) {
            activityLogsList.toList().sortedByDescending { document -> document.first }.forEach { document ->
                stickyHeader {
                    Column(
                        modifier = Modifier
                            .background(color = MaterialTheme.colors.surface)
                            .fillMaxWidth()
                            .padding(16.dp)) {
                        Text(
                            text = DateTimeFormatter.ofLocalizedDate(
                                FormatStyle.FULL
                            ).format(document.first),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.secondary
                        )
                    }
                    Divider()
                }

                itemsIndexed(
                    items = document.second.sortedByDescending { it.date },
                    key = { _, it -> it.id }
                ) { index, it ->
                    LogItem(log = it, name = users[it.userId]?.let { "${it.lastName}, ${it.firstName}" } ?: "Unknown User", count = index + 1)
                }
            }

            item {
                if (activityLogsViewModel.returnSize.value == 10) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .padding(4.dp)) {
                        Button(onClick = { activityLogsViewModel.load() }) {
                            Text(text = "Load More")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogItem(
    log: Log,
    name: String,
    count: Int,
) {
    val time = remember(log) {
        log.date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalTime()
    }
    Column {
        androidx.compose.material3.ListItem(
            modifier = Modifier.fillMaxWidth(),
            colors = ProjectListItemColors(),
            headlineContent = {
                Text(text = log.event.split("_").joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            overlineContent = {
                Text(text = name)
            },
            supportingContent = {
                Text(text = (time ?: LocalTime.now()).format(DateTimeFormatter.ofPattern("hh:mm a")))
            },
            trailingContent = {
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = count.toString())
                }
            }
        )
        Divider()
    }
}