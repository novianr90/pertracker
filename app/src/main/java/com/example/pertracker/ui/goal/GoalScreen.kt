package com.example.pertracker.ui.goal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pertracker.data.model.Goal
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    viewModel: GoalViewModel,
    onNavigateBack: () -> Unit
) {
    val goals by viewModel.goals.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // Form inputs
    var title by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Manage Goals") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            items(goals, key = { it.goalId }) { goal ->
                ListItem(
                    headlineContent = { Text(goal.title) },
                    supportingContent = {
                        Text("Target: ${goal.targetAmount} | Current: ${goal.currentAmount}")
                    },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteGoal(goal) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                Divider()
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Goal") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Goal Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = targetAmount,
                            onValueChange = { targetAmount = it },
                            label = { Text("Target Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val parsedTarget = targetAmount.toDoubleOrNull()
                        if (title.isNotBlank() && parsedTarget != null && parsedTarget > 0) {
                            viewModel.saveGoal(
                                Goal(
                                    title = title,
                                    targetAmount = parsedTarget,
                                    deadlineDate = Date(), // Generic Date since not specified
                                    isShariaCompliant = false
                                )
                            )
                            showDialog = false
                            title = ""
                            targetAmount = ""
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
