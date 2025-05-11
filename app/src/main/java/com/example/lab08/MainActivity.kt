package com.example.lab08

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lab08.data.Task
import com.example.lab08.data.TaskDatabase
import com.example.lab08.data.TaskViewModel
import com.example.lab08.data.TaskViewModel.FilterType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val db = TaskDatabase.getDatabase(applicationContext)
            val taskDao = db.taskDao()
            val viewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(taskDao))
            TaskScreen(viewModel)
        }
    }
}

class TaskViewModelFactory(private val taskDao: com.example.lab08.data.TaskDao) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    var newTaskDescription by remember { mutableStateOf("") }
    var taskBeingEdited by remember { mutableStateOf<Task?>(null) }
    var editedDescription by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(FilterType.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            FilterButton(
                text = "All",
                isSelected = selectedFilter == FilterType.ALL,
                onClick = {
                    selectedFilter = FilterType.ALL
                    viewModel.setFilter(FilterType.ALL)
                }
            )
            FilterButton(
                text = "Completed",
                isSelected = selectedFilter == FilterType.COMPLETED,
                onClick = {
                    selectedFilter = FilterType.COMPLETED
                    viewModel.setFilter(FilterType.COMPLETED)
                }
            )
            FilterButton(
                text = "Pending",
                isSelected = selectedFilter == FilterType.PENDING,
                onClick = {
                    selectedFilter = FilterType.PENDING
                    viewModel.setFilter(FilterType.PENDING)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.setSearchQuery(it)
            },
            label = { Text("Search Tasks") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = newTaskDescription,
            onValueChange = { newTaskDescription = it },
            label = { Text("New Task") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (newTaskDescription.isNotEmpty()) {
                    viewModel.addTask(newTaskDescription)
                    newTaskDescription = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Add Task")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            if (tasks.isEmpty()) {
                Text("No tasks found!")
            } else {
                tasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (taskBeingEdited == task) {
                            TextField(
                                value = editedDescription,
                                onValueChange = { editedDescription = it },
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = {
                                viewModel.editTask(task, editedDescription)
                                taskBeingEdited = null
                                editedDescription = ""
                            }) {
                                Text("Save")
                            }
                            Button(onClick = {
                                taskBeingEdited = null
                                editedDescription = ""
                            }) {
                                Text("Cancel")
                            }
                        } else {
                            Text(text = task.description, modifier = Modifier.weight(1f))
                            Button(onClick = { viewModel.toggleTaskCompletion(task) }) {
                                Text(if (task.isCompleted) "Completed" else "Pending")
                            }
                            Button(onClick = {
                                taskBeingEdited = task
                                editedDescription = task.description
                            }) {
                                Text("Edit")
                            }
                            IconButton(onClick = { viewModel.deleteTask(task) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.deleteAllTasks() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Delete All Tasks")
        }
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(text = text)
    }
}
