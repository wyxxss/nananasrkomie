package com.example.model

import androidx.annotation.DrawableRes
import com.example.R

enum class ColorAdaptationMode(val title: String, val description: String) {
    WALLPAPER_DYNAMIC(
        title = "Обои Pixel",
        description = "Автоматическая палитра Material You из обоев системы"
    ),
    ALBUM_ART_CHAMELEON(
        title = "Цвет обложки",
        description = "Динамический подбор акцентов под текущий трек"
    ),
    CURATED_PRESET(
        title = "Студийная",
        description = "Выбранная вручную выразительная цветовая тема"
    )
}

enum class ExpressiveShapeStyle(val title: String) {
    SCALLOPED_FLOWER("Цветок"),
    FOUR_SIDED_COOKIE("Печенька"),
    SQUIRCLE("Сквиркл"),
    STADIUM_PILL("Капсула")
}

enum class SurfaceContrastMode(val title: String, val subtitle: String) {
    TONAL_SOFT("Мягкий", "Деликатные тональные слои"),
    EXPRESSIVE_VIBRANT("Яркий", "Насыщенные акценты Material 3"),
    HIGH_CONTRAST("Контрастный", "Максимальная читаемость на ярких обоях")
}

enum class StudioPreset(
    val id: String,
    val label: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val tertiaryHex: Long,
    val surfaceHex: Long
) {
    PIXEL_CORAL("pixel_coral", "Коралл Pixel", 0xFFFF6B4A, 0xFFD9B8FF, 0xFFFFB74D, 0xFF211A1E),
    MINT_SYNTH("mint_synth", "Мятный синт", 0xFF3CD4A0, 0xFF8BB8FF, 0xFF9DF069, 0xFF131D1A),
    MIDNIGHT_PLUM("midnight_plum", "Ночная слива", 0xFFD0BCFF, 0xFFF2B8B5, 0xFF7DD8E8, 0xFF1C1824),
    TERRACOTTA_TAPE("terracotta_tape", "Терракота", 0xFFE07A5F, 0xFF81B29A, 0xFFF2CC8F, 0xFF231B18)
}

enum class WidgetKind(
    val id: String,
    val title: String,
    val subtitle: String,
    val gridLabel: String
) {
    VINYL_BLOOM(
        id = "vinyl_bloom",
        title = "Винил-цветок",
        subtitle = "Фестонный диск с вырезанной обложкой и адаптивным центром",
        gridLabel = "2×2 — 4×2"
    ),
    PILL_DECK(
        id = "pill_deck",
        title = "Экспрессив-дека",
        subtitle = "Асимметричный макет с прогрессом и тактильной панелью",
        gridLabel = "4×2"
    ),
    CLOVER_CONTROL(
        id = "clover_control",
        title = "Клевер-студия",
        subtitle = "Четырёхлепестковая форма Material 3 Expressive с крупным центром",
        gridLabel = "2×2 — 3×3"
    ),
    WAVE_BAR(
        id = "wave_bar",
        title = "Волновой бар",
        subtitle = "Элегантная капсула для док-зоны с волновой графикой",
        gridLabel = "4×1"
    )
}

data class DemoTrack(
    val id: Int,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    @DrawableRes val artRes: Int,
    val baseFreqHz: Double,
    val tempoBpm: Int
)

object DemoCatalog {
    val tracks = listOf(
        DemoTrack(
            id = 0,
            title = "Bloom Reverie",
            artist = "Kora & Pastel",
            album = "Material Echoes",
            durationMs = 214_000L,
            artRes = R.drawable.album_art_vinyl_bloom_1794518099661,
            baseFreqHz = 261.63, // C4 major7 ambient
            tempoBpm = 92
        ),
        DemoTrack(
            id = 1,
            title = "Neon Current",
            artist = "Aether Circuit",
            album = "Pixel Nocturne",
            durationMs = 198_000L,
            artRes = R.drawable.album_art_neon_pulse_1794518100522,
            baseFreqHz = 293.66, // D4 synthwave
            tempoBpm = 118
        ),
        DemoTrack(
            id = 2,
            title = "Terracotta Tape",
            artist = "Solstice Radio",
            album = "Warm Analog",
            durationMs = 242_000L,
            artRes = R.drawable.album_art_sunset_tape_1794518100396,
            baseFreqHz = 220.00, // A3 warm lo-fi
            tempoBpm = 84
        )
    )
}

data class NowPlayingState(
    val title: String = DemoCatalog.tracks[0].title,
    val artist: String = DemoCatalog.tracks[0].artist,
    val album: String = DemoCatalog.tracks[0].album,
    val isPlaying: Boolean = false,
    val positionMs: Long = 64_000L,
    val durationMs: Long = DemoCatalog.tracks[0].durationMs,
    val sourcePackage: String = "",
    val sourceAppName: String = "ToneForm Synth",
    val demoTrackIndex: Int = 0,
    val hasExternalSession: Boolean = false,
    val artRevision: Int = 0
) {
    val progressFraction: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}

data class WidgetCustomizationState(
    val colorMode: ColorAdaptationMode = ColorAdaptationMode.WALLPAPER_DYNAMIC,
    val shapeStyle: ExpressiveShapeStyle = ExpressiveShapeStyle.SCALLOPED_FLOWER,
    val contrastMode: SurfaceContrastMode = SurfaceContrastMode.EXPRESSIVE_VIBRANT,
    val selectedPreset: StudioPreset = StudioPreset.PIXEL_CORAL,
    val showProgress: Boolean = true,
    val showSourceBadge: Boolean = true,
    val showWaveform: Boolean = true,
    val revision: Int = 0
)
