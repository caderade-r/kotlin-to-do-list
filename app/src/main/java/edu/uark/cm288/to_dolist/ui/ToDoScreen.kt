package edu.uark.cm288.to_dolist.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.uark.cm288.to_dolist.domain.ToDoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoScreenRoot(
    viewModel: ToDoListViewModel,
    onItemClicked: (Int) -> Unit
) {
    val toDoList by viewModel.toDoList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("To-Do List") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onItemClicked(0) }) {
                Icon(Icons.Default.Add, contentDescription = "Add To-Do")
            }
        }
    ) { paddingValues ->
        if (toDoList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No tasks yet. Tap + to add one!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(toDoList, key = { it.id }) { item ->
                    ToDoItemCard(
                        item = item,
                        onItemClicked = { onItemClicked(item.id) },
                        onCheckedChange = { isChecked ->
                            viewModel.updateToDoItem(item.copy(isCompleted = isChecked))
                        },
                        onDeleteClicked = {
                            viewModel.deleteToDoItem(item)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ToDoItemCard(
    item: ToDoEntity,
    onItemClicked: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClicked: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClicked() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = onCheckedChange
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (item.dueDate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val dueText = if (item.dueTime.isNotBlank()) "Due: ${item.dueDate} ${item.dueTime}" else "Due: ${item.dueDate}"
                    Text(
                        text = dueText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (item.alarmEnabled) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Alarm Set (5m prior)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            IconButton(onClick = {
                val shareText = buildString {
                    append("To-Do: ${item.title}")
                    if (item.description.isNotBlank()) append("\nDescription: ${item.description}")
                    if (item.dueDate.isNotBlank()) {
                        val due = if (item.dueTime.isNotBlank()) "${item.dueDate} ${item.dueTime}" else item.dueDate
                        append("\nDue: $due")
                    }
                    append("\nStatus: ${if (item.isCompleted) "Completed" else "Pending"}")
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, item.title)
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                val chooser = Intent.createChooser(intent, "Share To-Do Item")
                context.startActivity(chooser)
            }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDeleteClicked) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoListScreenRoot(
    viewModel: ToDoListViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentItem by viewModel.currentItem

    var title by remember(currentItem) { mutableStateOf(currentItem?.title ?: "") }
    var description by remember(currentItem) { mutableStateOf(currentItem?.description ?: "") }
    var dueDate by remember(currentItem) { mutableStateOf(currentItem?.dueDate ?: "") }
    var dueTime by remember(currentItem) { mutableStateOf(currentItem?.dueTime ?: "") }
    var alarmEnabled by remember(currentItem) { mutableStateOf(currentItem?.alarmEnabled ?: false) }
    var isCompleted by remember(currentItem) { mutableStateOf(currentItem?.isCompleted ?: false) }

    val isEdit = currentItem != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit To-Do" else "New To-Do") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEdit) {
                        IconButton(onClick = {
                            val shareText = buildString {
                                append("To-Do: $title")
                                if (description.isNotBlank()) append("\nDescription: $description")
                                if (dueDate.isNotBlank()) {
                                    val due = if (dueTime.isNotBlank()) "$dueDate $dueTime" else dueDate
                                    append("\nDue: $due")
                                }
                                append("\nStatus: ${if (isCompleted) "Completed" else "Pending"}")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, title)
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            val chooser = Intent.createChooser(intent, "Share To-Do Item")
                            context.startActivity(chooser)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share To-Do"
                            )
                        }

                        IconButton(onClick = {
                            viewModel.deleteCurrentItem()
                            onNavigateBack()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete To-Do",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Due Date (e.g. 2026-03-30)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = dueTime,
                onValueChange = { dueTime = it },
                label = { Text("Due Time (e.g. 14:30)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = alarmEnabled,
                    onCheckedChange = { alarmEnabled = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Alarm (5 mins before due)")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { isCompleted = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Completed")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.saveToDoItem(title, description, dueDate, dueTime, alarmEnabled, isCompleted)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}
