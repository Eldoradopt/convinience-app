package com.example.convinenceapp.data

import androidx.compose.ui.graphics.Color

/**
 * Representative subset of the Citadel / Warhammer paint range.
 * Structure: name -> (hex, paintType)
 */
object PaintDatabase {

    data class PaintEntry(val name: String, val hex: String, val paintType: String) {
        fun toColor(): Color {
            val clean = hex.trimStart('#')
            val long = clean.toLongOrNull(16) ?: 0xFFAAAAAA
            return Color(
                red = ((long shr 16) and 0xFF) / 255f,
                green = ((long shr 8) and 0xFF) / 255f,
                blue = (long and 0xFF) / 255f
            )
        }
    }

    val PAINTS: List<PaintEntry> = listOf(
        // Base
        PaintEntry("Mephiston Red",       "#8b0000", "Base"),
        PaintEntry("Abaddon Black",       "#0a0a0a", "Base"),
        PaintEntry("Averland Sunset",     "#f4b400", "Base"),
        PaintEntry("Caledor Sky",         "#1e5fa0", "Base"),
        PaintEntry("Celestra Grey",       "#9caea8", "Base"),
        PaintEntry("Corax White",         "#e8e8e8", "Base"),
        PaintEntry("Daemonette Hide",     "#726081", "Base"),
        PaintEntry("Death Guard Green",   "#7a8c4e", "Base"),
        PaintEntry("Dryad Bark",          "#4a3320", "Base"),
        PaintEntry("Incubi Darkness",     "#1b4a47", "Base"),
        PaintEntry("Iron Warriors",       "#787878", "Base"),
        PaintEntry("Jokaero Orange",      "#d9481c", "Base"),
        PaintEntry("Kantor Blue",         "#0d2b6b", "Base"),
        PaintEntry("Khorne Red",          "#6b0f0f", "Base"),
        PaintEntry("Macragge Blue",       "#1a4085", "Base"),
        PaintEntry("Mechanicus Standard Grey", "#555f61", "Base"),
        PaintEntry("Mournfang Brown",     "#5e2408", "Base"),
        PaintEntry("Naggaroth Night",     "#36204a", "Base"),
        PaintEntry("Rhinox Hide",         "#3f2015", "Base"),
        PaintEntry("Screamer Pink",       "#9e2d5a", "Base"),
        PaintEntry("Steel Legion Drab",   "#796646", "Base"),
        PaintEntry("The Fang",            "#335c6e", "Base"),
        PaintEntry("Thousand Sons Blue",  "#1e6a9c", "Base"),
        PaintEntry("Waaagh! Flesh",       "#235c36", "Base"),
        PaintEntry("Wraithbone",          "#f5e6d3", "Base"),
        PaintEntry("Zandri Dust",         "#a89060", "Base"),
        // Layer
        PaintEntry("Altdorf Guard Blue",  "#1e4fa0", "Layer"),
        PaintEntry("Administratum Grey",  "#adb5b8", "Layer"),
        PaintEntry("Balor Brown",         "#8b6914", "Layer"),
        PaintEntry("Bloodreaver Flesh",   "#c46044", "Layer"),
        PaintEntry("Cadian Fleshtone",    "#c97c5e", "Layer"),
        PaintEntry("Calgar Blue",         "#4874b0", "Layer"),
        PaintEntry("Evil Sunz Scarlet",   "#c40c0c", "Layer"),
        PaintEntry("Fire Dragon Bright",  "#e8440a", "Layer"),
        PaintEntry("Flash Gitz Yellow",   "#f5e200", "Layer"),
        PaintEntry("Leadbelcher",         "#737373", "Layer"),
        PaintEntry("Liberator Gold",      "#c8a84a", "Layer"),
        PaintEntry("Lugganath Orange",    "#e88024", "Layer"),
        PaintEntry("Russ Grey",           "#6b8fa0", "Layer"),
        PaintEntry("Screaming Skull",     "#dcd8b2", "Layer"),
        PaintEntry("Temple Guard Blue",   "#3499a0", "Layer"),
        PaintEntry("Ushabti Bone",        "#c9b87a", "Layer"),
        PaintEntry("Wazdakka Red",        "#a01418", "Layer"),
        PaintEntry("Wild Rider Red",      "#d94020", "Layer"),
        PaintEntry("Wych Flesh",          "#e0a895", "Layer"),
        PaintEntry("Yriel Yellow",        "#f8c000", "Layer"),
        // Shade
        PaintEntry("Agrax Earthshade",    "#7a5c30", "Shade"),
        PaintEntry("Athonian Camoshade",  "#6b7c3f", "Shade"),
        PaintEntry("Biel-Tan Green",      "#2d7230", "Shade"),
        PaintEntry("Carroburg Crimson",   "#8b2040", "Shade"),
        PaintEntry("Drakenhof Nightshade","#203060", "Shade"),
        PaintEntry("Druchi Violet",       "#502870", "Shade"),
        PaintEntry("Fuegan Orange",       "#c04010", "Shade"),
        PaintEntry("Nuln Oil",            "#181818", "Shade"),
        PaintEntry("Reikland Fleshshade", "#b06040", "Shade"),
        PaintEntry("Seraphim Sepia",      "#806020", "Shade"),
        PaintEntry("Soulblight Grey",     "#505860", "Shade"),
        // Dry
        PaintEntry("Praxeti White",       "#e8e6e0", "Dry"),
        PaintEntry("Necron Compound",     "#a0a8a0", "Dry"),
        PaintEntry("Terminatus Stone",    "#c0b898", "Dry"),
        PaintEntry("Tyrant Skull",        "#d8d0b0", "Dry"),
        PaintEntry("Underhive Ash",       "#b0a888", "Dry"),
        PaintEntry("Kindleflame",         "#d07030", "Dry"),
        // Technical
        PaintEntry("Blood for the Blood God", "#800010", "Technical"),
        PaintEntry("Lahmian Medium",      "#c8c0b8", "Technical"),
        PaintEntry("Nihilakh Oxide",      "#48b8a0", "Technical"),
        PaintEntry("Nurgles Rot",         "#788010", "Technical"),
        PaintEntry("Agrellan Earth",      "#987850", "Technical"),
        PaintEntry("Contrast Aethermatic Blue", "#3860b8", "Technical"),
    )

    val types: List<String> = PAINTS.map { it.paintType }.distinct().sorted()

    fun byType(type: String): List<PaintEntry> = PAINTS.filter { it.paintType == type }
    fun byName(name: String): PaintEntry? = PAINTS.firstOrNull { it.name.equals(name, ignoreCase = true) }
    fun search(query: String): List<PaintEntry> =
        PAINTS.filter { it.name.contains(query, ignoreCase = true) || it.paintType.contains(query, ignoreCase = true) }
}
