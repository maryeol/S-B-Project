package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

public class GenerateTicket extends AppCompatActivity {

    private static final String TAG = "GenerateTicket";

    private ImageView qrCodeIV;
    private Button save;
    private String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/QRCode";
    private TextView id,pan,lane,time;

    Bitmap bitmap;
    QRGEncoder qrgEncoder;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_ticket);

        qrCodeIV = findViewById(R.id.idIVQrcode);
        save = findViewById(R.id.idBtnSaveeQR);
        id = findViewById(R.id.idEdt);
        pan = findViewById(R.id.panEdt);
        lane = findViewById(R.id.laneEdt);
        time = findViewById(R.id.dateEdt);

        //Get intent from MainActivity
        Intent intent = getIntent();
        String URL = intent.getStringExtra("url");

        String sID = String.valueOf(decode(getID(URL)));
        String sPan= decodePAN(extractpan(URL));
        String sLane = String.valueOf(extractlane(URL));
        String sTime =  String.valueOf(decode(extracttime(URL)));
        String newStringTime = sTime.substring(0, 2)
                + ":"
                + sTime.substring(2 , 4)
                + ":"
                + sTime.substring(4,6);
        String sFullDate = LocalDate.now() + "  " +newStringTime;

        id.setText("Parking ID Ticket : " + sID);
        pan.setText("Parking PAN : " + sPan);
        lane.setText("Entry number : " + sLane);
        time.setText("Entry In : "+ sFullDate);

        //Generation Of Ticker
        Log.d(TAG, URL);

        // below line is for getting
        // the windowmanager service.
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // initializing a variable for default display.
        Display display = manager.getDefaultDisplay();

        // creating a variable for point which
        // is to be displayed in QR Code.
        Point point = new Point();
        display.getSize(point);

        // getting width and
        // height of a point
        int width = point.x;
        int height = point.y;

        // generating dimension from width and height.
        int dimen = width < height ? width : height;
        dimen = dimen * 3 / 4;

        // setting this dimensions inside our qr code
        // encoder to generate our qr code.
        qrgEncoder = new QRGEncoder(savePath, null, QRGContents.Type.TEXT, dimen);

        // getting our qrcode in the form of bitmap.
        bitmap = qrgEncoder.getBitmap();
        // the bitmap is set inside our image
        // view using .setimagebitmap method.
        qrCodeIV.setImageBitmap(bitmap);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED ){
                    try {
                        System.out.println(savePath);
                        boolean save = new QRGSaver().save(savePath, URL, bitmap, QRGContents.ImageType.IMAGE_JPEG);
                        System.out.println(save);
                        String result = save ? "Image Saved" : "Image Not Saved";
                        Toast.makeText(GenerateTicket.this, result, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    ActivityCompat.requestPermissions(GenerateTicket.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    ActivityCompat.requestPermissions(GenerateTicket.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

                }
            }
        });
        //Log.d(TAG, "Save Of Ticket Success");
    }

    //Methods to
    //extract data from url to display
    //on TextView
    public String getID(String input){
        return input.substring(7,9) ;
    }
    public String extractlane(String input){
        return input.substring(9,10);
    }
    public String extractpan(String input){
        return input.substring(10,21);
    }
    public String extracttime(String input){
        return input.substring(21,24);
    }
    public long decode(String input){
        String order = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-";
        int base = order.length();
        long num = 0, r;
        while (input.length()>0) {
            r = order.indexOf(input.charAt(0));
            input = input.substring(1);
            num = num * base;
            num = num + r;
        }
        return num;
    }
    public String decodePAN(String input){
        String order = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-";
        int base = order.length();
        long num1 = 0, num2 = 0, r1 , r2;

        String str1=input.substring(0,5);
        String str2=input.substring(5,10);

        while (str1.length()>0) {
            r1 = order.indexOf(input.charAt(0));
            str1 = str1.substring(1);
            num1 = num1 * base;
            num1 = num1 + r1;
        }
        while (str2.length()>0) {
            r2 = order.indexOf(input.charAt(0));
            str2 = str2.substring(1);
            num2 = num2 * base;
            num2 = num2 + r2;
        }

        return String.valueOf(num1) + String.valueOf(num2);
    }
}