package jwtc.android.chess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by David on 12/3/2017.
 */

public class textbox extends Activity{
    private static String boardString;
    public static String getBoardStringGlobal(){ return boardString; }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.textbox);
        Button acquire = (Button) findViewById(R.id.button);
        EditText input = (EditText) findViewById(R.id.editText2);
        boardString = input.getText().toString();
        acquire.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
}
