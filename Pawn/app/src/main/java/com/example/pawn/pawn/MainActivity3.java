package com.example.pawn.pawn;

/**
 * Created by David on 11/12/2017. This is the activity for saving games.
 * It is currently blank but this is where you would put the data structure
 * for storing and accessing a saved game.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity3 extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        final Context context = this;
        Button acquire = (Button) findViewById(R.id.back);
        acquire.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
