package com.example.pertracker.ui.transaction

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pertracker.data.model.TransactionEntity
import java.util.Calendar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionInputScreen(viewModel: TransactionViewModel, onNavigateBack: () -> Unit) {
    val categories by viewModel.categories.collectAsState()
    val goals by viewModel.goals.collectAsState()
    
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    val selectedCategory = categories.find { it.categoryId == selectedCategoryId }
    var selectedGoalId by remember { mutableStateOf<Long?>(null) }
    var amount by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var dateInMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var expanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var expandedGoal by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val setDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            dateInMillis = setDate.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Add Transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            ) 
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = categories.find { it.categoryId == selectedCategoryId }?.name ?: "Select Category",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCategoryId = cat.categoryId
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedCategory?.type == com.example.pertracker.data.model.CategoryType.TRANSFER_GOAL) {
                        ExposedDropdownMenuBox(
                            expanded = expandedGoal,
                            onExpandedChange = { expandedGoal = !expandedGoal }
                        ) {
                            OutlinedTextField(
                                value = goals.find { it.goalId == selectedGoalId }?.title ?: "Select Goal",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Goal") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGoal) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedGoal,
                                onDismissRequest = { expandedGoal = false }
                            ) {
                                goals.forEach { goal ->
                                    DropdownMenuItem(
                                        text = { Text(goal.title) },
                                        onClick = {
                                            selectedGoalId = goal.goalId
                                            expandedGoal = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedButton(
                        onClick = { datePickerDialog.show() }, 
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Date", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dateInMillis))
                        Text(dateStr, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    if (selectedCategoryId != null && parsedAmount != null) {
                        isSubmitting = true
                        val tx = TransactionEntity(
                            categoryId = selectedCategoryId!!,
                            amount = parsedAmount,
                            transactionDate = dateInMillis,
                            remarks = remarks,
                            goalId = if (selectedCategory?.type == com.example.pertracker.data.model.CategoryType.TRANSFER_GOAL) selectedGoalId else null
                        )
                        viewModel.saveTransaction(tx) { syncResult ->
                            coroutineScope.launch {
                                isSubmitting = false
                                if (syncResult == true) {
                                    snackbarHostState.showSnackbar("Transaction Form Saved & Synced")
                                } else if (syncResult == false) {
                                    snackbarHostState.showSnackbar("Saved locally. Sync failed.")
                                } else {
                                    snackbarHostState.showSnackbar("Transaction Saved Locally")
                                }
                                onNavigateBack()
                            }
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please fill all required fields correctly.")
                        }
                    }
                }
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Transaction", fontSize = MaterialTheme.typography.titleMedium.fontSize)
                }
            }
        }
    }
}
