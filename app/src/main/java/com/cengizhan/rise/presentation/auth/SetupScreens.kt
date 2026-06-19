package com.cengizhan.rise.presentation.auth

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cengizhan.rise.core.LocalRiseColors

@Composable
fun AgeSetupScreen(
    onContinueClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedAgeRange by remember { mutableStateOf("") }

    SetupSelectionScreen(
        title = "Choose your age range",
        subtitle = "This helps personalize your discipline system.",
        options = listOf("13-17", "18-24", "25-34", "35-44", "45+"),
        selectedOption = selectedAgeRange,
        onOptionSelected = { selectedAgeRange = it },
        buttonText = "Continue",
        onContinueClick = {
            saveProfileValue(context, "ageRange", selectedAgeRange)
            onContinueClick()
        }
    )
}

@Composable
fun CountrySetupScreen(
    onContinueClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedCountry by remember { mutableStateOf("") }

    SetupSelectionScreen(
        title = "Choose your country",
        subtitle = "Set up your basic profile.",
        options = listOf("United States", "United Kingdom", "Canada", "Germany", "Turkey", "Other"),
        selectedOption = selectedCountry,
        onOptionSelected = { selectedCountry = it },
        buttonText = "Continue",
        onContinueClick = {
            saveProfileValue(context, "country", selectedCountry)
            onContinueClick()
        }
    )
}

@Composable
fun GenderSetupScreen(
    onFinishClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedGender by remember { mutableStateOf("") }

    SetupSelectionScreen(
        title = "Choose your gender",
        subtitle = "Finish your Rise profile setup.",
        options = listOf("Male", "Female", "Other", "Prefer not to say"),
        selectedOption = selectedGender,
        onOptionSelected = { selectedGender = it },
        buttonText = "Finish",
        onContinueClick = {
            context.getSharedPreferences(PROFILE_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("gender", selectedGender)
                .putBoolean("isProfileSetupCompleted", true)
                .apply()
            onFinishClick()
        }
    )
}

@Composable
private fun SetupSelectionScreen(
    title: String,
    subtitle: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    buttonText: String,
    onContinueClick: () -> Unit
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
            fontSize = 31.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = subtitle,
            color = LocalRiseColors.current.secondaryText,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        options.forEach { option ->
            SetupOptionCard(
                text = option,
                selected = selectedOption == option,
                onClick = {
                    onOptionSelected(option)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        PrimaryAuthButton(
            text = buttonText,
            enabled = selectedOption.isNotBlank(),
            onClick = onContinueClick
        )
    }
}

@Composable
private fun SetupOptionCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (selected)
                    LocalRiseColors.current.primaryButton
                else
                    LocalRiseColors.current.card
        )
    ) {
        Text(
            text = text,
            color =
                if (selected)
                    LocalRiseColors.current.primaryButtonText
                else
                    LocalRiseColors.current.primaryText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
    }
}

private fun saveProfileValue(
    context: Context,
    key: String,
    value: String
) {
    context.getSharedPreferences(PROFILE_PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(key, value)
        .apply()
}

private const val PROFILE_PREFS_NAME = "rise_profile_setup"

