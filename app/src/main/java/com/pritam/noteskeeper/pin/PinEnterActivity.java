package com.pritam.noteskeeper.pin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.pritam.noteskeeper.R;
import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.pritam.noteskeeper.fingerprint.FingerprintActivity;
import com.pritam.noteskeeper.notes.HomeActivity;

/**
 * Created by Pritam on 10/15/2017.
 */

public class PinEnterActivity extends AppCompatActivity {

    private PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;
    private Integer pinlength = 4;
    private String password = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pin);

        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);

        ((TextView) findViewById(R.id.finger)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PinEnterActivity.this, FingerprintActivity.class));
            }
        });

        password = getSharedPreferences();
        if (password != null && password.length() > 0) {
            pinlength = password.length();
            setNewPin(password.length());

        } else {
            passwordPopup();
        }
    }

    //    @Override
//    public void onBackPressed()
//    {
//        moveTaskToBack(true);
//    }


    AlertDialog.Builder alertDialogBuilder;
    private void passwordPopup() {

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.enter_pin, null);
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setIcon(R.mipmap.ic_launcher);
        alertDialogBuilder.setTitle("Enter Pin");
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setCancelable(false);

        final EditText ETpin1 = ((EditText) promptsView.findViewById(R.id.pin1));
        final EditText ETpin2 = ((EditText) promptsView.findViewById(R.id.pin2));

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkCondition(ETpin1, ETpin2, dialog);
            }
        });

        alertDialogBuilder.show();

    }

    private void checkCondition(EditText ETpin1, EditText ETpin2, DialogInterface dialog) {
        String pin1 = ETpin1.getText().toString();
        String pin2 = ETpin2.getText().toString();

        if (pin1 != null && pin2 != null && pin1.length() > 0 && pin2.length() > 0) {
            if (pin1.equals(pin2)) {
                password = pin1;
                pinlength = password.length();
                setSharedPreferences(password);
                dialog.dismiss();
                setNewPin(password.length());
            } else {
                passwordPopup();
            }
        } else {
            passwordPopup();
        }
    }

    private void setNewPin(Integer pinlength) {
        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(mPinLockListener);
        //mPinLockView.setCustomKeySet(new int[]{2, 3, 1, 5, 9, 6, 7, 0, 8, 4});
        //mPinLockView.enableLayoutShuffling();

        mPinLockView.setPinLength(pinlength);
        mPinLockView.setTextColor(ContextCompat.getColor(this, R.color.white));

        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);
    }


    private PinLockListener mPinLockListener = new PinLockListener() {
        @Override
        public void onComplete(String pin) {

            if (pin != null && pin.length() > 0) {
                if (pin.length() > 1 && pin.length() < pinlength) {
                    ((TextView) findViewById(R.id.errorTextpin)).setText(" ");
                } else {
                    if (pin.equals(password)) {
                        startActivity(new Intent(PinEnterActivity.this, HomeActivity.class));
                    } else {
                        ((TextView) findViewById(R.id.errorTextpin)).setText("Wrong Pin");
                    }
                }
            } else
                ((TextView) findViewById(R.id.errorTextpin)).setText("Enter Pin");

        }

        @Override
        public void onEmpty() {
            ((TextView) findViewById(R.id.errorTextpin)).setText("Enter Pin");
        }

        @Override
        public void onPinChange(int pinLength, String intermediatePin) {
            //Log.d(TAG, "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
            ((TextView) findViewById(R.id.errorTextpin)).setText(" ");
        }
    };

    public void setSharedPreferences(String key) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("password", key);
        editor.commit();
    }

    private String getSharedPreferences() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        String key = pref.getString("password", null);
        return key;
    }
}