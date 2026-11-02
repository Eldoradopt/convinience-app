package com.example.convinenceapp.data

/**
 * Pre-populated Warhammer armies and paint schemes.
 */
object DefaultWarhammerData {

    fun getDefaultArmies(): List<PaintArmy> {
        return listOf(
            PaintArmy(
                name = "Space Marines (Ultramarines)",
                posX = 40f,
                posY = 40f,
                paints = listOf(
                    PaintItem(name = "Macragge Blue", hex = "#1a4085", paintType = "Base", checked = true, wantToBuy = false),
                    PaintItem(name = "Abaddon Black", hex = "#0a0a0a", paintType = "Base", checked = true, wantToBuy = false),
                    PaintItem(name = "Leadbelcher", hex = "#737373", paintType = "Layer", checked = false, wantToBuy = true),
                    PaintItem(name = "Retributor Armour", hex = "#c8a84a", paintType = "Base", checked = false, wantToBuy = true),
                    PaintItem(name = "Nuln Oil", hex = "#181818", paintType = "Shade", checked = true, wantToBuy = false),
                    PaintItem(name = "Calgar Blue", hex = "#4874b0", paintType = "Layer", checked = false, wantToBuy = true),
                    PaintItem(name = "Mephiston Red", hex = "#8b0000", paintType = "Base", checked = false, wantToBuy = true)
                )
            ),
            PaintArmy(
                name = "Astra Militarum (Imperial Guard)",
                posX = 350f,
                posY = 40f,
                paints = listOf(
                    PaintItem(name = "Death Guard Green", hex = "#7a8c4e", paintType = "Base", checked = false, wantToBuy = true),
                    PaintItem(name = "Zandri Dust", hex = "#a89060", paintType = "Base", checked = false, wantToBuy = true),
                    PaintItem(name = "Leadbelcher", hex = "#737373", paintType = "Layer", checked = false, wantToBuy = true),
                    PaintItem(name = "Abaddon Black", hex = "#0a0a0a", paintType = "Base", checked = true, wantToBuy = false),
                    PaintItem(name = "Agrax Earthshade", hex = "#7a5c30", paintType = "Shade", checked = false, wantToBuy = true),
                    PaintItem(name = "Cadian Fleshtone", hex = "#c97c5e", paintType = "Layer", checked = false, wantToBuy = true),
                    PaintItem(name = "Nuln Oil", hex = "#181818", paintType = "Shade", checked = true, wantToBuy = false)
                )
            ),
            PaintArmy(
                name = "Death Korps of Krieg",
                posX = 40f,
                posY = 420f,
                paints = listOf(
                    PaintItem(name = "The Fang", hex = "#335c6e", paintType = "Base", checked = false, wantToBuy = true),
                    PaintItem(name = "Russ Grey", hex = "#6b8fa0", paintType = "Layer", checked = false, wantToBuy = true),
                    PaintItem(name = "Steel Legion Drab", hex = "#796646", paintType = "Base", checked = false, wantToBuy = true),
                    PaintItem(name = "Leadbelcher", hex = "#737373", paintType = "Layer", checked = false, wantToBuy = true),
                    PaintItem(name = "Nuln Oil", hex = "#181818", paintType = "Shade", checked = true, wantToBuy = false),
                    PaintItem(name = "Agrax Earthshade", hex = "#7a5c30", paintType = "Shade", checked = false, wantToBuy = true),
                    PaintItem(name = "Abaddon Black", hex = "#0a0a0a", paintType = "Base", checked = true, wantToBuy = false),
                    PaintItem(name = "Mournfang Brown", hex = "#5e2408", paintType = "Base", checked = false, wantToBuy = true)
                )
            ),
            PaintArmy(
                name = "Necrons",
                posX = 350f,
                posY = 420f,
                paints = listOf(
                    PaintItem(name = "Leadbelcher", hex = "#737373", paintType = "Layer", checked = false, wantToBuy = false),
                    PaintItem(name = "Nuln Oil", hex = "#181818", paintType = "Shade", checked = true, wantToBuy = false),
                    PaintItem(name = "Necron Compound", hex = "#a0a8a0", paintType = "Dry", checked = false, wantToBuy = false),
                    PaintItem(name = "Abaddon Black", hex = "#0a0a0a", paintType = "Base", checked = true, wantToBuy = false),
                    PaintItem(name = "Biel-Tan Green", hex = "#2d7230", paintType = "Shade", checked = false, wantToBuy = false)
                )
            ),
            PaintArmy(
                name = "Chaos Space Marines (World Eaters)",
                posX = 660f,
                posY = 40f,
                paints = listOf(
                    PaintItem(name = "Khorne Red", hex = "#6b0f0f", paintType = "Base", checked = false, wantToBuy = false),
                    PaintItem(name = "Retributor Armour", hex = "#c8a84a", paintType = "Base", checked = false, wantToBuy = false),
                    PaintItem(name = "Agrax Earthshade", hex = "#7a5c30", paintType = "Shade", checked = false, wantToBuy = false),
                    PaintItem(name = "Leadbelcher", hex = "#737373", paintType = "Layer", checked = false, wantToBuy = false),
                    PaintItem(name = "Nuln Oil", hex = "#181818", paintType = "Shade", checked = true, wantToBuy = false),
                    PaintItem(name = "Blood for the Blood God", hex = "#800010", paintType = "Technical", checked = false, wantToBuy = false)
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
