package com.example.convinenceapp.ui.boards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.convinenceapp.data.AspectMode
import com.example.convinenceapp.data.Board
import com.example.convinenceapp.data.BoardType

@Composable
fun BoardIconCard(
    board: Board,
    aspectMode: AspectMode,
    isConnecting: Boolean,
    isConnectionTarget: Boolean,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onConnectRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isConnectionTarget -> MaterialTheme.colorScheme.tertiary
        isConnecting -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val typeIcon = when (board.type) {
        BoardType.NOTE        -> "📝"
        BoardType.PAINT       -> "🎨"
        BoardType.CALCULATION -> "🧮"
    }

    Card(
        modifier = modifier
            .size(120.dp)
            .border(
                width = if (isConnectionTarget || isConnecting) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(text = "${board.iconEmoji} $typeIcon", fontSize = 20.sp)
                IconButton(
                    onClick = onPinToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (board.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (board.isPinned) "Unpin" else "Pin",
                        tint = if (board.isPinned) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Title
            if (aspectMode == AspectMode.TITLE || aspectMode == AspectMode.BOTH) {
                Text(
                    text = board.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Notes preview
            if (aspectMode == AspectMode.NOTES || aspectMode == AspectMode.BOTH) {
                val preview = when (board.type) {
                    BoardType.NOTE        -> board.noteContent.take(40)
                    BoardType.PAINT       -> "${board.paintData.armies.size} armies"
                    BoardType.CALCULATION -> board.calculationData?.let {
                        "${it.expression} = ${it.result}"
                    } ?: ""
                }
                Text(
                    text = preview,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp
                )
            }

            // Connect button
            TextButton(
                onClick = onConnectRequest,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(20.dp)
            ) {
                Text(
                    text = if (isConnecting) "Link here" else "🔗",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
