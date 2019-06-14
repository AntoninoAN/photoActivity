package com.example.implicitintenttest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.security.AccessController.getContext;


/**
 * You will find here both ways to read from the small version from the camera
 * and the full quality photo from the camera, to change between modes, please use
 * the swithcher.
 */
public class MainActivity extends AppCompatActivity {

    ImageView iv_photo;
    FloatingActionButton fab;
    private final int PHOTO_REQUEST_CODE = 404;
    String currentPhotoPath;
    Switch sw_photo_size;
    private static final String TAG = "MainActivity";
    static boolean checked_large_size = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv_photo = findViewById(R.id.iv_photo);
        fab = findViewById(R.id.fab_take_photo);
        sw_photo_size = findViewById(R.id.sw_photo_size);

        sw_photo_size.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: ");
                sw_photo_size.setText(
                        isChecked ? R.string.txt_sw_large_photo : R.string.txt_sw_small_photo
                );
                checked_large_size = isChecked;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checked_large_size)
                    dispatchTakePictureIntent();
                else
                    takePhoto();
            }
        });
    }

    /**
     * StartActivityForResult will return any response from the interaction with the camera
     * and the user. Could have a picture or not.
     *
     * @param requestCode Checking if this intent match with our {PHOTO_REQUEST_CODE}
     * @param resultCode  Yes or no (-1 or something else)
     * @param data        Intent with data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PHOTO_REQUEST_CODE) {
            if (!checked_large_size) {
                if (data != null) {
                    Bundle samplePhoto = data.getExtras();
                    //we get the small version of the picture.
                    Bitmap rawData = (Bitmap) samplePhoto.get("data");
                    iv_photo.setImageBitmap(rawData);
                }
            } else {
                //if the intent contains the path for the image, generated for the Provider.
                Log.d(TAG, "onActivityResult: currentPhotoPath " + currentPhotoPath);
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
//              Glide.with(this).load(currentPhotoPath).into(iv_photo);                
                iv_photo.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * Simple algorithm to generate a name for the photo.
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Intent for dispatching the path for the external directory.
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = getCameraIntent();
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.implicitintenttest.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, PHOTO_REQUEST_CODE);
            }
        }
    }

    /**
     * Intent for opening the camera. We relay here if the device can handle this request.
     */
    public void takePhoto() {
        Intent takePhotoIntent = getCameraIntent();
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePhotoIntent, PHOTO_REQUEST_CODE);
        }
    }

    /**
     * Prepare a common method for the camera intent.
     * @return
     */
    public Intent getCameraIntent() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        return intent;
    }
}












