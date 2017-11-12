package com.example.pawn.pawn;

/*
 * Created by David on 11/12/2017. This is the activity for the chess board.
 * It is currently blank, but this would be where to put the code for implementing
 * a chess board.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity2 extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        final Context context = this;
        Button acquire = (Button) findViewById(R.id.back);
        acquire.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity2.class);
                startActivity(intent);
            }
        });
    }
}

