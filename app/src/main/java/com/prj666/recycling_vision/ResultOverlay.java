package com.prj666.recycling_vision;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ResultOverlay extends AppCompatActivity {


    private TextView instructions;
    private String percentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_overlay);

        Intent intent = getIntent();
        byte [] bytes = intent.getExtras().getByteArray("picture");
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        instructions = findViewById(R.id.instructions);
        TextView objectName = findViewById(R.id.objectName);
        TextView matchProbability = findViewById(R.id.matchProbability);
        TextView matchFound = findViewById(R.id.matchResult);
        ImageView image = findViewById(R.id.objectImage);
        Button back = findViewById(R.id.back);

        percentage = intent.getStringExtra("matchPercent");
        String displayPercent = percentage + "% match";
        matchProbability.setText(displayPercent);

        String object = intent.getStringExtra("object");
        objectName.setText(object);
        image.setImageBitmap(bmp);

        if(object.equals("none")){
            matchFound.setText("Match Not Found");
            instructions.setText("Please try again");
        }
        else{
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://recycling-vision.herokuapp.com/item/single";
            Map<String, String> jsonData = new HashMap<>();
            jsonData.put("itemName", object);
            JSONObject json = new JSONObject(jsonData);

            final String[] result = {""};

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, url, json, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            result[0] = response.getString("data");
                            instructions.setText(result[0]);
                            /* TODO: REENABLE THIS CODE ONCE userID IS ACCESSIBLE
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                            byte [] imageData = baos.toByteArray();
                            String base64Img = Base64.encodeToString(imageData, 0);
                            int userID = ;

                            RequestQueue historyQueue = Volley.newRequestQueue(ResultOverlay.this);
                            String historyUrl = "https://recycling-vision.herokuapp.com/matchhistoryitem";
                            Map<String, String> insertJsonData = new HashMap<>();
                            insertJsonData.put("objectName", object);
                            insertJsonData.put("probabilityMatch", percentage);
                            insertJsonData.put("objectImage", base64Img);
                            insertJsonData.put("foundRecyclingInstruction", result[0]);
                            insertJsonData.put("userID", userID);

                            JSONObject historyJson = new JSONObject(insertJsonData);

                            JsonObjectRequest historyReq = new JsonObjectRequest(Request.Method.POST,
                                    historyUrl, historyJson, new Response.Listener<JSONObject>(){

                                @Override
                                public void onResponse(JSONObject response) {
                                    System.out.println("Match history saved");
                                }
                            }, new Response.ErrorListener(){

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    System.out.println("Error saving match history");
                                    error.getStackTrace();
                                }
                            });
                            */

                        } else {
                            result[0] = "Error";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    result[0] = "error";
                }
            });
            queue.add(request);
        }


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photo = new Intent(ResultOverlay.this, TakePhoto.class);
                ResultOverlay.this.startActivity(photo);
            }
        });
    }

}