package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int GRID_SIZE = 10;
    private static final int MINE_COUNT = 6;
    private static final String TAG = "Minesweeper";

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

    // TEMPORARY: reveals everything so I can check grid lines up with the
    // Logcat output. Will replace w real dig/flag logic
    private void buildGridUI() {
        GridLayout grid = findViewById(R.id.gridLayout01);
        LayoutInflater li = LayoutInflater.from(this);

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                TextView tv = (TextView) li.inflate(R.layout.custom_cell_layout, grid, false);

                Cell cell = board[r][c];
                if (cell.isMine) {
                    tv.setText("M");
                    tv.setBackgroundColor(Color.parseColor("#FF6666"));
                } else {
                    tv.setText(String.valueOf(cell.adjacentMines));
                    tv.setBackgroundColor(Color.LTGRAY);
                }

                GridLayout.LayoutParams lp = (GridLayout.LayoutParams) tv.getLayoutParams();
                lp.rowSpec = GridLayout.spec(r);
                lp.columnSpec = GridLayout.spec(c);

                grid.addView(tv, lp);
                cellViews[r][c] = tv;
            }
        }
    }
}