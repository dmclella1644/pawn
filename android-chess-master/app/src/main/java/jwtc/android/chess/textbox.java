package jwtc.android.chess;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    static final int PASTE_ID=2213;
    private MenuItem paste_m;
    EditText input2;
    CharSequence pasteData = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.textbox);
        Button acquire = (Button) findViewById(R.id.button);
        final EditText input = (EditText) findViewById(R.id.editText2);
        registerForContextMenu(findViewById(R.id.editText2));
        acquire.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boardString = input.getText().toString();
                finish();
            }
        });
        Button back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu,v,menuInfo);
        menu.add(0, PASTE_ID,0, "Paste");
        paste_m=menu.getItem(0);
        input2 = (EditText) findViewById(R.id.editText2);
        String inputString = input2.getText().toString();
        if(inputString.matches("")){
            paste_m.setEnabled(true);
        } else {
            paste_m.setEnabled(false);
        }
    }
    public String edits;
    public boolean onContextItemSelected(MenuItem item){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if(item.getItemId() == PASTE_ID){
            ClipData.Item clipItem = clipboard.getPrimaryClip().getItemAt(0);
            Log.d("clipboard_item",""+clipItem.getText());
            pasteData = clipItem.getText();
            String pasteStringData = pasteData.toString();
            Log.d("pasteData",""+pasteData.toString());
            if (pasteStringData.matches("")) {
                return false;
            } else {
                input2.setText(pasteStringData);
                Log.d("pasteData",""+pasteStringData);
                return true;
                /*Uri pasteUri = clipItem.getUri();

                // If the URI contains something, try to get text from it
                if (pasteUri != null) {

                    // calls a routine to resolve the URI and get data from it. This routine is not
                    // presented here.
                    pasteData = pasteUri.toString();

                } else {

                    // Something is wrong. The MIME type was plain text, but the clipboard does not contain either
                    // text or a Uri. Report an error.
                    Log.e("textbox_error", "Clipboard contains an invalid data type");
                    pasteData = null;
                    return false;
                }*/
            }
        } else {
            return false;
        }
    }
}
