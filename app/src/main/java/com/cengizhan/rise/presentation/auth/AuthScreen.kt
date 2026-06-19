package com.cengizhan.rise.presentation.auth

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cengizhan.rise.core.LocalRiseColors

@Composable
fun AuthScreen(
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onContinueAsGuestClick: () -> Unit,
    onContinueWithGoogleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalRiseColors.current.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Rise",
            color = LocalRiseColors.current.primaryText,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Start building your discipline system today.",
            color = LocalRiseColors.current.secondaryText,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        PrimaryAuthButton(
            text = "Login",
            onClick = onLoginClick
        )

        Spacer(modifier = Modifier.height(14.dp))

        PrimaryAuthButton(
            text = "Create Account",
            onClick = onCreateAccountClick
        )

        Spacer(modifier = Modifier.height(14.dp))

        SecondaryAuthButton(
            text = "Continue as Guest",
            onClick = onContinueAsGuestClick
        )

        Spacer(modifier = Modifier.height(14.dp))

        SecondaryAuthButton(
            text = "Continue with Google",
            onClick = onContinueWithGoogleClick
        )
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthFormScaffold(
        title = "Login",
        subtitle = "Continue your discipline system.",
        onBackClick = onBackClick
    ) {
        RiseTextField(
            value = emailOrUsername,
            onValueChange = { emailOrUsername = it },
            label = "Email or username"
        )

        Spacer(modifier = Modifier.height(14.dp))

        RiseTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(28.dp))

        PrimaryAuthButton(
            text = "Login",
            onClick = onLoginSuccess
        )
    }
}

@Composable
fun CreateAccountScreen(
    onAccountCreated: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthFormScaffold(
        title = "Create Account",
        subtitle = "Save your starter profile locally for now.",
        onBackClick = onBackClick
    ) {
        RiseTextField(
            value = name,
            onValueChange = { name = it },
            label = "Name"
        )

        Spacer(modifier = Modifier.height(14.dp))

        RiseTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email"
        )

        Spacer(modifier = Modifier.height(14.dp))

        RiseTextField(
            value = username,
            onValueChange = { username = it },
            label = "Username"
        )

        Spacer(modifier = Modifier.height(14.dp))

        RiseTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(28.dp))

        PrimaryAuthButton(
            text = "Create Account",
            onClick = {
                context.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString("accountName", name)
                    .putString("accountEmail", email)
                    .putString("accountUsername", username)
                    .apply()
                onAccountCreated()
            }
        )
    }
}

@Composable
private fun AuthFormScaffold(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalRiseColors.current.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            color = LocalRiseColors.current.primaryText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = subtitle,
            color = LocalRiseColors.current.secondaryText,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        content()

        Spacer(modifier = Modifier.height(14.dp))

        SecondaryAuthButton(
            text = "Back",
            onClick = onBackClick
        )
    }
}

@Composable
private fun RiseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation =
            if (isPassword)
                PasswordVisualTransformation()
            else
                androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = LocalRiseColors.current.primaryText,
            unfocusedTextColor = LocalRiseColors.current.primaryText,
            focusedLabelColor = LocalRiseColors.current.primaryText,
            unfocusedLabelColor = LocalRiseColors.current.secondaryText,
            focusedBorderColor = LocalRiseColors.current.primaryText,
            unfocusedBorderColor = LocalRiseColors.current.outline,
            cursorColor = LocalRiseColors.current.primaryText
        )
    )
}

@Composable
internal fun PrimaryAuthButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LocalRiseColors.current.primaryButton,
            disabledContainerColor = LocalRiseColors.current.outline
        )
    ) {
        Text(
            text = text,
            color = if (enabled) LocalRiseColors.current.primaryButtonText else LocalRiseColors.current.secondaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun SecondaryAuthButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = LocalRiseColors.current.primaryText
        )
    ) {
        Text(
            text = text,
            color = LocalRiseColors.current.primaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private const val AUTH_PREFS_NAME = "rise_auth"

