package com.example.convinenceapp.data

/**
 * Pre-populated Warhammer armies and paint schemes.
 */
object DefaultWarhammerData {

    fun getDefaultArmies(): List<PaintArmy> {
        return listOf(
            PaintArmy(
                name = "Space Marines (Ultramarines)",
                paints = listOf(
                    PaintItem(name = "Macragge Blue", hex = "#1a4085", paintType = "Base", checked = true),
                    PaintItem(name = "Abaddon Black", hex = "#0a0a0a", paintType = "Base", checked = true),
                    PaintItem(name = "Leadbelcher", hex = "#737373", paintType = "Layer", checked = false),
                    PaintItem(name = "Retributor Armour", hex = "#c8a84a", paintType = "Base", checked = false),
                    PaintItem(name = "Nuln Oil", hex = "#181818", paintType = "Shade", checked = true),
                    PaintItem(name = "Calgar Blue", hex = "#4874b0", paintType = "Layer", checked = false),
                    PaintItem(name = "Mephiston Red", hex = "#8b0000", paintType = "Base", checked = false)
                )
            ),
            PaintArmy(
                name = "Astra Militarum (Imperial Guard)",
                paints = listOf(
                    PaintItem(name = "Death Guard Green", hex = "#7a8c4e", paintType = "Base", checked = false),
                    PaintItem(name = "Zandri Dust", hex = "#a89060", paintType = "Base", checked = false),
                    PaintItem(name = "Leadbelcher", hex = "#737373", paintType = "Layer", checked = false),
                    PaintItem(name = "Abaddon Black", hex = "#0a0a0a", paintType = "Base", checked = true),
                    PaintItem(name = "Agrax Earthshade", hex = "#7a5c30", paintType = "Shade", checked = false),
                    PaintItem(name = "Cadian Fleshtone", hex = "#c97c5e", paintType = "Layer", checked = false),
                    PaintItem(name = "Nuln Oil", hex = "#181818", paintType = "Shade", checked = true)
                )
            ),
            PaintArmy(
                name = "Necrons",
                paints = listOf(
                    PaintItem(name = "Leadbelcher", hex = "#737373", paintType = "Layer", checked = false),
                    PaintItem(name = "Nuln Oil", hex = "#181818", paintType = "Shade", checked = true),
                    PaintItem(name = "Necron Compound", hex = "#a0a8a0", paintType = "Dry", checked = false),
                    PaintItem(name = "Abaddon Black", hex = "#0a0a0a", paintType = "Base", checked = true),
                    PaintItem(name = "Biel-Tan Green", hex = "#2d7230", paintType = "Shade", checked = false)
                )
            ),
            PaintArmy(
                name = "Chaos Space Marines (World Eaters)",
                paints = listOf(
                    PaintItem(name = "Khorne Red", hex = "#6b0f0f", paintType = "Base", checked = false),
                    PaintItem(name = "Retributor Armour", hex = "#c8a84a", paintType = "Base", checked = false),
                    PaintItem(name = "Agrax Earthshade", hex = "#7a5c30", paintType = "Shade", checked = false),
                    PaintItem(name = "Leadbelcher", hex = "#737373", paintType = "Layer", checked = false),
                    PaintItem(name = "Nuln Oil", hex = "#181818", paintType = "Shade", checked = true),
                    PaintItem(name = "Blood for the Blood God", hex = "#800010", paintType = "Technical", checked = false)
                )
            )
        )
    }

    fun createDefaultPaintBoard(id: String = "default_warhammer_paint_board", posX: Float = 100f, posY: Float = 100f): Board {
        return Board(
            id = id,
            title = "Warhammer Armies Paint Checklist",
            type = BoardType.PAINT,
            iconEmoji = "🎨",
            posX = posX,
            posY = posY,
            colorHex = "#E8F5E9",
            isPinned = true,
            paintData = PaintBoardData(armies = getDefaultArmies())
        )
    }
}
