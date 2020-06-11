package com.rahtech.runussd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE = 1; // just some integer to identify request
    EditText input;
    Spinner spinner;
    Button button;

    Map<String, Integer> SIM_map;
    ArrayList<String> simcardNames;

    TelephonyManager telephonyManager;
    TelephonyManager.UssdResponseCallback ussdResponseCallback;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        input = findViewById(R.id.input);
        spinner = findViewById(R.id.spinner);
        button = findViewById(R.id.submit);

        SIM_map = new HashMap<>();
        simcardNames = new ArrayList<>();

        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        ussdResponseCallback = new TelephonyManager.UssdResponseCallback() {
            @Override
            public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                //if our request is successful then we get response here

                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {

                //request failures will be catched here

                Toast.makeText(MainActivity.this, failureCode, Toast.LENGTH_SHORT).show();

                //here failure reasons are in the form of failure codes
            }
        };

        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg); //no need to change anything here
            }
        };
        //we use SUBSCRIPTION MANAGER to get details about SIM cards in mobile

        //For running USSD code we are going to use Telephony Manager for that we req a callback and a handler

        //run ussd code on clicking button

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //here we have 2 chances

                //1. calling USSD code using preferred SIM card in users mobile but we have a problem with this
                //when users did not fix their prferred SIM it will give error
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE},REQ_CODE);

                }
                //telephonyManager.sendUssdRequest(input.getText().toString(), ussdResponseCallback, handler);
                //this takes 3 values as input
                //1.ussd code to run
                //2.callback
                //3.handler

                //2.calling ussd from particular SIM card using subs Id we stored in map before
                String selected_SIM=spinner.getSelectedItem().toString();
                telephonyManager.createForSubscriptionId(SIM_map.get(selected_SIM)).sendUssdRequest(input.getText().toString()
                        ,ussdResponseCallback,handler);

                //that's it we are ready to go
            }
        });

    }

    private void sims_details() {

        //store all available sim connection info as list

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //request permission if its not granted already

            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},REQ_CODE);
        }
        List<SubscriptionInfo> subscriptionInfos = SubscriptionManager.from(getApplicationContext()).getActiveSubscriptionInfoList();
        //this requires READ_PHONE_STATE permission

        //loop through all info objects and store info we store 2 details here carrier name and subscription Id fro each active SIM card
        //so we need map and an array to set SIM card names to Spinner

        for(SubscriptionInfo subscriptionInfo:subscriptionInfos){

            SIM_map.put(subscriptionInfo.getCarrierName().toString(),subscriptionInfo.getSubscriptionId());
            simcardNames.add(subscriptionInfo.getCarrierName().toString());
        }
        //we create arrayadapter to set it to spinner

        ArrayAdapter arrayAdapter=new ArrayAdapter(MainActivity.this,R.layout.support_simple_spinner_dropdown_item,simcardNames);
        spinner.setAdapter(arrayAdapter);
    }

    //override on req permission granted here


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            sims_details();
            //calling function again once we get permissions granted
        }
    }
}