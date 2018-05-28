package com.pritam.noteskeeper.notes;

/**
 * Created by Pritam on 10/14/2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    // Logcat tag
    //private static final String LOG = DBHelper.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 1;      // change version when added new method

    // Database Name
    private static final String DATABASE_NAME = "NotesKeeper";


    public DBHelper(final Context context) {
        super(context,  DATABASE_NAME, null, DATABASE_VERSION);
    }

//    public DBHelper(Context context) {
//
//        super(context, Environment.getExternalStorageDirectory() + File.separator + "NotesKeeper" +
//                File.separator + DATABASE_NAME, null, DATABASE_VERSION);
//
//
//        if (HomeActivity.ExternalCard)
//            SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory() + File.separator + "NotesKeeper" +
//                    File.separator + DATABASE_NAME, null);
//        else
//            SQLiteDatabase.openOrCreateDatabase(DATABASE_NAME, null);
//    }



    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables

        db.execSQL("create table NotesData(id text primary key, serialno integer , secure integer ,deleted integer , marked integer, time text, title text, details text)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS NotesData");

        // create new tables
        onCreate(db);
    }

    // closing database
    @Override
    public synchronized void close() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            db.close();
            super.close();
        }
    }

    /**
     * get datetime
     */
    Date date = new Date();
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd-MM-yyyy hh:mm a", Locale.getDefault());
        return dateFormat.format(date);
    }


    private String getID() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyMMddHHmmss", Locale.getDefault());
        return dateFormat.format(date);
    }



    public void deleteNotes(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (name != null && name.length() > 0) {
            db.delete("NotesData", " id = ?", new String[]{name});
//        } else//delete all
//        {
//            db.execSQL(" delete from NotesData ");
//        }
        }
    }

    public long createNotes(HashMap<String,Object> dataal ,Boolean importdata) { //ArrayList<HashMap<String,Object>> dataal
        SQLiteDatabase db = this.getWritableDatabase();
        long ccnt = 0;
        try {
//            db.beginTransaction();
//            String sql = "Insert or Replace into NotesData (id , serialno, secure, deleted, marked, time , title , details ) values(?,?,?,?,?,?,?,?)";
//            SQLiteStatement insert = db.compileStatement(sql);
//
//            //for (int i = 0; i < dataal.size(); i++)
//            {
//                insert.bindString(1, getID());
//                insert.bindString(2, dataal.get("serialno").toString()); //.get(i)
//                insert.bindString(3, dataal.get("secure").toString());
//                insert.bindString(4, "0");
//                insert.bindString(5, dataal.get("marked").toString());
//                insert.bindString(6, getDateTime());
//                insert.bindString(7, dataal.get("title").toString());
//                insert.bindString(8, dataal.get("details").toString());
//                insert.execute();
//                ccnt++;
//            }
//            db.setTransactionSuccessful();

            String sql;
                if(importdata) {
                    sql = "Insert or Replace into NotesData (id , serialno, secure, deleted, marked, time , title , details ) values( '" +
                            dataal.get("id").toString() + "' , " + dataal.get("serialno").toString() + "," + dataal.get("secure").toString() + "," +
                            "0" + "," + dataal.get("marked").toString() + ",'" +  dataal.get("time").toString()+ "','" + dataal.get("title").toString() + "','" + dataal.get("details").toString() + "' )";

                } else {
                    sql = "Insert or Replace into NotesData (id , serialno, secure, deleted, marked, time , title , details ) values( '" +
                            getID() + "' , " + dataal.get("serialno").toString() + "," + dataal.get("secure").toString() + "," +
                            "0" + "," + dataal.get("marked").toString() + ",'" + getDateTime()  + "','" + dataal.get("title").toString() + "','" + dataal.get("details").toString() + "' )";

                }

           // System.out.println("---> "+ sql);
            db.execSQL(sql);

        } catch (Exception e) {
        } finally {
           // db.endTransaction();
        }
        db.close();
        return ccnt;

    }

    public ArrayList<HashMap<String,Object>> getNotes(String query) {

        ArrayList<HashMap<String,Object>>  data = new ArrayList();
        String selectQuery = "SELECT  * FROM NotesData ";
        if (query != null && query.length() > 2)
            selectQuery = selectQuery + " where title like '" + query + "%' ";

        try {

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (c.moveToFirst()) {
                do {
                    HashMap<String, Object> NotesData = new HashMap<>();
                    // get to list id , , secure, , ,  ,  ,
                    NotesData.put("id", c.getString(c.getColumnIndex("id")));
                    NotesData.put("serialno", c.getInt(c.getColumnIndex("serialno")));
                    NotesData.put("secure", c.getInt(c.getColumnIndex("secure")));
                    NotesData.put("deleted", c.getInt(c.getColumnIndex("deleted")));
                    NotesData.put("marked", c.getInt(c.getColumnIndex("marked")));
                    NotesData.put("time", c.getString(c.getColumnIndex("time")));
                    NotesData.put("title", c.getString(c.getColumnIndex("title")));
                    NotesData.put("details", c.getString(c.getColumnIndex("details")));

                    data.add(NotesData);

                } while (c.moveToNext());
            }
        } catch (Exception e) {
            //System.out.println("ERROR:::"+e);
        }

        return data;
    }

    public long updateNotes(HashMap<String,Object> data) {
        SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("serialno", data.get("serialno").toString());
            values.put("secure", data.get("secure").toString());
            values.put("deleted", data.get("deleted").toString());
            values.put("marked", data.get("marked").toString());
            values.put("time", getDateTime());
            values.put("title", data.get("title").toString());
            values.put("details", data.get("details").toString());
            // update row
            db.update("NotesData", values, "id" + " = ?", new String[]{data.get("id").toString()});

            db.close();

        return 0;
    }

    public long updateAllNotes(ArrayList<HashMap<String,Object>> data) {
        SQLiteDatabase db = this.getWritableDatabase();

        long ccnt = 0;
            for (int i = 0; i < data.size(); i++) {
                ContentValues values = new ContentValues();
                values.put("serialno", data.get(i).get("serialno").toString());
                values.put("secure", data.get(i).get("secure").toString());
                values.put("deleted", data.get(i).get("deleted").toString());
                values.put("marked", data.get(i).get("marked").toString());
                values.put("time", getDateTime());
                values.put("title", data.get(i).get("title").toString());
                values.put("details", data.get(i).get("details").toString());

                // update row
                db.update("NotesData", values, "id" + " = ?", new String[]{data.get(i).get("id").toString()});
                ccnt++;
            }
            db.close();

        return ccnt;
    }

}
