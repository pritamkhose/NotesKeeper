package com.pritam.noteskeeper.notes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.pritam.noteskeeper.R;
import com.pritam.noteskeeper.fingerprint.FingerprintActivity;
import com.pritam.noteskeeper.pin.PinEnterActivity;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class HomeActivity extends AppCompatActivity implements ColorPickerDialogListener {

    ArrayList<HashMap<String, Object>> aList = new ArrayList<>();
    View activityView;
    static public Boolean ExternalCard = true, onStartup = true;
    public static final int PERMISSION_REQUEST_CODE = 200;
    //AlertDialog alertDialog;
    View promptsView;
    Dialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openpopup(0, false);
            }
        });

        activityView = getWindow().getDecorView().getRootView();


        {
            LayoutInflater li = LayoutInflater.from(this);
            promptsView = li.inflate(R.layout.notes_detail, null);
//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//            //alertDialogBuilder.setTitle("New Note");
//            alertDialogBuilder.setView(promptsView);
//            // create alert dialog
//            alertDialog = alertDialogBuilder.create();
//            alertDialog.setCanceledOnTouchOutside(true);

            alertDialog = new Dialog(this, android.R.style.Theme_Holo_Light_NoActionBar);
            alertDialog.setContentView(promptsView);

            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    saveNotes();
                }
            });
        }

    }

    private void saveNotes() {
        if (promptsView != null) {
            String title = ((EditText) promptsView.findViewById(R.id.title)).getText().toString();
            String details = ((EditText) promptsView.findViewById(R.id.detail)).getText().toString();
            if ((details != null && details.length() > 0) || (title != null && title.length() > 0)) {
                HashMap<String, Object> hm = new HashMap<>();
                String noteid = ((TextView) promptsView.findViewById(R.id.noteid)).getText().toString();
                hm.put("id", noteid);

                if (details != null && details.length() > 0)
                    hm.put("details", details);
                else
                    hm.put("details", "");


                if (title != null && title.length() > 0)
                    hm.put("title", title);
                else
                    hm.put("title", "");

                if (aList.size() == 0)
                    hm.put("serialno", 1);
                else
                    hm.put("serialno", aList.size());

                String headercolor = ((TextView) promptsView.findViewById(R.id.headercolor)).getText().toString();
                if (headercolor != null && headercolor.length() > 0)
                    hm.put("marked", headercolor);
                else
                    hm.put("marked", "");


                hm.put("deleted", "0");
                hm.put("secure", "0");

                DBHelper DBHelper = new DBHelper(getApplicationContext());
                if (noteid != null && noteid.length() > 1) {
                    DBHelper.updateNotes(hm);
                } else {
                    DBHelper.createNotes(hm, false);

                }

                aList = DBHelper.getNotes("");
                DBHelper.close();
                ListViewRefresh();
//                            Snackbar.make(activityView, "Note Saved", Snackbar.LENGTH_LONG)
//                                    .setAction("Action", null).show();
                alertDialog.dismiss();

                //

            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (!checkPermission()) {
            requestPermission();
        } else {
            try {
                if (onStartup)
                    onStartup();
            } catch (Exception e) {
                Toast.makeText(this, "As Permission Denied,\nApplication unable work", Toast.LENGTH_SHORT).show();
            }
        }

    }

//    @Override
//    public void onStop() {
//        super.onStop();
//
//        Intent intent = new Intent(this, FingerprintActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
//    }

	/*--------------------------------------------------------------------*/


    private boolean checkPermission() {

        // madatory device bulid compileSdkVersion < 23

        return ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//                &&ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                ;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{
                WRITE_EXTERNAL_STORAGE,
                READ_EXTERNAL_STORAGE,
//                ACCESS_FINE_LOCATION,
//                CAMERA,
//                CALL_PHONE,
//                READ_CONTACTS
        }, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean WRITE_EXTERNAL_STORAGE1 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean READ_EXTERNAL_STORAGE1 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
//                    boolean locationAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
//                    boolean cameraAccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED;
//                    boolean CALL_PHONE1 = grantResults[4] == PackageManager.PERMISSION_GRANTED;
//                    boolean READ_CONTACTS1 = grantResults[5] == PackageManager.PERMISSION_GRANTED;

                    //if (locationAccepted && cameraAccepted && WRITE_EXTERNAL_STORAGE1 && READ_EXTERNAL_STORAGE1 && CALL_PHONE1 && READ_CONTACTS1) {
                    //Snackbar.make(view, "Permission Granted, Now you can access location data and camera.", Snackbar.LENGTH_LONG).show();
                    if (WRITE_EXTERNAL_STORAGE1 && READ_EXTERNAL_STORAGE1) {

                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Toast.makeText(this, "As Permission Denied,\nApplication unable work", Toast.LENGTH_LONG).show();

                            if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {//ACCESS_FINE_LOCATION
                                showMessageOKCancel("You need to allow access to All the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{
                                                                    WRITE_EXTERNAL_STORAGE,
                                                                    READ_EXTERNAL_STORAGE,
//                                                                    ACCESS_FINE_LOCATION,
//                                                                    CAMERA,
//                                                                    CALL_PHONE,
//                                                                    READ_CONTACTS
                                                            },
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(HomeActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    private void onStartup() {
        onStartup = false;
        foldercreate();
        DBHelper DBHelper = new DBHelper(this);
        aList = DBHelper.getNotes("");
        DBHelper.close();

        ListViewRefresh();
    }
/*--------------------------------------------------------------------*/

    private void foldercreate() {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "NotesKeeper" + File.separator);
        if (file.exists() == false) {
            File dataDir = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                dataDir = new File(Environment.getExternalStorageDirectory() + File.separator + "NotesKeeper" + File.separator);
                if (!dataDir.isDirectory()) {
                    dataDir.mkdirs();
                }
            }

            if (!dataDir.exists()) {
                ExternalCard = false;
            }

        }
    }

    ListView listView;
    CustomListAdapter adapter;

    private void ListViewRefresh() {
        if (aList.size() == 0) {
            Snackbar.make(activityView, "No Note Found", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        }

        // Getting a reference to listview of main.xml layout file
        listView = (ListView) findViewById(R.id.ListView);
        adapter = new CustomListAdapter(this, aList);        // both note at left side & call btn at right side
        // adapter.notifyDataSetChanged();
        // Setting the adapter to the listView
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                openpopup(position, true);
            }
        });

    }


    void openpopup(int id, boolean newnote) {
        /*LayoutInflater li = LayoutInflater.from(this);
        promptsView = li.inflate(R.layout.notes_detail, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        //alertDialogBuilder.setTitle("New Note");
        alertDialogBuilder.setView(promptsView);

        // create alert dialog
        alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);*/


        ((ImageButton) promptsView.findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNotes();

            }
        });


        ((ImageButton) promptsView.findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNotes();

            }
        });

        ((ImageButton) promptsView.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DBHelper DBHelper = new DBHelper(getApplicationContext());
                DBHelper.deleteNotes(((TextView) promptsView.findViewById(R.id.noteid)).getText().toString());
                aList = DBHelper.getNotes("");
                DBHelper.close();

                Snackbar.make(activityView, "Note Deleted", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                alertDialog.dismiss();

                ListViewRefresh();
            }
        });

        ((ImageButton) promptsView.findViewById(R.id.share)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Share text:
                String text = ((EditText) promptsView.findViewById(R.id.title)).getText().toString()
                        + " -\n" + ((AutoCompleteTextView) promptsView.findViewById(R.id.detail)).getText().toString();
                Intent intent2 = new Intent();
                intent2.setAction(Intent.ACTION_SEND);
                intent2.setType("text/plain");
                intent2.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(intent2, "Share via"));

            }
        });


        ((ImageButton) promptsView.findViewById(R.id.colorpicker)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(activityView.getWindowToken(), 0);

                ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                        .setAllowPresets(false)
                        .setDialogId(DIALOG_ID)
                        .setColor(Color.BLACK)
                        .setShowAlphaSlider(false)
                        .setShowColorShades(true)
                        .show(HomeActivity.this);
            }
        });

        ((ImageButton) promptsView.findViewById(R.id.secure)).setVisibility(View.GONE);
        ((ImageButton) promptsView.findViewById(R.id.secure)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        if (newnote && aList != null && aList.get(id).get("id") != null & aList.get(id).get("id").toString().length() > 0) {
            ((TextView) promptsView.findViewById(R.id.noteid)).setText(aList.get(id).get("id").toString());
            ((EditText) promptsView.findViewById(R.id.title)).setText(aList.get(id).get("title").toString());
            ((AutoCompleteTextView) promptsView.findViewById(R.id.detail)).setText(aList.get(id).get("details").toString());

            String color = aList.get(id).get("marked").toString();
            int color_int = 0;
            if (color != null && color.length() > 0) {
                try {
                    color_int = Integer.parseInt(color);
                } catch (Exception e) {
                    color_int = 0;
                }
            } else {
                color_int = 0;
            }
            String time = aList.get(id).get("time").toString();
            if (time != null && time.length() > 0) {
                ((TextView) promptsView.findViewById(R.id.time_item)).setVisibility(View.VISIBLE);
                ((TextView) promptsView.findViewById(R.id.time_item)).setText("" + time);
            } else {
                ((TextView) promptsView.findViewById(R.id.time_item)).setVisibility(View.GONE);
            }

            ((LinearLayout) promptsView.findViewById(R.id.header)).setBackgroundColor(color_int);
            ((TextView) promptsView.findViewById(R.id.headercolor)).setText(String.valueOf(color_int));

        } else {
            ((TextView) promptsView.findViewById(R.id.noteid)).setText("");
            ((EditText) promptsView.findViewById(R.id.title)).setText("");
            ((AutoCompleteTextView) promptsView.findViewById(R.id.detail)).setText("");
            int color_int = getResources().getColor(R.color.md_orange_300);
            ((LinearLayout) promptsView.findViewById(R.id.header)).setBackgroundColor(color_int);
            ((TextView) promptsView.findViewById(R.id.headercolor)).setText(String.valueOf(color_int));
            ((TextView) promptsView.findViewById(R.id.time_item)).setVisibility(View.GONE);

            ColorPickerDialog.newBuilder()
                    .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                    .setAllowPresets(false)
                    .setDialogId(DIALOG_ID)
                    .setColor(Color.BLACK)
                    .setShowAlphaSlider(false)
                    .setShowColorShades(true)
                    .show(HomeActivity.this);

        }


        // show it
        alertDialog.show();

    }

    // Give your color picker dialog unique IDs if you have multiple dialogs.
    final int DIALOG_ID = 0;

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case DIALOG_ID:
                // We got result from the dialog that is shown when clicking on the icon in the action bar.
                ((LinearLayout) promptsView.findViewById(R.id.header)).setBackgroundColor(color);
                ((TextView) promptsView.findViewById(R.id.headercolor)).setText(String.valueOf(color));

                break;
        }
    }

    @Override
    public void onDialogDismissed(int i) {
        //Toast.makeText(HomeActivity.this, "No Color Selected", Toast.LENGTH_SHORT).show();
    }

    private void doHelp() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("LICENSE");

        // Setting Dialog Message
        alertDialog.setMessage(R.string.ApacheLICENSE);

        alertDialog.setCancelable(true);

        alertDialog.setPositiveButton("Email us", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, "pritamkhose@gmail.com");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback of NotesKeeper");
                intent.putExtra(Intent.EXTRA_TEXT, "Hi Pritam,\n(pritamkhose@gmail.com) \n\n I'm . \n\n Regards,\n");
                //intent.setData(Uri.parse("mailto: pritamkhose@gmail.com"));
                startActivity(Intent.createChooser(intent, "Thanking You for contact us"));

            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                 dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    private void doExit() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
        intent.putExtra("Exit", true);
        startActivity(intent);
        finish();
        System.exit(0);
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            //super.onBackPressed();
            //Exit When Back and Set no History
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
            intent.putExtra("EXIT", true);
            startActivity(intent);
            finish();
            System.exit(0);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Snackbar.make(activityView, "Please click BACK again to exit", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            doExit();
            return true;
        } else if (id == R.id.action_help) {
            doHelp();
            return true;
        } else if (id == R.id.action_menu) {
            menuPopup();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void menuPopup() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.mipmap.ic_launcher);
        builderSingle.setTitle("Menu");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
        //arrayAdapter.add("Deleted Notes");
        arrayAdapter.add("Change Pin");
        arrayAdapter.add("Import");
        arrayAdapter.add("Export");

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                String strName = arrayAdapter.getItem(which);
                switch (which) {
                    case 0 : {
                        passwordPopup();
                    } break;

                    case 1: {
                        importFile();
                    }
                    break;

                    case 2: {
                        exportFile();
                    }
                    break;
                }

                dialog.dismiss();
            }
        });
        builderSingle.show();


    }


        AlertDialog.Builder alertDialogBuilder;
        private void passwordPopup() {

            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.enter_pin, null);
            alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setIcon(R.mipmap.ic_launcher);
            alertDialogBuilder.setTitle("Update Pin");
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

            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   dialog.dismiss();
                }
            });

            alertDialogBuilder.show();

        }

        private void checkCondition(EditText ETpin1, EditText ETpin2, DialogInterface dialog) {
            String pin1 = ETpin1.getText().toString();
            String pin2 = ETpin2.getText().toString();

            if (pin1 != null && pin2 != null && pin1.length() > 0 && pin2.length() > 0) {
                if (pin1.equals(pin2)) {
                    String password = pin1;
                    setSharedPreferences(password);
                    dialog.dismiss();
                    Snackbar.make(activityView, "Pin updated successfully", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                } else {
                    passwordPopup();
                }
            } else {
                passwordPopup();
            }
    }

    public void setSharedPreferences(String key) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("password", key);
        editor.commit();
    }


    private void importFile() {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "NotesKeeper" + File.separator + "NotesKeeper.json");
        if (file.exists()) {

            //Read text from file
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    //text.append('\n');
                }
                br.close();


                Gson gson = new Gson();
                JsonElement element = gson.fromJson(text.toString(), JsonElement.class);
                JsonObject jsonObj = element.getAsJsonObject();
                JsonArray result = jsonObj.getAsJsonArray("results");

                DBHelper DBHelper = new DBHelper(getApplicationContext());
                for (int i = 0; i < result.size(); i++) {
                    HashMap<String, Object> NotesData = new HashMap<>();
                    JsonElement elem = result.get(i);
                    JsonObject jsonObject = elem.getAsJsonObject();
                    NotesData.put("id", jsonObject.get("id").getAsString());
                    NotesData.put("serialno", jsonObject.get("serialno").getAsInt());
                    NotesData.put("secure", jsonObject.get("secure").getAsInt());
                    NotesData.put("deleted", jsonObject.get("deleted").getAsInt());
                    NotesData.put("marked", jsonObject.get("marked").getAsInt());
                    NotesData.put("time", jsonObject.get("time").getAsString());
                    NotesData.put("title", jsonObject.get("title").getAsString());
                    NotesData.put("details", jsonObject.get("details").getAsString());

                    DBHelper.createNotes(NotesData,true);
                }
                aList = DBHelper.getNotes("");
                DBHelper.close();
                ListViewRefresh();

            } catch (Exception e) {
                Toast.makeText(this, "Error occur" + e.toString(), Toast.LENGTH_LONG).show();
            }

        } else
            Toast.makeText(this, "No backup found", Toast.LENGTH_LONG).show();

    }

    private void exportFile() {
        foldercreate();
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "NotesKeeper" + File.separator + "NotesKeeper.json");
            Writer output = new BufferedWriter(new FileWriter(file));
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("results", aList);
            output.write(gson.toJson(data));
            output.close();
            Toast.makeText(this, "File saved", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Unable to save file", Toast.LENGTH_LONG).show();
        }

    }


}
