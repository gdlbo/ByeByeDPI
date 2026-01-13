package io.github.dovecoteescapee.byedpi.activities

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


@Serializable
data object Home : NavKey
@Serializable
data object Settings : NavKey
@Serializable
data object SettingsCmd : NavKey
@Serializable
data object SettingsUI : NavKey
@Serializable
data object SettingsApps : NavKey
@Serializable
data object Test : NavKey
@Serializable
data object SettingsTest : NavKey

