package com.jeet.island.settings

data class GlassSettings(
    val glassEnabled: Boolean = true,
    val glassOpacity: Float = 0.55f,
    val borderAlpha: Float = 0.10f,
    val highlightStrength: Float = 0.60f,
    val noiseAmount: Float = 0.03f,
    val tintHue: Float = 0f,
    val cornerSmoothing: Float = 0.5f
) {
    companion object {
        val CLASSIC_BLACK = GlassSettings(
            glassEnabled = false,
            glassOpacity = 1.0f, borderAlpha = 0f, highlightStrength = 0f,
            noiseAmount = 0f, tintHue = 0f, cornerSmoothing = 0.5f
        )
        val FROSTED = GlassSettings()
        val CRYSTAL = GlassSettings(
            glassOpacity = 0.35f, borderAlpha = 0.18f, highlightStrength = 0.9f,
            noiseAmount = 0.02f, tintHue = 0f
        )
        val SMOKE = GlassSettings(
            glassOpacity = 0.75f, borderAlpha = 0.05f, highlightStrength = 0.3f,
            noiseAmount = 0f, tintHue = 30f
        )
    }
}

enum class GlassPreset(val label: String, val settings: GlassSettings) {
    CLASSIC_BLACK("Classic Black", GlassSettings.CLASSIC_BLACK),
    FROSTED("Frosted", GlassSettings.FROSTED),
    CRYSTAL("Crystal", GlassSettings.CRYSTAL),
    SMOKE("Smoke", GlassSettings.SMOKE)
}
