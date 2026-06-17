package com.example.rolecall.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rolecall.R
import com.example.rolecall.network.AuthRepository
import com.example.rolecall.network.TokenManager
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    // State variables
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Sign up vars
    var isSignUpLoading by remember { mutableStateOf(false) }
    val signUpActionSource = remember { MutableInteractionSource() }
    val signUpIsPressed by signUpActionSource.collectIsPressedAsState()
    val signUpContainerColor =
        if (signUpIsPressed || isSignUpLoading)
            MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface

    // Login vars
    var isLoginLoading by remember { mutableStateOf(false) }
    val loginActionSource = remember { MutableInteractionSource() }
    val loginIsPressed by loginActionSource.collectIsPressedAsState()
    val loginContainerColor =
        if (loginIsPressed || isLoginLoading)
            MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.background

    // Coroutine scope for network request
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.rolecall_logo),
                contentDescription = "RoleCall Logo",
                modifier = Modifier
                    .size(164.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(42.dp))

        Column(
            modifier = Modifier.padding(start = 64.dp, end = 64.dp)
        ) {
            // Email Input Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.surface,
                    fontSize = 16.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email,
                    contentDescription = "Email Icon",
                    tint = MaterialTheme.colorScheme.background) },
                trailingIcon = {
                    if (email.isNotEmpty()) {
                        IconButton(
                            onClick = { email = "" },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Clear,
                                contentDescription = "Clear text",
                                tint = MaterialTheme.colorScheme.background,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Input Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(mask = '\u25CF'),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text("Password") },
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.surface,
                    fontSize =16.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                leadingIcon = { Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Key Icon",
                    tint = MaterialTheme.colorScheme.background) },
                trailingIcon = {
                    Row(
                        modifier = Modifier
                            .width(IntrinsicSize.Min)
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val image =
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description =
                            if (passwordVisible) "Hide password" else "Show password"

                        if (password.isNotEmpty()) {
                            // Clear
                            IconButton(
                                onClick = { password = "" },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear text",
                                    tint = MaterialTheme.colorScheme.background,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            // Show
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = description,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sign Up Button
                Button(
                    onClick = {
                        isSignUpLoading = true
                        // background task to authenticate
                        coroutineScope.launch {
                            val token = AuthRepository.signUpWithEmail(
                                email,
                                password,
                                tokenManager
                            )
                            isSignUpLoading = false

                            if (token != null) {
                                // Success
                                onLoginSuccess(token)
                            } else {
                                // TODO: Snackbar or Text error here
                                Log.e("SUPABASE", "Login Failed: Check credentials or network.")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.5f),
                    interactionSource = signUpActionSource,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = signUpContainerColor,
                        disabledContainerColor = signUpContainerColor
                    ),
                    enabled = !isSignUpLoading // Disable button while loading
                ) {
                    if(isSignUpLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Sign Up",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Login Button
                Button(
                    onClick = {
                        isLoginLoading = true
                        // background task to authenticate
                        coroutineScope.launch {
                            val token = AuthRepository.loginWithEmail(
                                email,
                                password,
                                tokenManager)
                            isLoginLoading = false

                            if (token != null) {
                                // Success
                                onLoginSuccess(token)
                            } else {
                                // TODO: Snackbar or Text error here
                                Log.e("SUPABASE", "Login Failed: Check credentials or network.")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = loginContainerColor,
                        disabledContainerColor = loginContainerColor
                    ),
                    enabled = !isLoginLoading // Disable button while loading
                ) {
                    if(isLoginLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Login",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}