package com.example.convinenceapp.ui.boards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.convinenceapp.data.AspectMode
import com.example.convinenceapp.data.Board
import com.example.convinenceapp.data.BoardType
import kotlin.math.roundToInt

@Composable
fun CanvasCard(
    board: Board,
    aspectMode: AspectMode,
    isConnecting: Boolean,
    isConnectionTarget: Boolean,
    scale: Float,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onConnectRequest: () -> Unit,
    onDragEnd: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBaseBg = remember(board.colorHex) {
        runCatching {
            val hex = board.colorHex.trimStart('#')
            val longVal = hex.toLong(16)
            if (hex.length == 6) Color(0xFF000000 or longVal) else Color(longVal)
        }.getOrElse { Color(0xFFFFF9C4) }
    }

    val borderColor = when {
        isConnectionTarget -> MaterialTheme.colorScheme.primary
        isConnecting -> MaterialTheme.colorScheme.tertiary
        board.isPinned -> MaterialTheme.colorScheme.secondary
        else -> Color.Black.copy(alpha = 0.08f)
    }

    val typeBadge = when (board.type) {
        BoardType.NOTE        -> "Note"
        BoardType.PAINT       -> "Paint Board"
        BoardType.CALCULATION -> "Calculation"
    }

    val typeColor = when (board.type) {
        BoardType.NOTE        -> Color(0xFF5C6BC0)
        BoardType.PAINT       -> Color(0xFF2E7D32)
        BoardType.CALCULATION -> Color(0xFFE65100)
    }

    var currentX by remember(board.posX) { mutableFloatStateOf(board.posX) }
    var currentY by remember(board.posY) { mutableFloatStateOf(board.posY) }

    val currentScale by rememberUpdatedState(scale)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    Card(
        modifier = modifier
            .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
            .width(170.dp)
            .shadow(
                elevation = if (isConnecting || isConnectionTarget) 10.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .border(
                width = if (isConnectionTarget || isConnecting) 2.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(board.id) {
                detectDragGestures(
                    onDragStart = { },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentX += dragAmount.x / currentScale
                        currentY += dragAmount.y / currentScale
                    },
                    onDragEnd = {
                        currentOnDragEnd(currentX, currentY)
                    },
                    onDragCancel = {
                        currentOnDragEnd(currentX, currentY)
                    }
                )
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBaseBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Emoji + Type Pill & Pin Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = typeColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = board.iconEmoji, fontSize = 13.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = typeBadge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    }
                }

                IconButton(
                    onClick = onPinToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (board.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (board.isPinned) "Unpin" else "Pin",
                        tint = if (board.isPinned) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Title
            if (aspectMode == AspectMode.TITLE || aspectMode == AspectMode.BOTH) {
                Text(
                    text = board.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E2022),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            // Preview Content
            if (aspectMode == AspectMode.NOTES || aspectMode == AspectMode.BOTH) {
                val preview = when (board.type) {
                    BoardType.NOTE -> board.noteContent.ifEmpty { "Empty note..." }
                    BoardType.PAINT -> {
                        val total = board.paintData.armies.sumOf { it.paints.size }
                        val owned = board.paintData.armies.sumOf { it.paints.count { p -> p.checked } }
                        "${board.paintData.armies.size} Armies • $owned/$total owned"
                    }
                    BoardType.CALCULATION -> board.calculationData?.let {
                        "${it.expression} = ${it.result}"
                    } ?: "No calc"
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.04f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = preview,
                        fontSize = 11.sp,
                        color = Color(0xFF424242),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            // Link / Connect Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    onClick = onConnectRequest,
                    shape = RoundedCornerShape(6.dp),
                    color = if (isConnecting) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.06f)
                ) {
                    Text(
                        text = if (isConnecting) "Linking..." else "🔗 Link",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isConnecting) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
