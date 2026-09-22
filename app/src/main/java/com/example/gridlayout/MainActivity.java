package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.util.Random;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private static final int GRID_SIZE = 10;
    private static final int MINE_COUNT = 6;
    private static final String TAG = "Minesweeper";
    private boolean gameOver = false;
    private boolean gameWon = false;

    private Cell[][] board = new Cell[GRID_SIZE][GRID_SIZE];
    private TextView[][] cellViews = new TextView[GRID_SIZE][GRID_SIZE];

    private TextView mineCountTv;
    private TextView timerTv;
    private TextView modeIconTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mineCountTv = findViewById(R.id.mineCountTv);
        timerTv = findViewById(R.id.timerTv);
        modeIconTv = findViewById(R.id.modeIconTv);

        initializeBoard();
        logBoard();
        buildGridUI();
    }

    private void initializeBoard() {
        for (int r = 0; r < GRID_SIZE; r++)
            for (int c = 0; c < GRID_SIZE; c++)
                board[r][c] = new Cell();

        Random rand = new Random();
        int placed = 0;
        while (placed < MINE_COUNT) {
            int r = rand.nextInt(GRID_SIZE);
            int c = rand.nextInt(GRID_SIZE);
            if (!board[r][c].isMine) {
                board[r][c].isMine = true;
                placed++;
            }
        }

        for (int r = 0; r < GRID_SIZE; r++)
            for (int c = 0; c < GRID_SIZE; c++)
                if (!board[r][c].isMine)
                    board[r][c].adjacentMines = countAdjacentMines(r, c);
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = row + dr, nc = col + dc;
                if (nr >= 0 && nr < GRID_SIZE && nc >= 0 && nc < GRID_SIZE && board[nr][nc].isMine)
                    count++;
            }
        }
        return count;
    }

    private void logBoard() {
        for (int r = 0; r < GRID_SIZE; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < GRID_SIZE; c++) {
                row.append(board[r][c].isMine ? "M" : board[r][c].adjacentMines).append(" ");
            }
            Log.d(TAG, row.toString());
        }
    }

    private boolean isDigMode = true;
    private int minesRemaining = MINE_COUNT;

    private boolean timerStarted = false;
    private int secondsElapsed = 0;

    private final Handler timerHandler = new Handler();
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            secondsElapsed++;
            timerTv.setText("⏱ " + secondsElapsed);
            timerHandler.postDelayed(this, 1000);
        }
    };

    private void buildGridUI() {
        GridLayout grid = findViewById(R.id.gridLayout01);
        LayoutInflater li = LayoutInflater.from(this);

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                TextView tv = (TextView) li.inflate(R.layout.custom_cell_layout, grid, false);
                tv.setBackgroundColor(Color.GRAY);

                final int row = r, col = c;
                tv.setOnClickListener(v -> onCellClick(row, col));

                GridLayout.LayoutParams lp = (GridLayout.LayoutParams) tv.getLayoutParams();
                lp.rowSpec = GridLayout.spec(r);
                lp.columnSpec = GridLayout.spec(c);

                grid.addView(tv, lp);
                cellViews[r][c] = tv;
            }
        }

        mineCountTv.setText("🚩 " + minesRemaining);
        modeIconTv.setOnClickListener(v -> toggleMode());
    }

    private void revealCell(int row, int col) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) return;
        Cell cell = board[row][col];
        if (cell.isRevealed || cell.isFlagged) return;

        cell.isRevealed = true;
        updateCellUI(row, col);
        Log.d(TAG, "Revealed (" + row + "," + col + ") - mine=" + cell.isMine + " adjacent=" + cell.adjacentMines);

        if (cell.isMine) {
            endGame(false);
            return;
        }

        if (cell.adjacentMines == 0) {
            for (int dr = -1; dr <= 1; dr++)
                for (int dc = -1; dc <= 1; dc++)
                    if (dr != 0 || dc != 0) revealCell(row + dr, col + dc);
        }

        checkWinCondition();
    }

    private void toggleMode() {
        isDigMode = !isDigMode;
        modeIconTv.setText(isDigMode ? "⛏️" : "🚩");
        Log.d(TAG, "Mode switched to: " + (isDigMode ? "DIG" : "FLAG"));
    }

    private void onCellClick(int row, int col) {

        if (gameOver) {
            Log.d(TAG, "Game already over (" + (gameWon ? "WON" : "LOST") + ") — would navigate to result page here.");
            return;
        }

        if (!timerStarted) {
            timerStarted = true;
            timerHandler.postDelayed(timerRunnable, 1000);
        }

        Cell cell = board[row][col];

        if (isDigMode) {
            if (cell.isFlagged) {
                Log.d(TAG, "Blocked: cannot dig a flagged cell at (" + row + "," + col + ")");
                return;
            }
            revealCell(row, col);
        } else {
            if (cell.isRevealed) return;
            cell.isFlagged = !cell.isFlagged;
            minesRemaining += cell.isFlagged ? -1 : 1;
            mineCountTv.setText("🚩 " + minesRemaining);
            updateCellUI(row, col);
            Log.d(TAG, "Flag toggled at (" + row + "," + col + ") - remaining=" + minesRemaining);
        }
    }

    private void checkWinCondition() {
        for (int r = 0; r < GRID_SIZE; r++)
            for (int c = 0; c < GRID_SIZE; c++)
                if (!board[r][c].isMine && !board[r][c].isRevealed) return;
        endGame(true);
    }

    private void endGame(boolean won) {
        if (gameOver) return;
        timerHandler.removeCallbacks(timerRunnable);
        gameOver = true;
        gameWon = won;
        Log.d(TAG, "GAME OVER — result: " + (won ? "WON" : "LOST"));
        revealAllCells();
    }

    private void revealAllCells() {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                board[r][c].isRevealed = true;
                updateCellUI(r, c);
            }
        }
    }

    private void updateCellUI(int row, int col) {
        Cell cell = board[row][col];
        TextView tv = cellViews[row][col];

        if (cell.isFlagged && !cell.isRevealed) {
            tv.setText("🚩");
            tv.setBackgroundColor(Color.LTGRAY);
            return;
        }
        if (!cell.isRevealed) {
            tv.setText("");
            tv.setBackgroundColor(Color.GRAY);
            return;
        }
        if (cell.isMine) {
            tv.setText("💣");
            tv.setBackgroundColor(Color.parseColor("#FF6666"));
        } else if (cell.adjacentMines > 0) {
            tv.setText(String.valueOf(cell.adjacentMines));
            tv.setBackgroundColor(Color.parseColor("#DDDDDD"));
        } else {
            tv.setText("");
            tv.setBackgroundColor(Color.parseColor("#DDDDDD"));
        }
    }
}