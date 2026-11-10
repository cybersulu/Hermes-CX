package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .testTag("dialog_privacy_policy")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Privacy Policy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Effective Date: November 9, 2026",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PolicySection(
                        title = "1. Overview",
                        body = "Hermes CX is a currency and unit converter application. We respect your privacy and are committed to protecting it. The app operates primarily on your device and does not collect, store, sell, or share any personal information."
                    )
                    PolicySection(
                        title = "2. No Personal Data Collection",
                        body = "We do not collect, request, or store any personally identifiable information (PII) such as your name, email address, phone number, location, or contacts. No user account or sign-in is required."
                    )
                    PolicySection(
                        title = "3. Local Device Storage",
                        body = "To support offline conversions and save your preferences, the app stores cached exchange rates, pinned currencies, saved currency pairs, selected units, theme settings, and calculation options locally on your device. This data never leaves your device and can be cleared anytime via Android App Settings or by uninstalling the app."
                    )
                    PolicySection(
                        title = "4. Permissions Used",
                        body = "• INTERNET: Used solely to fetch public foreign exchange rates over secure HTTPS.\n• ACCESS_NETWORK_STATE: Used to detect offline status and fall back to cached rates."
                    )
                    PolicySection(
                        title = "5. Third-Party Services",
                        body = "The app queries the public ExchangeRate-API (open.er-api.com) over HTTPS solely to retrieve current exchange rates. No advertising SDKs, analytics trackers, or user profiling tools are used."
                    )
                    PolicySection(
                        title = "6. Children's Privacy & Security",
                        body = "The app does not collect personal data from anyone, including children under 13. All exchange rate fetches use encrypted HTTPS connections."
                    )
                    PolicySection(
                        title = "7. Contact",
                        body = "For questions about this Privacy Policy, please contact: pete.teoh@gmail.com"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_close_privacy_policy")
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
