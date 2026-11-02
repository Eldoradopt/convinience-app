package com.example.convinenceapp.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Persists boards as individual JSON files under [Context.filesDir]/boards/.
 * Connections are stored in a single connections.json file.
 */
class BoardStorage(private val context: Context) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private val boardsDir: File
        get() = File(context.filesDir, "boards").also { it.mkdirs() }

    private val connectionsFile: File
        get() = File(context.filesDir, "boards/connections.json")

    // ── Boards ────────────────────────────────────────────────────────────────

    fun createBoard(board: Board): Board {
        val file = File(boardsDir, "${board.id}.json")
        file.writeText(json.encodeToString(board))
        return board
    }

    fun loadBoard(boardId: String): Board? {
        val file = File(boardsDir, "$boardId.json")
        if (!file.exists()) return null
        return runCatching { json.decodeFromString<Board>(file.readText()) }.getOrNull()
    }

    fun listBoards(): List<Board> {
        val boards = boardsDir
            .listFiles { f -> f.extension == "json" && f.nameWithoutExtension != "connections" }
            ?.mapNotNull { f ->
                runCatching { json.decodeFromString<Board>(f.readText()) }.getOrNull()
            }
            ?.toMutableList() ?: mutableListOf()

        // Automatically populate default Warhammer paint board if missing
        if (boards.none { it.id == "default_warhammer_paint_board" || it.type == BoardType.PAINT }) {
            val defaultBoard = DefaultWarhammerData.createDefaultPaintBoard()
            createBoard(defaultBoard)
            boards.add(0, defaultBoard)
        }

        return boards.sortedByDescending { it.updatedAt }
    }

    fun updateBoard(board: Board): Board {
        val updated = board.copy(updatedAt = System.currentTimeMillis())
        val file = File(boardsDir, "${board.id}.json")
        file.writeText(json.encodeToString(updated))
        return updated
    }

    fun deleteBoard(boardId: String) {
        File(boardsDir, "$boardId.json").delete()
        // Remove any connections involving this board
        val conns = loadConnections().filter { it.fromId != boardId && it.toId != boardId }
        saveConnections(conns)
    }

    // ── Connections ───────────────────────────────────────────────────────────

    fun loadConnections(): List<BoardConnection> {
        if (!connectionsFile.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<BoardConnection>>(connectionsFile.readText())
        }.getOrDefault(emptyList())
    }

    fun saveConnection(fromId: String, toId: String) {
        val conns = loadConnections().toMutableList()
        val exists = conns.any {
            (it.fromId == fromId && it.toId == toId) ||
                (it.fromId == toId && it.toId == fromId)
        }
        if (!exists) {
            conns.add(BoardConnection(fromId, toId))
            saveConnections(conns)
        }
    }

    fun removeConnection(fromId: String, toId: String) {
        val conns = loadConnections().filter {
            !((it.fromId == fromId && it.toId == toId) ||
                (it.fromId == toId && it.toId == fromId))
        }
        saveConnections(conns)
    }

    fun connectionsForBoard(boardId: String): List<BoardConnection> =
        loadConnections().filter { it.fromId == boardId || it.toId == boardId }

    private fun saveConnections(conns: List<BoardConnection>) {
        connectionsFile.writeText(json.encodeToString(conns))
    }

    companion object {
        fun newId(): String = java.util.UUID.randomUUID().toString()
    }
}
