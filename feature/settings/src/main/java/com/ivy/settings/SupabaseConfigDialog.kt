package com.ivy.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SupabaseConfigDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSave: (url: String, anonKey: String, tablePrefix: String) -> Unit,
    onClear: () -> Unit,
    isConfigured: Boolean
) {
    if (!visible) return

    var url by remember { mutableStateOf("") }
    var anonKey by remember { mutableStateOf("") }
    var tablePrefix by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Supabase Configuration",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Configure your Supabase backend connection. These credentials will be stored securely on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Supabase URL") },
                    placeholder = { Text("https://your-project.supabase.co") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = anonKey,
                    onValueChange = { anonKey = it },
                    label = { Text("Anonymous Key") },
                    placeholder = { Text("eyJ...") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tablePrefix,
                    onValueChange = { tablePrefix = it },
                    label = { Text("Table Prefix (Optional)") },
                    placeholder = { Text("dev_") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (isConfigured) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "⚠️ Supabase is already configured",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (url.isNotBlank() && anonKey.isNotBlank()) {
                        onSave(url.trim(), anonKey.trim(), tablePrefix.trim())
                        onDismiss()
                    }
                },
                enabled = url.isNotBlank() && anonKey.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Column {
                if (isConfigured) {
                    TextButton(
                        onClick = {
                            onClear()
                            onDismiss()
                        }
                    ) {
                        Text("Clear Config")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
