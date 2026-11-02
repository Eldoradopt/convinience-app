package com.example.convinenceapp.ui.boards

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.convinenceapp.data.Board
import com.example.convinenceapp.data.BoardConnection
import com.example.convinenceapp.data.BoardStorage
import com.example.convinenceapp.data.BoardType
import com.example.convinenceapp.data.CalculationData
import com.example.convinenceapp.data.DefaultWarhammerData
import com.example.convinenceapp.data.PaintBoardData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BoardsViewModel(private val storage: BoardStorage) : ViewModel() {

    private val _boards = MutableStateFlow<List<Board>>(emptyList())
    val boards: StateFlow<List<Board>> = _boards

    private val _connections = MutableStateFlow<List<BoardConnection>>(emptyList())
    val connections: StateFlow<List<BoardConnection>> = _connections

    private val _selectedBoard = MutableStateFlow<Board?>(null)
    val selectedBoard: StateFlow<Board?> = _selectedBoard

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _boards.value = storage.listBoards()
                _connections.value = storage.loadConnections()
            }
        }
    }

    fun createBoard(
        title: String,
        type: BoardType,
        emoji: String = "📋",
        posX: Float = 100f,
        posY: Float = 100f,
        colorHex: String = "#FFF9C4"
    ): Board {
        val board = Board(
            id = BoardStorage.newId(),
            title = title,
            type = type,
            iconEmoji = emoji,
            posX = posX,
            posY = posY,
            colorHex = colorHex,
            paintData = if (type == BoardType.PAINT) PaintBoardData(armies = DefaultWarhammerData.getDefaultArmies()) else PaintBoardData()
        )
        viewModelScope.launch(Dispatchers.IO) {
            storage.createBoard(board)
            _boards.value = storage.listBoards()
        }
        return board
    }

    fun updatePosition(boardId: String, newX: Float, newY: Float) {
        val currentList = _boards.value
        val index = currentList.indexOfFirst { it.id == boardId }
        if (index != -1) {
            val updated = currentList[index].copy(posX = newX, posY = newY)
            val newList = currentList.toMutableList()
            newList[index] = updated
            _boards.value = newList
            viewModelScope.launch(Dispatchers.IO) {
                storage.updateBoard(updated)
            }
        }
    }

    fun selectBoard(boardId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedBoard.value = storage.loadBoard(boardId)
        }
    }

    fun updateBoard(board: Board) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = storage.updateBoard(board)
            _selectedBoard.value = updated
            _boards.value = storage.listBoards()
        }
    }

    fun deleteBoard(boardId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            storage.deleteBoard(boardId)
            _boards.value = storage.listBoards()
            _connections.value = storage.loadConnections()
            if (_selectedBoard.value?.id == boardId) _selectedBoard.value = null
        }
    }

    fun togglePin(board: Board) {
        updateBoard(board.copy(isPinned = !board.isPinned))
    }

    fun addConnection(fromId: String, toId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            storage.saveConnection(fromId, toId)
            _connections.value = storage.loadConnections()
        }
    }

    fun removeConnection(fromId: String, toId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            storage.removeConnection(fromId, toId)
            _connections.value = storage.loadConnections()
        }
    }

    fun saveCalculationToBoard(expression: String, result: String, boardId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetId = boardId ?: BoardStorage.newId()
            val existing = if (boardId != null) storage.loadBoard(boardId) else null
            val board = existing?.copy(
                calculationData = CalculationData(expression, result),
                updatedAt = System.currentTimeMillis()
            ) ?: Board(
                id = targetId,
                title = "Calc: $expression",
                type = BoardType.CALCULATION,
                iconEmoji = "🧮",
                calculationData = CalculationData(expression, result)
            )
            if (existing != null) storage.updateBoard(board) else storage.createBoard(board)
            _boards.value = storage.listBoards()
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BoardsViewModel(BoardStorage(context.applicationContext)) as T
    }
}
