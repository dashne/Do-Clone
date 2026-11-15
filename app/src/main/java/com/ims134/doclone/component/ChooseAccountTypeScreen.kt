package com.ims134.doclone.component


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.accounts.AuthenticatorDescription
import com.ims134.doclone.clone.AccountTypeItem

/**
 * Direct translation of `res/layout/choose_account_type.xml`:
 *
 *   1. 3dp dark bar at the top (`#323232`)
 *   2. Vertical list of account types — each row is [icon] [label]
 */
@Composable
fun ChooseAccountTypeScreen(
    items: List<AccountTypeItem>,
    onItemClick: (AccountTypeItem) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        // Top bar — was `<View background="#323232" height="3dp">`
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color(0xFF323232)),
        )
        // Account type list — was the ListView with `choose_account_row` items
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            items(items) { item ->
                AccountRow(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun AccountRow(item: AccountTypeItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Was `dVar.Stub.setImageDrawable(...)` — the ImageView is `wrap_content`,
        // so Stub null drawable collapses to zero width. Match that here.
        if (item.icon != null) {
            Image(
                bitmap = item.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.width(16.dp))
        }
        // Was `dVar.b.setText(...)`. Compose's Text rejects null, so fall back
        // to the type string (which is always non-null) when the label failed
        // to load.
        Text(
            text = item.label ?: item.desc.type,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

// ===== Preview =====

@Preview(showBackground = true, showSystemUi = true, name = "Three account types")
@Composable
private fun PreviewChooseAccountType() {
    val sample = listOf(
        AccountTypeItem(
            desc = fakeDescription("com.google", "com.google.android.gms"),
            label = "Google",
            icon = null,
        ),
        AccountTypeItem(
            desc = fakeDescription("com.android.exchange", "com.android.exchange"),
            label = "Corporate (Exchange)",
            icon = null,
        ),
        AccountTypeItem(
            desc = fakeDescription("com.android.email", "com.android.email"),
            label = "Email",
            icon = null,
        ),
    )
    MaterialTheme {
        ChooseAccountTypeScreen(sample) {}
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Single account type")
@Composable
private fun PreviewSingleAccountType() {
    val sample = listOf(
        AccountTypeItem(
            desc = fakeDescription("com.google", "com.google.android.gms"),
            label = "Google",
            icon = null,
        ),
    )
    MaterialTheme {
        ChooseAccountTypeScreen(sample) {}
    }
}

private fun fakeDescription(type: String, pkg: String) = AuthenticatorDescription(
    type,
    pkg,
    0, // labelId
    0, // iconId
    0, // smallIconId
    0, // accountPreferencesId
    false, // customTokens
)