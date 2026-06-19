package com.cengizhan.rise.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cengizhan.rise.presentation.auth.AgeSetupScreen
import com.cengizhan.rise.presentation.auth.AuthScreen
import com.cengizhan.rise.presentation.auth.CountrySetupScreen
import com.cengizhan.rise.presentation.auth.CreateAccountScreen
import com.cengizhan.rise.presentation.auth.GenderSetupScreen
import com.cengizhan.rise.presentation.auth.LoginScreen
import com.cengizhan.rise.presentation.home.MainScreen
import com.cengizhan.rise.presentation.onboarding.OnboardingScreen
import com.cengizhan.rise.presentation.splash.SplashScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Auth : Screen("auth")
    data object Login : Screen("login")
    data object CreateAccount : Screen("create_account")
    data object SetupAge : Screen("setup_age")
    data object SetupCountry : Screen("setup_country")
    data object SetupGender : Screen("setup_gender")
    data object Main : Screen("main")
}

@Composable
fun RiseNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStartedClick = {
                    navController.navigate(Screen.Auth.route)
                }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onCreateAccountClick = {
                    navController.navigate(Screen.CreateAccount.route)
                },
                onContinueAsGuestClick = {
                    navController.navigate(Screen.SetupAge.route)
                },
                onContinueWithGoogleClick = {
                    navController.navigate(Screen.SetupAge.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.SetupAge.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CreateAccount.route) {
            CreateAccountScreen(
                onAccountCreated = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.SetupAge.route) {
            AgeSetupScreen(
                onContinueClick = {
                    navController.navigate(Screen.SetupCountry.route)
                }
            )
        }

        composable(Screen.SetupCountry.route) {
            CountrySetupScreen(
                onContinueClick = {
                    navController.navigate(Screen.SetupGender.route)
                }
            )
        }

        composable(Screen.SetupGender.route) {
            GenderSetupScreen(
                onFinishClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen()
        }
    }
}
