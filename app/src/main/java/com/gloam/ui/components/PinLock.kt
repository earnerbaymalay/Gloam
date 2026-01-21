package com.gloam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinLockScreen(
    isSetup: Boolean = false,
    onPinEntered: (String) -> Boolean,
    onBiometricClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val currentPin = if (isConfirming) confirmPin else pin
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Gloam",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = when {
                isSetup && !isConfirming -> "Create your PIN"
                isSetup && isConfirming -> "Confirm your PIN"
                else -> "Enter your PIN"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // PIN dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < currentPin.length)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                )
            }
        }
        
        // Error message
        error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Number pad
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("bio", "0", "del")
            ).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    row.forEach { key ->
                        when (key) {
                            "bio" -> {
                                if (onBiometricClick != null && !isSetup) {
                                    PinKeyButton(
                                        onClick = onBiometricClick,
                                        content = {
                                            Icon(
                                                Icons.Default.Fingerprint,
                                                contentDescription = "Biometric",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(72.dp))
                                }
                            }
                            "del" -> {
                                PinKeyButton(
                                    onClick = {
                                        if (isConfirming && confirmPin.isNotEmpty()) {
                                            confirmPin = confirmPin.dropLast(1)
                                        } else if (!isConfirming && pin.isNotEmpty()) {
                                            pin = pin.dropLast(1)
                                        }
                                        error = null
                                    },
                                    content = {
                                        Icon(
                                            Icons.Default.Backspace,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                )
                            }
                            else -> {
                                PinKeyButton(
                                    onClick = {
                                        error = null
                                        if (isConfirming) {
                                            if (confirmPin.length < 4) {
                                                confirmPin += key
                                                if (confirmPin.length == 4) {
                                                    if (confirmPin == pin) {
                                                        if (onPinEntered(confirmPin)) {
                                                            // Success
                                                        } else {
                                                            error = "Failed to save PIN"
                                                            confirmPin = ""
                                                        }
                                                    } else {
                                                        error = "PINs don't match"
                                                        confirmPin = ""
                                                    }
                                                }
                                            }
                                        } else {
                                            if (pin.length < 4) {
                                                pin += key
                                                if (pin.length == 4) {
                                                    if (isSetup) {
                                                        isConfirming = true
                                                    } else {
                                                        if (!onPinEntered(pin)) {
                                                            error = "Incorrect PIN"
                                                            pin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    content = {
                                        Text(
                                            text = key,
                                            fontSize = 28.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinKeyButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
