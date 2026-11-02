package com.example.convinenceapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.convinenceapp.MainActivity
import com.example.convinenceapp.R
import com.example.convinenceapp.data.Board
import com.example.convinenceapp.data.BoardStorage
import com.example.convinenceapp.data.BoardType

class BoardWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_SWITCH_BOARD) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val delta = intent.getIntExtra(EXTRA_DELTA, 1)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                switchBoard(context, appWidgetId, delta)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val ACTION_SWITCH_BOARD = "com.example.convinenceapp.ACTION_SWITCH_BOARD"
        const val EXTRA_DELTA = "extra_delta"
        private const val PREFS_NAME = "widget_prefs"
        private const val PREF_BOARD_INDEX = "board_index_"

        private fun getSavedIndex(context: Context, appWidgetId: Int): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getInt("$PREF_BOARD_INDEX$appWidgetId", 0)
        }

        private fun saveIndex(context: Context, appWidgetId: Int, index: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putInt("$PREF_BOARD_INDEX$appWidgetId", index).apply()
        }

        private fun switchBoard(context: Context, appWidgetId: Int, delta: Int) {
            val storage = BoardStorage(context)
            val boards = storage.listBoards()
            if (boards.isNotEmpty()) {
                val currentIndex = getSavedIndex(context, appWidgetId)
                val newIndex = (currentIndex + delta + boards.size) % boards.size
                saveIndex(context, appWidgetId, newIndex)
                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val storage = BoardStorage(context)
            val boards = storage.listBoards()

            val views = RemoteViews(context.packageName, R.layout.widget_board)

            if (boards.isNotEmpty()) {
                val index = getSavedIndex(context, appWidgetId) % boards.size
                val currentBoard = boards[index]

                // Board Header and Type Badge
                views.setTextViewText(R.id.widget_title, "${currentBoard.iconEmoji} ${currentBoard.title.take(20)}")
                views.setTextViewText(R.id.widget_board_counter, "${index + 1}/${boards.size}")

                when (currentBoard.type) {
                    BoardType.NOTE -> {
                        views.setTextViewText(R.id.widget_section_label, "📝 Note Content")
                        val content = currentBoard.noteContent.ifBlank { "Empty note. Tap to write..." }
                        views.setTextViewText(R.id.widget_main_text, content.take(180))
                    }
                    BoardType.PAINT -> {
                        val total = currentBoard.paintData.armies.sumOf { it.paints.size }
                        val owned = currentBoard.paintData.armies.sumOf { it.paints.count { p -> p.checked } }
                        views.setTextViewText(R.id.widget_section_label, "🎨 Progress: $owned/$total owned")

                        val buyPaints = currentBoard.paintData.armies.flatMap { it.paints }
                            .filter { !it.checked && it.wantToBuy }
                            .distinctBy { it.name.lowercase().trim() }

                        if (buyPaints.isNotEmpty()) {
                            val buyText = "🛒 Buy List:\n" + buyPaints.take(4).joinToString("\n") { p ->
                                "• ${p.name} (${p.paintType})"
                            } + if (buyPaints.size > 4) "\n+ ${buyPaints.size - 4} more..." else ""
                            views.setTextViewText(R.id.widget_main_text, buyText)
                        } else {
                            val needed = currentBoard.paintData.armies.flatMap { it.paints }
                                .filter { !it.checked }
                                .distinctBy { it.name.lowercase().trim() }
                            if (needed.isEmpty()) {
                                views.setTextViewText(R.id.widget_main_text, "✅ All paints acquired!")
                            } else {
                                val neededText = "Needed:\n" + needed.take(4).joinToString("\n") { p ->
                                    "• ${p.name} (${p.paintType})"
                                } + if (needed.size > 4) "\n+ ${needed.size - 4} more..." else ""
                                views.setTextViewText(R.id.widget_main_text, neededText)
                            }
                        }
                    }
                    BoardType.CALCULATION -> {
                        views.setTextViewText(R.id.widget_section_label, "🧮 Saved Calculation")
                        val calc = currentBoard.calculationData
                        if (calc != null) {
                            views.setTextViewText(R.id.widget_main_text, "${calc.expression}\n= ${calc.result}")
                        } else {
                            views.setTextViewText(R.id.widget_main_text, "No calculation saved.")
                        }
                    }
                }
            } else {
                views.setTextViewText(R.id.widget_title, "📋 Boards Canvas")
                views.setTextViewText(R.id.widget_board_counter, "0/0")
                views.setTextViewText(R.id.widget_section_label, "Welcome")
                views.setTextViewText(R.id.widget_main_text, "Tap to open the app and add cards to your canvas.")
            }

            // Click Prev Board
            val prevIntent = Intent(context, BoardWidgetProvider::class.java).apply {
                action = ACTION_SWITCH_BOARD
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(EXTRA_DELTA, -1)
            }
            val prevPending = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 1,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPending)

            // Click Next Board
            val nextIntent = Intent(context, BoardWidgetProvider::class.java).apply {
                action = ACTION_SWITCH_BOARD
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(EXTRA_DELTA, 1)
            }
            val nextPending = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 2,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_next, nextPending)

            // Click body to launch app
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val launchPending = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 3,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_content_container, launchPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun notifyUpdate(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, BoardWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}
