package com.example.rolecall.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.rolecall.R
import com.example.rolecall.ui.theme.PrimaryText
import com.example.rolecall.ui.theme.SecondaryText

/**
 * A reusable "─── or ───" divider followed by a "Continue with Google"
 * outlined button. Used on both the Log In and Sign Up screens so the
 * two flows stay visually identical.
 *
 * @param onGoogleClick invoked when the user taps the Google button
 * @param enabled       false while an auth call is already in flight
 */
@Composable
fun ProviderButtonRow(
    onGoogleClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = SecondaryText)
            Text(
                "or",
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = SecondaryText)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onGoogleClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, SecondaryText)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Continue with Google", color = PrimaryText)
        }
    }
}
