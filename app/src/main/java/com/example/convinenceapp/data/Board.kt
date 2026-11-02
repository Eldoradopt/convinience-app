package com.example.convinenceapp.data

import kotlinx.serialization.Serializable

enum class BoardType { NOTE, PAINT, CALCULATION }
enum class AspectMode { TITLE, NOTES, BOTH }

@Serializable
data class Board(
    val id: String,
    val title: String,
    val type: BoardType,
    val iconEmoji: String = "📋",
    val isPinned: Boolean = false,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val colorHex: String = "#FFFFFF",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // type-specific payload stored as JSON string
    val noteContent: String = "",
    val paintData: PaintBoardData = PaintBoardData(),
    val calculationData: CalculationData? = null,
)

@Serializable
data class PaintBoardData(
    val armies: List<PaintArmy> = emptyList()
)

@Serializable
data class PaintArmy(
    val name: String,
    val paints: List<PaintItem> = emptyList()
)

@Serializable
data class PaintItem(
    val name: String,
    val hex: String,
    val paintType: String,
    val checked: Boolean = false
)

@Serializable
data class CalculationData(
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class BoardConnection(
    val fromId: String,
    val toId: String
)
