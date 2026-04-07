package com.example.pertracker.ui.transaction

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pertracker.data.model.TransactionEntity
import java.util.Calendar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionInputScreen(viewModel: TransactionViewModel, onNavigateBack: () -> Unit) {
    val categories by viewModel.categories.collectAsState()
    
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var amount by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var dateInMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var expanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    
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
        topBar = { TopAppBar(title = { Text("Add Transaction") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
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
                    modifier = Modifier.menuAnchor().fillMaxWidth()
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

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("Remarks") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                val cal = Calendar.getInstance().apply { timeInMillis = dateInMillis }
                Text("Date: ${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    if (selectedCategoryId != null && parsedAmount != null) {
                        isSubmitting = true
                        val tx = TransactionEntity(
                            categoryId = selectedCategoryId!!,
                            amount = parsedAmount,
                            transactionDate = dateInMillis,
                            remarks = remarks
                        )
                        viewModel.saveTransaction(tx) { syncResult ->
                            coroutineScope.launch {
                                isSubmitting = false
                                if (syncResult == true) {
                                    snackbarHostState.showSnackbar("Sync Transaction Success")
                                } else if (syncResult == false) {
                                    snackbarHostState.showSnackbar("Sync Failed. Check sheets")
                                }
                                onNavigateBack()
                            }
                        }
                    }
                }
            ) {
                Text(if (isSubmitting) "Saving..." else "Save Transaction")
            }
        }
    }
}
