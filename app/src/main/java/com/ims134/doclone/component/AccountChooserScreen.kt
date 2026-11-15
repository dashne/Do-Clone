package com.ims134.doclone.component


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Direct translation of `res/layout/choose_type_and_account.xml`:
 *
 *   1. Optional description text (GONE when empty)
 *   2. Vertical single-choice account list
 *   3. Horizontal divider
 *   4. Two buttons: "No" (cancel) / "Yes" (OK)
 */
@Composable
fun AccountChooserScreen(
    description: String?,
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onOk: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        // Description — hidden when null/blank, matching `visibility = View.GONE`
        if (!description.isNullOrBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp,
                ),
            )
        }

        // Account list — single-choice radio rows
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .selectableGroup(),
        ) {
            itemsIndexed(items) { index, label ->
                val selected = index == selectedIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected,
                            onClick = { onItemSelected(index) },
                            role = Role.RadioButton,
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = null, // whole row is clickable
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // Divider — was `?android:attr/dividerHorizontal` at 1dp
        HorizontalDivider()

        // Button bar — was `?android:attr/buttonBarStyle`
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            ) {
                // Was `@android:string/no`
                Text(stringResource(android.R.string.no))
            }
            Button(
                onClick = onOk,
                enabled = selectedIndex != -1, // was `button.setEnabled(i != -1)`
                modifier = Modifier.weight(1f),
            ) {
                // Was `@android:string/yes`
                Text(stringResource(android.R.string.yes))
            }
        }
    }
}

// ===== Previews =====

@Preview(name = "With accounts", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewWithAccounts() {
    MaterialTheme {
        AccountChooserScreen(
            description = "Select an account to use with this app",
            items = listOf(
                "alice@gmail.com",
                "bob@gmail.com",
                "carol@outlook.com",
                "Add account",
            ),
            selectedIndex = 1,
            onItemSelected = {},
            onOk = {},
            onCancel = {},
        )
    }
}

@Preview(name = "No description, no selection", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewNoDescription() {
    MaterialTheme {
        AccountChooserScreen(
            description = null,
            items = listOf("alice@gmail.com", "Add account"),
            selectedIndex = -1,
            onItemSelected = {},
            onOk = {},
            onCancel = {},
        )
    }
}

@Preview(name = "Empty (add-account row highlighted)", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewEmptyAddSelected() {
    MaterialTheme {
        AccountChooserScreen(
            description = "No accounts yet — tap Add account to get started",
            items = listOf("Add account"),
            selectedIndex = 0,
            onItemSelected = {},
            onOk = {},
            onCancel = {},
        )
    }
}