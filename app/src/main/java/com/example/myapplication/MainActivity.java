package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private static final String TAG = "Main Activity";
    private BluetoothAdapter mBluetoothAdapter;
    private BeaconManager beaconManager;

    // for enabling GPS
    private Context context;
    private LocationManager locationManager ;

    // For Display
    private Map<String, BeaconModel> beaconModelMap;
    private CustomBeaconAdapter mAdapter;

    // for the recycle view
    private List<BeaconModel> mBeacons;

    // for transmission
    private List<BeaconTransmitter> beaconList = new ArrayList<>();

    // for ticket generation
    private Button btnTicket;

    String s = generateURL();  //e.g : s= "http://3021"
    String original_id = s.substring(7,11);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check if phone support bluetooth
        initVariable();
        //Initiate the adapter
        initUI();
        //Enable Bluetooth and GPS Location
        checkBluetoothStatus();
        CheckGpsStatus();
        //Start transmit beacon eddystone URL
        startTransmit();
        //Button to generate ticket
        btnTicket = findViewById(R.id.CreateTicket);
        btnTicket.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try{
                    int i = 0;
                    System.out.println(decode(getID(mBeacons.get(i).getUrl())));
                    System.out.println(s.substring(7,11));
                    String x = String.valueOf(decode(getID(mBeacons.get(i).getUrl())));
                    String y =s.substring(7,11);
                    while (!x.equals(y)){
                        i= i + 1;
                    }
                    String URL = mBeacons.get(i).getUrl();
                    Intent intent = new Intent (MainActivity.this , GenerateTicket.class);
                    intent.putExtra("url", URL);
                    startActivity(intent);
                }
                catch(Exception e){
                    Context context = getApplicationContext();
                    CharSequence text = "Cannot Generate Ticket!";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
    }

    private void initVariable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        }

        mBeacons = new ArrayList<>();
        beaconModelMap = new HashMap<>();

        // for the recycle view
        mAdapter = new CustomBeaconAdapter(this, mBeacons);
    }
    private void initUI() {
        ListView beaconList = (ListView) findViewById(R.id.beacon_list);
        beaconList.setAdapter(mAdapter);
    }

    private void checkBluetoothStatus() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mBluetoothAdapter.isEnabled()) {
                    Toast.makeText(MainActivity.this, "Turning bluetooth...", Toast.LENGTH_SHORT).show();
                    mBluetoothAdapter.enable();
                    Toast.makeText(MainActivity.this, "Bluetooth On", Toast.LENGTH_SHORT).show();
                }
                checkLocationPermission();
            }
        }, 1000);
    }
    private void CheckGpsStatus() {
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.d(TAG, "GPS is enabeled already!");
        }
        else{
            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent1);
        }

    }
    private void checkLocationPermission() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int rc = ActivityCompat.checkSelfPermission(this, permission);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, 1000);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int numOfRequest = grantResults.length;
        boolean isGranted = numOfRequest == 1 && PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1];
        String permission;
        if (requestCode == 1000) {
            permission = Manifest.permission.ACCESS_FINE_LOCATION;
            if (isGranted) {
                onPermissionGranted();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    checkLocationPermission();
                } else {
                    showOpenPermissionSetting();
                }
            }
        }
    }
    void showOpenPermissionSetting() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage("Location Permission Required")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 1001);
                    }
                })
                .show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            checkLocationPermission();
        }
    }
    private void onPermissionGranted() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();

        // Detect the main identifier (URL) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));

        beaconManager.bind(this);
    }

    private void startTransmit() {
        beaconList.clear();
        generateBeaconURL(s);
    }
    //Generate Unique ID
    //To transmit it as URL.
    private String generateURL() {
        int max = 4095;
        int min = 1000;
        String generatedString = "http://";
        int generatedint = (int)(Math.random() * (max - min)) + min;
        generatedString = generatedString + Integer.toString(generatedint) ;
        Log.d(TAG , generatedString);
        return generatedString;
    }
    //Emit Eddystone Beacon URL
    private void generateBeaconURL(String URL){
        try {
            byte[] urlBytes = UrlBeaconUrlCompressor.compress(URL);
            Identifier encodedUrlIdentifier = Identifier.fromBytes(urlBytes, 0, urlBytes.length, false);
            ArrayList<Identifier> identifiers = new ArrayList<Identifier>();
            identifiers.add(encodedUrlIdentifier);

            Beacon beacon = new Beacon.Builder()
                    .setBluetoothName("Mariem")
                    .setIdentifiers(identifiers)
                    .setManufacturer(0x0188)
                    .setTxPower(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                    .build();

            BeaconParser beaconParser = new BeaconParser()
                    .setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v");

            BeaconTransmitter beaconTransmitter = new BeaconTransmitter(
                    getApplicationContext(), beaconParser);
            beaconTransmitter.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
            beaconTransmitter.startAdvertising(beacon);


        } catch (MalformedURLException e) {
            Log.d("ww", "That URL cannot be parsed");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
        for (int i = 0; i < beaconList.size(); i++)
            beaconList.get(i).stopAdvertising();
    }

    //Scan Eddystone Beacon URL
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    //Log.d(TAG, "Scan Beacon: " + beacons.size());
                    for (Beacon beacon : beacons) {
                        //This is to insert the beacon in the map(mac, beacon)
                        //Get the Mac Address ( index of Map )
                        String Address = beacon.getBluetoothAddress();
                        if (!beaconModelMap.containsKey(Address)) {
                            try {
                                BeaconModel model1 = new BeaconModel();
                                model1.setBleAddress(Address);

                                //Set the Url
                                String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
                                model1.setUrl(url);

                                // Display the Url

                                String decoded_id = String.valueOf(decode(getID(url)));

                                if (original_id.equals(decoded_id)){
                                    btnTicket.setEnabled(true);
                                    System.out.println("url : " + url);
                                    System.out.println(" ID : " + getID(url) +
                                            " decoded ID : " + decode(getID(url))+
                                            " Lane : " + extractlane(url) +
                                            " extracted PAN : " + extractpan(url) +
                                            " decoded PAN : " + decodePAN(extractpan(url)) +
                                            " Time : " + extracttime(url) +
                                            " decoded Time : " + decode(extracttime(url))
                                    );
                                }

                                //Set the Rssi
                                String rssi = String.valueOf(beacon.getRssi());
                                model1.setRssi(rssi);

                                beaconModelMap.put(Address, model1);
                            }
                            catch (Exception e){
                                System.out.println("Exception has occured, probably null URL" );
                            }
                        }
                        else {
                            try {
                                beaconModelMap.get(Address).setRssi(String.valueOf(beacon.getRssi()));
                                String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
                                beaconModelMap.get(Address).setUrl(url);

                                // Display the Url

                                String decoded_id = String.valueOf(decode(getID(url)));
                                if (original_id.equals(decoded_id)){
                                    btnTicket.setEnabled(true);
                                    System.out.println("url : " + url);
                                    System.out.println(" ID : " + getID(url) +
                                            " decoded ID : " + decode(getID(url))+
                                            " Lane : " + extractlane(url)+
                                            " extracted PAN : " + extractpan(url)+
                                            " decoded PAN : " + decodePAN(extractpan(url))+
                                            " extracted Time : " + extracttime(url)+
                                            " decoded Time : " + decode(extracttime(url))
                                    );
                                }
                            }
                            catch (Exception e){
                                System.out.println("Exception has occured, probably null URL" );
                            }
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBeacons.clear();
                            mBeacons.addAll(beaconModelMap.values());
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("mariem", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

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