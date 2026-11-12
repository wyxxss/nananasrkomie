package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ColorAdaptationMode
import com.example.model.ExpressiveShapeStyle
import com.example.model.StudioPreset
import com.example.model.SurfaceContrastMode
import com.example.ui.ToneFormUiState
import com.example.ui.theme.ExpressivePolygons
import com.example.ui.theme.RoundedPolygonShape

@Composable
fun PaletteLabScreen(
    uiState: ToneFormUiState,
    onColorModeSelected: (ColorAdaptationMode) -> Unit,
    onShapeSelected: (ExpressiveShapeStyle) -> Unit,
    onContrastSelected: (SurfaceContrastMode) -> Unit,
    onPresetSelected: (StudioPreset) -> Unit,
    onToggleShowProgress: (Boolean) -> Unit,
    onToggleShowWaveform: (Boolean) -> Unit,
    onToggleShowSourceBadge: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val custom = uiState.customization

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Лаборатория стиля",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.onBackground
                    )
                    Text(
                        text = "Настройка палитры, геометрии Material 3 Expressive и слоёв виджетов",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            // Live Material You Swatch Inspector
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = colors.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Активные токены Material You",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onSurface
                            )
                            Surface(
                                shape = CircleShape,
                                color = colors.primaryContainer
                            ) {
                                Text(
                                    text = if (uiState.wallpaperSwatches?.hasLiveWallpaperColors == true)
                                        "Pixel Обои OK" else "Динамика M3",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TokenSwatchTile("Primary", colors.primary, colors.onPrimary, Modifier.weight(1f))
                            TokenSwatchTile("Secondary", colors.secondary, colors.onSecondary, Modifier.weight(1f))
                            TokenSwatchTile("Tertiary", colors.tertiary, colors.onTertiary, Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TokenSwatchTile("Container", colors.primaryContainer, colors.onPrimaryContainer, Modifier.weight(1f))
                            TokenSwatchTile("Sec. Cont", colors.secondaryContainer, colors.onSecondaryContainer, Modifier.weight(1f))
                            TokenSwatchTile("Tert. Cont", colors.tertiaryContainer, colors.onTertiaryContainer, Modifier.weight(1f))
                        }
                    }
                }
            }

            // Color Adaptation Mode
            item {
                SectionHeader("Источник цветов")
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorAdaptationMode.entries.forEach { mode ->
                        val selected = custom.colorMode == mode
                        Surface(
                            onClick = { onColorModeSelected(mode) },
                            shape = RoundedCornerShape(22.dp),
                            color = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (selected) Modifier.border(2.dp, colors.primary, RoundedCornerShape(22.dp))
                                    else Modifier
                                )
                                .testTag("lab_colormode_${mode.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (selected) colors.onPrimaryContainer else colors.onSurface
                                    )
                                    Text(
                                        text = mode.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selected) colors.onPrimaryContainer.copy(alpha = 0.8f)
                                        else colors.onSurfaceVariant
                                    )
                                }
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(colors.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "Выбрано",
                                            tint = colors.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Studio Expressive Presets
            item {
                SectionHeader("Студийные палитры")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudioPreset.entries.forEach { preset ->
                        val active = custom.colorMode == ColorAdaptationMode.CURATED_PRESET &&
                            custom.selectedPreset == preset
                        Surface(
                            onClick = { onPresetSelected(preset) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (active) colors.secondaryContainer else colors.surfaceContainerLow,
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (active) Modifier.border(2.dp, colors.primary, RoundedCornerShape(20.dp))
                                    else Modifier
                                )
                                .testTag("preset_${preset.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color(preset.primaryHex))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color(preset.tertiaryHex))
                                    )
                                }
                                Text(
                                    text = preset.label.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Expressive Shape Geometry Selector
            item {
                SectionHeader("Форма обложки и акцентов (M3 Expressive)")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpressiveShapeStyle.entries.forEach { shapeStyle ->
                        val active = custom.shapeStyle == shapeStyle
                        val polyShape = remember(shapeStyle) {
                            RoundedPolygonShape(ExpressivePolygons.polygonFor(shapeStyle))
                        }
                        Surface(
                            onClick = { onShapeSelected(shapeStyle) },
                            shape = RoundedCornerShape(22.dp),
                            color = if (active) colors.primaryContainer else colors.surfaceContainerLow,
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (active) Modifier.border(2.dp, colors.primary, RoundedCornerShape(22.dp))
                                    else Modifier
                                )
                                .testTag("shape_${shapeStyle.name}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(polyShape)
                                        .background(if (active) colors.primary else colors.secondary)
                                )
                                Text(
                                    text = shapeStyle.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) colors.onPrimaryContainer else colors.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Surface Contrast Mode
            item {
                SectionHeader("Контраст поверхности")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SurfaceContrastMode.entries.forEach { contrast ->
                        val active = custom.contrastMode == contrast
                        Surface(
                            onClick = { onContrastSelected(contrast) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (active) colors.tertiaryContainer else colors.surfaceContainerLow,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("contrast_${contrast.name}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = contrast.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (active) colors.onTertiaryContainer else colors.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Detail Toggles
            item {
                SectionHeader("Элементы виджета")
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = colors.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        SettingSwitchRow(
                            title = "Индикатор прогресса трека",
                            checked = custom.showProgress,
                            onCheckedChange = onToggleShowProgress,
                            testTag = "switch_progress"
                        )
                        SettingSwitchRow(
                            title = "Волновая графика эквалайзера",
                            checked = custom.showWaveform,
                            onCheckedChange = onToggleShowWaveform,
                            testTag = "switch_waveform"
                        )
                        SettingSwitchRow(
                            title = "Бейдж музыкального плеера",
                            checked = custom.showSourceBadge,
                            onCheckedChange = onToggleShowSourceBadge,
                            testTag = "switch_badge"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun TokenSwatchTile(
    label: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val hex = remember(bgColor) {
        String.format("#%06X", 0xFFFFFF and bgColor.toArgb())
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        modifier = modifier.height(62.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                maxLines = 1
            )
            Text(
                text = hex,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
