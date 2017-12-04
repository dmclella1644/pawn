package jwtc.android.chess;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static jwtc.android.chess.start.getmCurrentPhotoPath;

/**
 * Created by David on 12/2/2017.
 */

public class CornerSelector extends Activity implements View.OnTouchListener {
    myCanvas mCustomDrawableView;
    private Bitmap bitmap;
    private Bitmap bitmap2;
    boolean done = false;
    private float xPosition;
    private float yPosition;
    private int corners = 0;
    float d = 100F;
    RectF[] ellipse = new RectF[4];
    int ellipsePosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCustomDrawableView = new myCanvas(this);
        setContentView(mCustomDrawableView);
        mCustomDrawableView.setOnTouchListener(this);
        mCustomDrawableView.setDrawingCacheEnabled(true);
        if(done == false) {
            bitmap = mCustomDrawableView.getDrawingCache();
        } else {
            bitmap2 = mCustomDrawableView.getDrawingCache();
        }
    }

    private static int[][] cornerPosition = new int[4][2];

    public static int[][] getCornerPosition(){
        return cornerPosition;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motion){
        xPosition = motion.getX();
        yPosition = motion.getY();
        bitmap = view.getDrawingCache();
        view.invalidate();
        return true;
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.done_popupmenu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.done:
                        done = true;
                        //bitmap2 = mCustomDrawableView.getDrawingCache();
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

    private class myCanvas extends View{
        public myCanvas(Context context){
            super(context);
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            Bitmap bmp = BitmapFactory.decodeFile(getmCurrentPhotoPath());
            canvas.drawBitmap(bmp, null, new RectF(0, 0, getWidth(), getHeight()), null);
            Paint paint = new Paint();
            /*if(done == true) {
                RectF rectangle;
                float[][] lineCorners = new float[2][2];
                for(int i = 0; i < 2; i++){
                    for(int j = 0; j < 2; j++){
                        lineCorners[i][j] = cornerPosition[i][j];
                    }
                }
                if (bitmap2 != null) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.BLUE);
                    double min = 100.;
                    int pos = -1;
                    for (int i = 0; i < 2; i++) {
                        double dis = Math.sqrt(Math.pow((lineCorners[i][0] - xPosition), 2.) + Math.pow((lineCorners[i][1] - yPosition), 2.));
                        if (dis < min) {
                            min = dis;
                            pos = i;
                        }
                        if(pos != -1) {
                            lineCorners[pos][0] = xPosition;
                            lineCorners[pos][1] = yPosition;
                        }
                        rectangle = new RectF(lineCorners[0][0],lineCorners[0][1],lineCorners[1][0],lineCorners[1][1]);
                        canvas.drawRect(rectangle, paint);
                    }
                }
            } else {*/
                if (bitmap != null) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.RED);
                    if (corners > 0) {
                        double min = 100.;
                        int pos = -1;
                        for (int i = 0; i <= corners; i++) {
                            double dis = Math.sqrt(Math.pow((cornerPosition[i][0] - xPosition), 2.) + Math.pow((cornerPosition[i][1] - yPosition), 2.));
                            Log.d("distance", "" + dis);
                            if (dis < min) {
                                min = dis;
                                pos = i;
                            }
                            Log.d("corner", "" + i);
                            Log.d("min", "" + min);
                            Log.d("pos", "" + pos);
                        }
                        ellipsePosition = pos;
                        if (min < 100.) {
                            cornerPosition[ellipsePosition][0] = (int) xPosition;
                            cornerPosition[ellipsePosition][1] = (int) yPosition;
                            Log.d("ellipse_moved", "" + ellipsePosition);
                            ellipse[ellipsePosition] = new RectF(xPosition - d / 2, yPosition - d / 2, xPosition + d / 2, yPosition + d / 2);
                            for (int i = 0; i <= corners; i++) {
                                if (ellipse[i] != null) {
                                    canvas.drawOval(ellipse[i], paint);
                                }
                            }
                            return;
                        }
                        if (corners == 3) {
                            //ask if done. exit

                        }
                    }
                    if (ellipsePosition != -1) {
                        Log.d("ellipseBreak", "here" + ellipsePosition);
                        ellipse[ellipsePosition] = new RectF(xPosition - d / 2, yPosition - d / 2, xPosition + d / 2, yPosition + d / 2);
                        cornerPosition[ellipsePosition][0] = (int) xPosition;
                        cornerPosition[ellipsePosition][1] = (int) yPosition;
                        Log.d("cornerPosition", "" + cornerPosition[ellipsePosition][0]);
                        Log.d("cornerPosition", "" + cornerPosition[ellipsePosition][1]);
                    } else if (corners > 2 && ellipse[3] != null && ellipsePosition != 3) {
                        Log.d("Popup_menu", "Up!");
                        showPopup(mCustomDrawableView);
                    /*done = new PopupMenu(CornerSelector.this, mCustomDrawableView);
                    done.getMenuInflater().inflate(R.menu.done_popupmenu, done.getMenu());

                    done.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Log.d("Popup_menu","Up!");
                            Toast.makeText(CornerSelector.this,"" + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });
                    done.show();*/
                    } else {
                        ellipse[corners] = new RectF(xPosition - d / 2, yPosition - d / 2, xPosition + d / 2, yPosition + d / 2);
                        cornerPosition[corners][0] = (int) xPosition;
                        cornerPosition[corners][1] = (int) yPosition;
                        Log.d("cornerPosition", "" + cornerPosition[corners][0]);
                        Log.d("cornerPosition", "" + cornerPosition[corners][1]);
                    }
                    Log.d("ellipseBreak", "here" + corners);

                    for (int i = 0; i <= corners; i++) {
                        if (ellipse[i] != null) {
                            canvas.drawOval(ellipse[i], paint);
                        }
                    }
                    Log.d("corners", "" + corners);
                    switch (corners) {
                        case 0:
                            if (ellipse[0] != null) {
                                corners = 1;
                            }
                            Log.d("case", "" + 0);
                            Log.d("corners", "" + corners);
                            for (int i = 0; i < 4; i++) {
                                Log.d("ellipse", "" + ellipse[i]);
                            }
                            return;
                        case 1:
                            if (ellipse[1] != null) {
                                corners = 2;
                            }
                            Log.d("case", "" + 1);
                            Log.d("corners", "" + corners);
                            for (int i = 0; i < 4; i++) {
                                Log.d("ellipse", "" + ellipse[i]);
                            }
                            return;
                        case 2:
                            if (ellipse[2] != null) {
                                corners = 3;
                            }
                            Log.d("case", "" + 2);
                            Log.d("corners", "" + corners);
                            for (int i = 0; i < 4; i++) {
                                Log.d("ellipse", "" + ellipse[i]);
                            }
                            return;
                        case 3:
                            if (ellipse[3] != null) {
                                corners = 3;
                            }
                            Log.d("case", "" + 3);
                            Log.d("corners", "" + corners);
                            for (int i = 0; i < 4; i++) {
                                Log.d("ellipse", "" + ellipse[i]);
                            }
                            return;
                        default:
                            corners = 3;
                            Log.d("corners", "" + corners);
                            return;
                    }
                }
            //}
        }
    }
}
