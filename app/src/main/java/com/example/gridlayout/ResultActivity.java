package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        int seconds = getIntent().getIntExtra("seconds", 0);
        boolean won = getIntent().getBooleanExtra("won", false);

        TextView resultTv = findViewById(R.id.resultTv);
        resultTv.setText("Used " + seconds + " seconds.\n"
                + (won ? "You won." : "You lost.") + "\n"
                + (won ? "Good job!" : "Better luck next time!"));

        Button playAgainBtn = findViewById(R.id.playAgainBtn);
        playAgainBtn.setOnClickListener(v -> {
            startActivity(new Intent(ResultActivity.this, MainActivity.class));
            finish();
        });
    }
}