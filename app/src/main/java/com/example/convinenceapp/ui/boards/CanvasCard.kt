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
import androidx.compose.ui.draw.shadow
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
    val cardBg = remember(board.colorHex) {
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
        else -> Color.Black.copy(alpha = 0.12f)
    }

    val typeIcon = when (board.type) {
        BoardType.NOTE        -> "📝"
        BoardType.PAINT       -> "🎨"
        BoardType.CALCULATION -> "🧮"
    }

    var offsetX by remember(board.posX) { mutableFloatStateOf(board.posX) }
    var offsetY by remember(board.posY) { mutableFloatStateOf(board.posY) }

    Card(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(160.dp)
            .shadow(
                elevation = if (isConnecting || isConnectionTarget) 8.dp else 3.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isConnectionTarget || isConnecting) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .pointerInput(board.id, scale) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x / scale
                        offsetY += dragAmount.y / scale
                    },
                    onDragEnd = {
                        onDragEnd(offsetX, offsetY)
                    }
                )
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header: Emoji / Type & Pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${board.iconEmoji} $typeIcon",
                    fontSize = 18.sp
                )
                IconButton(
                    onClick = onPinToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (board.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (board.isPinned) "Unpin" else "Pin",
                        tint = if (board.isPinned) MaterialTheme.colorScheme.primary else Color.Gray,
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
                    color = Color(0xFF212121),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Preview Content
            if (aspectMode == AspectMode.NOTES || aspectMode == AspectMode.BOTH) {
                val preview = when (board.type) {
                    BoardType.NOTE -> board.noteContent.ifEmpty { "Empty note..." }
                    BoardType.PAINT -> "${board.paintData.armies.size} armies configured"
                    BoardType.CALCULATION -> board.calculationData?.let {
                        "${it.expression} = ${it.result}"
                    } ?: "No calc"
                }
                Text(
                    text = preview,
                    fontSize = 11.sp,
                    color = Color(0xFF424242),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }

            // Link / Connect handle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onConnectRequest,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        text = if (isConnecting) "Link 🎯" else "🔗 Link",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
