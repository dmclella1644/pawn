package com.example.pawn.pawn;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

//import static com.example.pawn.pawn.R.raw.color_counter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button acquire = (Button) findViewById(R.id.picture);
        acquire.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                closed = true;
                dispatchTakePictureIntent();
            }
        });
    }
    String mCurrentPhotoPath;
    Uri UriForPhoto;
    boolean closed = false;

    public void startCropImageActivity(Uri imageURI) {
        CropImage.activity(imageURI).start(this);
        //after image has been edited to have the chessboard within it, we call python code.
        /*try {
            Process p = Runtime.getRuntime().exec("python color_counter.py");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //after chessboard and pieces are set up, we delete the photo
        File fdelete = new File(mCurrentPhotoPath);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.d("file_delete","file Deleted :" + mCurrentPhotoPath);
            } else {
                Log.d("file_delete","file not Deleted :" + mCurrentPhotoPath);
            }
        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public class fileObserver extends FileObserver {
        public String absolutePath;
        public fileObserver(String path) {
            super(path, FileObserver.ALL_EVENTS);
            absolutePath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            Log.d("string", mCurrentPhotoPath);
            switch(event) {
                case ACCESS:
                    Log.d("access", "YES!");
                    break;
                case CLOSE_WRITE:
                    Log.d("close_write", "YES!");
                    if(closed){
                        startCropImageActivity(UriForPhoto);
                        closed = false;
                    }
                    break;
                case CREATE:
                    Log.d("create", "YES!");
                    break;
                case DELETE:
                    Log.d("delete", "YES!");
                    break;
                case MODIFY:
                    Log.d("modify", "YES!");
                    break;
                case OPEN:
                    Log.d("open", "YES!");
                    //startCropImageActivity(UriForPhoto);
                    break;
                default:
                    Log.d("onEvent", "YES! " + event);
                    break;
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;
    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try{
                photoFile = createImageFile();
            } catch (IOException ex){
            }
            if(photoFile != null){
                UriForPhoto = FileProvider.getUriForFile(this,"com.example.android.fileprovider",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, UriForPhoto);
                fileObserver test = new fileObserver(mCurrentPhotoPath);
                test.startWatching();
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
}
