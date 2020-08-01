package com.prj666.recycling_vision;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TakePhoto extends AppCompatActivity implements ConfirmPictureFragment.ConfirmPictureListener {
    private byte[] img; //temp
    private String filename;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap bmp;
    private ImageView previewImage;

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        previewImage = findViewById(R.id.image_preview);
        Button takePhoto = findViewById(R.id.takephoto);
        final Button sendPhoto = findViewById(R.id.sendphoto);
        Button back = findViewById(R.id.back_takephoto);
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(TakePhoto.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                }
                else{
                    ActivityCompat.requestPermissions(TakePhoto.this, new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
                    if(ContextCompat.checkSelfPermission(TakePhoto.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        takePicture();
                    }
                    else{
                        Toast.makeText(TakePhoto.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        sendPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bmp != null){
                    pictureConfirmation();
                }
                else{
                    Toast.makeText(TakePhoto.this, "Please take a photo first", Toast.LENGTH_SHORT).show();
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void takePicture(){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(i.resolveActivity(getPackageManager()) != null){
            startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if(bmp != null){
                bmp.recycle();
            }
            Bundle extras = data.getExtras();
            bmp = (Bitmap) extras.get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp = Bitmap.createScaledBitmap(bmp, 512, 512, true);
            bmp.compress(Bitmap.CompressFormat.JPEG, 0, stream);

            previewImage.setImageBitmap(bmp);
            img = stream.toByteArray();
            File storage = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File image = File.createTempFile("rv-photo_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()), ".jpg", storage);
                filename = image.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendPhoto() throws IOException {
        RequestQueue queue = Volley.newRequestQueue(this);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String url = "https://rv-tensorflow.herokuapp.com/upload";

                OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(60, TimeUnit.SECONDS).build();
                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file",filename, RequestBody.create(MediaType.parse("image/jpeg"), img))
                         .build();
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Response response = null;
                System.out.println("alright");
                try {
                    response = client.newCall(request).execute();
                    System.out.println("executing");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(response.isSuccessful()){
                    System.out.println("good");
                    previewImage.buildDrawingCache();
                    Bitmap image= previewImage.getDrawingCache();
                    Bundle extras = new Bundle();
                    extras.putParcelable("image", image);
                    Intent resultOverlay = new Intent(TakePhoto.this, ResultOverlay.class);
                    resultOverlay.putExtras(extras);
                    startActivity(resultOverlay);
                }
                else{
                    System.out.println("no response from server in time");
                }
            }
        };

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 5, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
        threadPool.execute(runnable);
        Map<String, String> jsonData = new HashMap<>();

        JSONObject json = new JSONObject(jsonData);
    }

    private void pictureConfirmation(){
        FragmentManager fm = getSupportFragmentManager();
        ConfirmPictureFragment dialog = ConfirmPictureFragment.newInstance("Send photo");
        dialog.show(fm, "confirm_picture");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) throws IOException {
        sendPhoto();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length <= 0 || grantResults[0] != 0) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onBackPressed() {
        startActivity(new Intent(this, Navigation.class));
        finishAffinity();
    }
}
