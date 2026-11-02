package com.example.convinenceapp.ui.boards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.convinenceapp.data.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardEditorScreen(
    viewModel: BoardsViewModel,
    boardId: String,
    onBack: () -> Unit
) {
    val board by viewModel.selectedBoard.collectAsState()

    LaunchedEffect(boardId) {
        viewModel.selectBoard(boardId)
    }

    val currentBoard = board ?: return

    var editingTitle by remember(currentBoard.id) { mutableStateOf(currentBoard.title) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = editingTitle,
                        onValueChange = { editingTitle = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleMedium,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (editingTitle.isNotBlank() && editingTitle != currentBoard.title) {
                            viewModel.updateBoard(currentBoard.copy(title = editingTitle.trim()))
                        }
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentBoard.type) {
                BoardType.NOTE        -> NoteEditor(currentBoard, viewModel)
                BoardType.PAINT       -> PaintCanvasBoardEditor(currentBoard, viewModel)
                BoardType.CALCULATION -> CalculationViewer(currentBoard)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Board") },
            text = { Text("Delete \"${currentBoard.title}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBoard(currentBoard.id)
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ─── Note Editor ─────────────────────────────────────────────────────────────

@Composable
private fun NoteEditor(board: Board, viewModel: BoardsViewModel) {
    var content by remember(board.id) { mutableStateOf(board.noteContent) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Write your notes here…") },
            label = { Text("Note") }
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.updateBoard(board.copy(noteContent = content)) },
            modifier = Modifier.align(Alignment.End)
        ) { Text("Save") }
    }
}

// ─── Paint Canvas Board Editor ───────────────────────────────────────────────

@Composable
private fun PaintCanvasBoardEditor(board: Board, viewModel: BoardsViewModel) {
    var paintData by remember(board.id) {
        val data = if (board.paintData.armies.isEmpty()) {
            PaintBoardData(armies = DefaultWarhammerData.getDefaultArmies())
        } else {
            board.paintData
        }
        mutableStateOf(data)
    }

    var showAddArmyDialog by remember { mutableStateOf(false) }
    var showAddPaintDialog by remember { mutableStateOf<Int?>(null) } // army index
    var syncAcrossArmies by remember { mutableStateOf(true) }
    var exportSuccessMessage by remember { mutableStateOf(false) }

    // Canvas pan & zoom state
    var panOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var zoomScale by remember { mutableFloatStateOf(1f) }

    // Synchronized Toggle Helper
    fun togglePaintItem(paintName: String, armyIndex: Int, paintIndex: Int, newChecked: Boolean) {
        val updatedArmies = if (syncAcrossArmies) {
            paintData.armies.map { army ->
                val updatedPaints = army.paints.map { p ->
                    if (p.name.equals(paintName, ignoreCase = true)) {
                        // If checked as owned, unmark from buy list
                        p.copy(checked = newChecked, wantToBuy = if (newChecked) false else p.wantToBuy)
                    } else p
                }
                army.copy(paints = updatedPaints)
            }
        } else {
            val armies = paintData.armies.toMutableList()
            val paints = armies[armyIndex].paints.toMutableList()
            val target = paints[paintIndex]
            paints[paintIndex] = target.copy(checked = newChecked, wantToBuy = if (newChecked) false else target.wantToBuy)
            armies[armyIndex] = armies[armyIndex].copy(paints = paints)
            armies
        }
        val updated = paintData.copy(armies = updatedArmies)
        paintData = updated
        viewModel.updateBoard(board.copy(paintData = updated))
    }

    // Toggle "Want to Buy" (Shopping List) state
    fun toggleWantToBuy(paintName: String, armyIndex: Int, paintIndex: Int, wantBuy: Boolean) {
        val updatedArmies = if (syncAcrossArmies) {
            paintData.armies.map { army ->
                val updatedPaints = army.paints.map { p ->
                    if (p.name.equals(paintName, ignoreCase = true)) {
                        p.copy(wantToBuy = wantBuy)
                    } else p
                }
                army.copy(paints = updatedPaints)
            }
        } else {
            val armies = paintData.armies.toMutableList()
            val paints = armies[armyIndex].paints.toMutableList()
            paints[paintIndex] = paints[paintIndex].copy(wantToBuy = wantBuy)
            armies[armyIndex] = armies[armyIndex].copy(paints = paints)
            armies
        }
        val updated = paintData.copy(armies = updatedArmies)
        paintData = updated
        viewModel.updateBoard(board.copy(paintData = updated))
    }

    // Export shopping list into a sticky note card on the main board canvas
    fun exportToShoppingNote() {
        val buyList = mutableMapOf<String, Pair<PaintItem, MutableList<String>>>()
        paintData.armies.forEach { army ->
            army.paints.forEach { p ->
                if (!p.checked && p.wantToBuy) {
                    val key = p.name.lowercase().trim()
                    if (!buyList.containsKey(key)) {
                        buyList[key] = p to mutableListOf(army.name)
                    } else {
                        buyList[key]?.second?.add(army.name)
                    }
                }
            }
        }

        val noteText = buildString {
            appendLine("🛒 PAINT SHOPPING LIST")
            appendLine("Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())}")
            appendLine("─────────────────────────")
            if (buyList.isEmpty()) {
                appendLine("No paints marked for buying yet.")
                appendLine("Tap the cart icon 🛒 on paints you want to buy!")
            } else {
                buyList.values.forEachIndexed { i, (paint, armies) ->
                    appendLine("${i + 1}. [ ] ${paint.name} (${paint.paintType})")
                    appendLine("   Hex: ${paint.hex}")
                    appendLine("   For: ${armies.joinToString(", ")}")
                }
            }
        }

        viewModel.createBoard(
            title = "Paint Shopping List",
            type = BoardType.NOTE,
            emoji = "🛒",
            posX = board.posX + 180f,
            posY = board.posY + 20f,
            colorHex = "#FFF9C4"
        ).also { created ->
            viewModel.updateBoard(created.copy(noteContent = noteText))
        }
        exportSuccessMessage = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Canvas Controls
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = syncAcrossArmies,
                        onCheckedChange = { syncAcrossArmies = it }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Sync Colors", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = { exportToShoppingNote() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Export Note", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { showAddArmyDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Army", fontSize = 11.sp)
                    }

                    IconButton(
                        onClick = {
                            panOffset = Offset(0f, 0f)
                            zoomScale = 1f
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Center Canvas")
                    }
                }
            }
        }

        // Infinite Army Canvas Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.4f, 2.5f)
                        panOffset += pan
                    }
                }
        ) {
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
                // Subtle dot grid
                val dotColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dotSpacing = 36f
                    val w = size.width
                    val h = size.height
                    var x = -2000f
                    while (x < w + 2000f) {
                        var y = -2000f
                        while (y < h + 2000f) {
                            drawCircle(color = dotColor, radius = 2f, center = Offset(x, y))
                            y += dotSpacing
                        }
                        x += dotSpacing
                    }
                }

                // Army Cards placed on Canvas with smooth relative drag tracking
                paintData.armies.forEachIndexed { armyIdx, army ->
                    val defaultPos = Offset(
                        x = 40f + (armyIdx % 2) * 310f,
                        y = 40f + (armyIdx / 2) * 440f
                    )
                    val cardPos = if (army.posX == 0f && army.posY == 0f) defaultPos else Offset(army.posX, army.posY)

                    ArmyCanvasCard(
                        army = army,
                        position = cardPos,
                        scale = zoomScale,
                        onPositionChanged = { newPos ->
                            val armies = paintData.armies.toMutableList()
                            armies[armyIdx] = armies[armyIdx].copy(posX = newPos.x, posY = newPos.y)
                            val updated = paintData.copy(armies = armies)
                            paintData = updated
                            viewModel.updateBoard(board.copy(paintData = updated))
                        },
                        onAddPaint = { showAddPaintDialog = armyIdx },
                        onDeleteArmy = {
                            val armies = paintData.armies.toMutableList().also { it.removeAt(armyIdx) }
                            val updated = paintData.copy(armies = armies)
                            paintData = updated
                            viewModel.updateBoard(board.copy(paintData = updated))
                        },
                        onTogglePaint = { paintName, pIdx, checked ->
                            togglePaintItem(paintName, armyIdx, pIdx, checked)
                        },
                        onToggleWantToBuy = { paintName, pIdx, wantBuy ->
                            toggleWantToBuy(paintName, armyIdx, pIdx, wantBuy)
                        },
                        onDeletePaint = { pIdx ->
                            val armies = paintData.armies.toMutableList()
                            val paints = armies[armyIdx].paints.toMutableList().also { it.removeAt(pIdx) }
                            armies[armyIdx] = armies[armyIdx].copy(paints = paints)
                            val updated = paintData.copy(armies = armies)
                            paintData = updated
                            viewModel.updateBoard(board.copy(paintData = updated))
                        }
                    )
                }
            }

            // Quick Floating Shopping Note Card on canvas
            FloatingShoppingSummaryCard(
                paintData = paintData,
                onExportNote = { exportToShoppingNote() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }

    if (exportSuccessMessage) {
        AlertDialog(
            onDismissRequest = { exportSuccessMessage = false },
            title = { Text("Shopping Note Created! 📝") },
            text = { Text("A new Shopping List note card has been created directly on your main boards canvas.") },
            confirmButton = {
                Button(onClick = { exportSuccessMessage = false }) { Text("Great") }
            }
        )
    }

    if (showAddArmyDialog) {
        AddArmyDialog(
            onDismiss = { showAddArmyDialog = false },
            onAdd = { name ->
                val nextPos = Offset(60f + (paintData.armies.size * 30f), 80f + (paintData.armies.size * 30f))
                val armies = paintData.armies + PaintArmy(name = name, posX = nextPos.x, posY = nextPos.y)
                val updated = paintData.copy(armies = armies)
                paintData = updated
                viewModel.updateBoard(board.copy(paintData = updated))
                showAddArmyDialog = false
            }
        )
    }

    showAddPaintDialog?.let { armyIdx ->
        AddPaintDialog(
            onDismiss = { showAddPaintDialog = null },
            onAdd = { paint ->
                val alreadyCheckedElsewhere = paintData.armies.any { a ->
                    a.paints.any { it.name.equals(paint.name, ignoreCase = true) && it.checked }
                }
                val paintToAdd = if (alreadyCheckedElsewhere) paint.copy(checked = true) else paint

                val armies = paintData.armies.toMutableList()
                armies[armyIdx] = armies[armyIdx].copy(paints = armies[armyIdx].paints + paintToAdd)
                val updated = paintData.copy(armies = armies)
                paintData = updated
                viewModel.updateBoard(board.copy(paintData = updated))
                showAddPaintDialog = null
            }
        )
    }
}

// ─── Free-Floating Draggable Army Card (Zero Snap / Glitch-Free) ──────────────

@Composable
private fun ArmyCanvasCard(
    army: PaintArmy,
    position: Offset,
    scale: Float,
    onPositionChanged: (Offset) -> Unit,
    onAddPaint: () -> Unit,
    onDeleteArmy: () -> Unit,
    onTogglePaint: (paintName: String, paintIndex: Int, checked: Boolean) -> Unit,
    onToggleWantToBuy: (paintName: String, paintIndex: Int, wantBuy: Boolean) -> Unit,
    onDeletePaint: (paintIndex: Int) -> Unit
) {
    // Current position state
    var currentPos by remember(position) { mutableStateOf(position) }
    val currentScale by rememberUpdatedState(scale)
    val currentOnPositionChanged by rememberUpdatedState(onPositionChanged)

    Card(
        modifier = Modifier
            .offset { IntOffset(currentPos.x.roundToInt(), currentPos.y.roundToInt()) }
            .width(300.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.2f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .pointerInput(army.name) {
                detectDragGestures(
                    onDragStart = { /* Started drag cleanly without displacement */ },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentPos += dragAmount / currentScale
                    },
                    onDragEnd = {
                        currentOnPositionChanged(currentPos)
                    },
                    onDragCancel = {
                        currentOnPositionChanged(currentPos)
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Army Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val total = army.paints.size
                val owned = army.paints.count { it.checked }
                val toBuy = army.paints.count { !it.checked && it.wantToBuy }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = army.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "$owned / $total owned",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (toBuy > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "🛒 $toBuy in buy list",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                Row {
                    IconButton(onClick = onAddPaint, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add Paint", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDeleteArmy, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Army", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Paints inside this Army
            if (army.paints.isEmpty()) {
                Text(
                    "No paints yet. Tap + to add colors.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    army.paints.forEachIndexed { pIdx, paint ->
                        PaintRow(
                            paint = paint,
                            onToggle = { checked -> onTogglePaint(paint.name, pIdx, checked) },
                            onToggleWantToBuy = { wantBuy -> onToggleWantToBuy(paint.name, pIdx, wantBuy) },
                            onDelete = { onDeletePaint(pIdx) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaintRow(
    paint: PaintItem,
    onToggle: (Boolean) -> Unit,
    onToggleWantToBuy: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val color = remember(paint.hex) {
        runCatching {
            val hex = paint.hex.trimStart('#')
            val longVal = hex.toLong(16)
            if (hex.length == 6) Color(0xFF000000 or longVal) else Color(longVal)
        }.getOrElse { Color.Gray }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox: Owned or not
        Checkbox(
            checked = paint.checked,
            onCheckedChange = onToggle,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(2.dp))

        // Visual color swatch
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
        Spacer(Modifier.width(8.dp))

        // Paint Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = paint.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (paint.checked) FontWeight.Normal else FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = "${paint.paintType} • ${paint.hex}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }

        // Shopping List (Want to buy) toggle button: only active if not owned
        if (!paint.checked) {
            IconButton(
                onClick = { onToggleWantToBuy(!paint.wantToBuy) },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Want to buy",
                    tint = if (paint.wantToBuy) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
        }
    }
}

// ─── Floating Shopping Summary Card on Canvas ────────────────────────────────

@Composable
private fun FloatingShoppingSummaryCard(
    paintData: PaintBoardData,
    onExportNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemsToBuy = remember(paintData) {
        paintData.armies.flatMap { it.paints }
            .filter { !it.checked && it.wantToBuy }
            .distinctBy { it.name.lowercase().trim() }
    }

    Card(
        modifier = modifier
            .width(220.dp)
            .shadow(6.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Buy List (${itemsToBuy.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                TextButton(
                    onClick = onExportNote,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("Export Note", fontSize = 10.sp)
                }
            }

            if (itemsToBuy.isEmpty()) {
                Text(
                    "Tap 🛒 on any unowned paint to mark for shopping.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    itemsToBuy.take(3).forEach { paint ->
                        val swatch = runCatching {
                            val hex = paint.hex.trimStart('#')
                            val longVal = hex.toLong(16)
                            if (hex.length == 6) Color(0xFF000000 or longVal) else Color(longVal)
                        }.getOrElse { Color.Gray }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(swatch)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(paint.name, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                    if (itemsToBuy.size > 3) {
                        Text("+ ${itemsToBuy.size - 3} more...", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddArmyDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Army / Force") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Army Name (e.g. Tyranids, Orks)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onAdd(name.trim()) }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddPaintDialog(onDismiss: () -> Unit, onAdd: (PaintItem) -> Unit) {
    var query by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var customHex by remember { mutableStateOf("#888888") }
    var customType by remember { mutableStateOf("Base") }
    var useDatabase by remember { mutableStateOf(true) }

    val suggestions = remember(query) {
        if (query.length >= 2) PaintDatabase.search(query).take(6) else emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Paint") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = useDatabase, onCheckedChange = { useDatabase = it })
                    Spacer(Modifier.width(8.dp))
                    Text(if (useDatabase) "From Citadel Range" else "Custom Hex")
                }

                if (useDatabase) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search paint name (e.g. Calgar)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    suggestions.forEach { entry ->
                        val c = entry.toColor()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAdd(PaintItem(entry.name, entry.hex, entry.paintType, checked = false, wantToBuy = true)) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(entry.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text("${entry.paintType} • ${entry.hex}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                } else {
                    OutlinedTextField(value = customName, onValueChange = { customName = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = customHex, onValueChange = { customHex = it }, label = { Text("Hex color (e.g. #FF0000)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = customType, onValueChange = { customType = it }, label = { Text("Type (Base/Layer/Shade…)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            if (!useDatabase) {
                Button(
                    onClick = { if (customName.isNotBlank()) onAdd(PaintItem(customName.trim(), customHex, customType, checked = false, wantToBuy = true)) },
                    enabled = customName.isNotBlank()
                ) { Text("Add") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ─── Calculation Viewer ───────────────────────────────────────────────────────

@Composable
private fun CalculationViewer(board: Board) {
    val data = board.calculationData
    val dateStr = data?.timestamp?.let {
        SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(it))
    } ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (data == null) {
            Text("No calculation saved.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Expression", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(data.expression, style = MaterialTheme.typography.bodyLarge)
                    HorizontalDivider()
                    Text("Result", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(data.result, style = MaterialTheme.typography.headlineMedium)
                    HorizontalDivider()
                    Text("Saved on $dateStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
