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

    /*
     * This is what happens when the activity is created. Currently, it is a
     * blank slate that has a button on it. This can be changed during the
     * UI development.
     */
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

    /*
     * Global variables here. mCurrentPhotoPath is a String that contains the file
     * path to the photo. This allows the programmer to easily find out where the
     * photo is if they want to edit it or delete it. The boolean closed is used
     * so the crop tool is only called once. This can be changed for later. The
     * UriForPhoto is a Uri for the photo. This is needed for the Crop command.
     */
    String mCurrentPhotoPath;
    Uri UriForPhoto;
    boolean closed = false;

    public void startCropImageActivity(Uri imageURI) {
        /*
         * This is where the cropping, Python code, and deletion occur. Currently,
         * the execution of the Python code is failing. Nothing wrong with Python,
         * just Runtime.exec
         */
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

    /*
     * The occurs when the Crop command is finished. This changes the photo
     * into the cropped image. For changing the code so the user would pick
     * 4 points, you would need to change the previous command and this
     * command.
     */
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

    /*
     * FileObserver is an abstract class that can be used by defining
     * another class, in this case fileObserver (confusing...). FileObserver
     * allows the program to detect if a file has been accessed, modified,
     * written to, opened, closed, or deleted. This is possibly the easiest
     * way to find out when a photo is taken and edit it.
     */
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

    /*
     * createImageFile creates the file name for the photo and the path to the file.
     * The most important parts of this are the timestamp, the file path, and the
     * file directory. The timestamp is the name for the file, so there should not be
     * any overlap between different photos taken by the same phone. Another way
     * of doing this is to put a counter at the end of the image name. The file path
     * is required for other parts of the program, which is why mCurrentPhotoPath is
     * a global variable. The file directory is a private directory, which means no
     * other apps or phones can access this directory. This is not required, but if
     * the plan is to delete the photos after creating them and converting them, then
     * this allows other programs to not access the photos and for the user to not
     * see them. However, we may want to change it, if the users want to see the photos.
     * If so, change getExternalFilesDir to getExternalStoragePublicDirectory for the
     * storageDir variable. This makes the directory public, so other apps and phones
     * can access them.
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /*
     * dispatchTakePictureIntent tells the phone to use its camera app to take
     * a photo. It also calls code for the file name and path and creates a fileObserver
     * for the file path. Currently, the fileObserver does not "stopWatching" for changes
     * to the file until it goes out of scope. This is due to calling test.stopWatching()
     * before going out of scope ends up with the crop command not being called. Also, the
     * event 32768 gets called and I'm not sure what that does, if test.stopWatching() is put
     * after startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO).
     */
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
