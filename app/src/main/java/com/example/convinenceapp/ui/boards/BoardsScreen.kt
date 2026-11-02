package com.example.convinenceapp.ui.boards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.convinenceapp.data.AspectMode
import com.example.convinenceapp.data.Board
import com.example.convinenceapp.data.BoardType
import com.example.convinenceapp.ui.theme.AppThemeMode
import com.example.convinenceapp.ui.theme.ThemeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardsScreen(
    viewModel: BoardsViewModel,
    onOpenBoard: (String) -> Unit
) {
    val boards by viewModel.boards.collectAsState()
    val connections by viewModel.connections.collectAsState()
    val currentTheme by ThemeState.theme.collectAsState()

    var aspectMode by remember { mutableStateOf(AspectMode.BOTH) }
    var showNewBoardDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var connectingFromId by remember { mutableStateOf<String?>(null) }
    var filterType by remember { mutableStateOf<BoardType?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    // Canvas panning and zooming
    var panOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var zoomScale by remember { mutableFloatStateOf(1f) }

    // Real-time board visual centers for smooth connector lines
    val cardCenters = remember { mutableStateMapOf<String, Offset>() }

    val filteredBoards = remember(boards, filterType) {
        val list = if (filterType == null) boards else boards.filter { it.type == filterType }
        list.sortedBy { it.isPinned }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Boards Canvas") },
                actions = {
                    // Quick Add Board Button in Top Bar
                    IconButton(onClick = { showNewBoardDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "New Board",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Theme Switcher Button
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(Icons.Default.Palette, contentDescription = "Themes")
                    }

                    // Reset View / Center
                    IconButton(onClick = {
                        panOffset = Offset(0f, 0f)
                        zoomScale = 1f
                    }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Canvas View")
                    }

                    // Aspect mode toggle
                    TextButton(onClick = {
                        aspectMode = when (aspectMode) {
                            AspectMode.TITLE -> AspectMode.NOTES
                            AspectMode.NOTES -> AspectMode.BOTH
                            AspectMode.BOTH  -> AspectMode.TITLE
                        }
                    }) {
                        Text(
                            when (aspectMode) {
                                AspectMode.TITLE -> "Title"
                                AspectMode.NOTES -> "Notes"
                                AspectMode.BOTH  -> "Both"
                            }
                        )
                    }

                    // Filter
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All") },
                            onClick = { filterType = null; showFilterMenu = false }
                        )
                        BoardType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = { filterType = type; showFilterMenu = false }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewBoardDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Board")
            }
        }
    ) { padding ->
        val canvasBgColor = MaterialTheme.colorScheme.background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(canvasBgColor)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.4f, 2.5f)
                        panOffset += pan
                    }
                }
        ) {
            // Infinite canvas layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = panOffset.x
                        translationY = panOffset.y
                        scaleX = zoomScale
                        scaleY = zoomScale
                    }
            ) {
                val gridColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                val dotColor = MaterialTheme.colorScheme.primary

                // Canvas dot-grid background and connection lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dotSpacing = 36f
                    val width = size.width
                    val height = size.height

                    var x = -2000f
                    while (x < width + 2000f) {
                        var y = -2000f
                        while (y < height + 2000f) {
                            drawCircle(color = gridColor, radius = 2f, center = Offset(x, y))
                            y += dotSpacing
                        }
                        x += dotSpacing
                    }

                    // Connection curves (cubic bezier paths)
                    for (conn in connections) {
                        val from = cardCenters[conn.fromId]
                        val to = cardCenters[conn.toId]
                        if (from != null && to != null) {
                            val path = Path().apply {
                                moveTo(from.x, from.y)
                                val controlX = (from.x + to.x) / 2
                                cubicTo(controlX, from.y, controlX, to.y, to.x, to.y)
                            }
                            drawPath(
                                path = path,
                                color = lineColor,
                                style = Stroke(width = 4f)
                            )
                            drawCircle(dotColor, radius = 6f, center = from)
                            drawCircle(dotColor, radius = 6f, center = to)
                        }
                    }
                }

                // Cards on the canvas
                filteredBoards.forEach { board ->
                    CanvasCard(
                        board = board,
                        aspectMode = aspectMode,
                        isConnecting = connectingFromId != null && connectingFromId != board.id,
                        isConnectionTarget = connectingFromId == board.id,
                        scale = zoomScale,
                        onClick = {
                            if (connectingFromId != null && connectingFromId != board.id) {
                                viewModel.addConnection(connectingFromId!!, board.id)
                                connectingFromId = null
                            } else {
                                onOpenBoard(board.id)
                            }
                        },
                        onPinToggle = { viewModel.togglePin(board) },
                        onConnectRequest = {
                            connectingFromId = if (connectingFromId == board.id) null else board.id
                        },
                        onDragEnd = { newX, newY ->
                            viewModel.updatePosition(board.id, newX, newY)
                        },
                        modifier = Modifier.onGloballyPositioned { coords ->
                            val pos = coords.positionInParent()
                            val size = coords.size
                            cardCenters[board.id] = Offset(
                                pos.x + size.width / 2f,
                                pos.y + size.height / 2f
                            )
                        }
                    )
                }
            }

            // Connection linking banner
            if (connectingFromId != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Tap another card to create a connection line 🔗",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = { connectingFromId = null }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false },
            onSelectTheme = { selected ->
                ThemeState.setTheme(selected)
                showThemeDialog = false
            }
        )
    }

    if (showNewBoardDialog) {
        val nextOffset = remember(boards.size) { (boards.size * 25f) % 200f }
        val targetX = (-panOffset.x / zoomScale) + 60f + nextOffset
        val targetY = (-panOffset.y / zoomScale) + 80f + nextOffset

        NewBoardDialog(
            onDismiss = { showNewBoardDialog = false },
            onCreate = { title, type, emoji, colorHex ->
                viewModel.createBoard(
                    title = title,
                    type = type,
                    emoji = emoji,
                    posX = targetX,
                    posY = targetY,
                    colorHex = colorHex
                )
                showNewBoardDialog = false
            }
        )
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: AppThemeMode,
    onDismiss: () -> Unit,
    onSelectTheme: (AppThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose App Theme") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppThemeMode.entries.forEach { mode ->
                    Surface(
                        onClick = { onSelectTheme(mode) },
                        shape = MaterialTheme.shapes.medium,
                        color = if (currentTheme == mode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = mode.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (currentTheme == mode) {
                                Text("Active ✓", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun NewBoardDialog(
    onDismiss: () -> Unit,
    onCreate: (String, BoardType, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(BoardType.NOTE) }
    var selectedColor by remember { mutableStateOf("#FFF9C4") }

    val typeEmojis = mapOf(
        BoardType.NOTE to "📝",
        BoardType.PAINT to "🎨",
        BoardType.CALCULATION to "🧮"
    )

    val colorPalette = listOf(
        "#FFF9C4", // Pastel Yellow
        "#E1F5FE", // Pastel Blue
        "#E8F5E9", // Pastel Green
        "#FCE4EC", // Pastel Pink
        "#EDE7F6", // Pastel Purple
        "#FFFFFF"  // Clean White
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Board Card") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("e.g. Shopping List or Project A") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Board Type", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BoardType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = {
                                Text("${typeEmojis[type]} ${type.name.lowercase().replaceFirstChar { it.uppercase() }}")
                            }
                        )
                    }
                }

                Text("Card Color", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorPalette.forEach { hex ->
                        val parsedColor = runCatching {
                            Color(0xFF000000 or hex.trimStart('#').toLong(16))
                        }.getOrDefault(Color.White)

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(parsedColor, MaterialTheme.shapes.small)
                                .clickable { selectedColor = hex }
                        ) {
                            if (selectedColor == hex) {
                                Text(
                                    "✓",
                                    modifier = Modifier.align(Alignment.Center),
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(
                            title.trim(),
                            selectedType,
                            typeEmojis[selectedType] ?: "📋",
                            selectedColor
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
