package ca.on.tradeport.calibrationnetwork;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



public class SessionManager {


    public Context context;
    public CoordinatorLayout coordinatorLayout;
    public String error_message;
    public int login_success = 0, logout_success = 0;

    SharedPreferences sharedPreferences;
    public static String MY_PREFS_NAME = "session";
    public RelativeLayout spinnerContainer;
    public Snackbar snackbar;
    Boolean isInternetActive = false;

    String loginToCalValue ="";


    private final String loginURL = "http://com.tradeport.on.ca/index.php";
    private final String logoutURL = "http://com.tradeport.on.ca/logout.php";
    private final String updateSessionURL = "http://com.tradeport.on.ca/update_session.php";


    public SessionManager(Context context, CoordinatorLayout coordinatorLayout){

        this.context = context;
        this.coordinatorLayout = coordinatorLayout;
        this.sharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        this.isInternetActive = isOnline();
    }



    public SessionManager(Context context){

        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        this.isInternetActive = fragmentIsOnline();
    }

    public SessionManager(Context context, RelativeLayout spinnerContainer, CoordinatorLayout coordinatorLayout) {

        this.context = context;
        this.coordinatorLayout = coordinatorLayout;
        this.sharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        this.spinnerContainer = spinnerContainer;
        this.isInternetActive = isOnline();

    }



    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();



        if (netInfo == null) {

            snackbar = Snackbar.make(coordinatorLayout,
                    "To continue please make sure your device is connected to the internet.",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();

                        }
                    });
            snackbar.setActionTextColor(context.getResources().getColor(R.color.cn_green));
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(Color.DKGRAY);
            TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snackbar.show();


        }

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public boolean fragmentIsOnline() {


        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if(netInfo == null) {

            Toast.makeText(context, "To continue please make sure your device is connected to the internet.", Toast.LENGTH_LONG).show();

        }

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    public class LoginBackground extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(!isInternetActive){
                this.cancel(true);
            }

            error_message = "";
            login_success = 0;

            spinnerContainer.setAlpha(1);


        }

        @Override
        protected String doInBackground(String... params) {

            loginToCalValue = params[4];


            RequestBody formBody = new FormBody.Builder()
                    .add("username", params[0])
                    .add("password", params[1])
                    .add("oneSignalUserID", params[2])
                    .add("deviceID", params[3])
                    .add("tag", "login")
                    .build();


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(loginURL).post(formBody).build();
            Response response = null;
            String result = null;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                result = response.body().string();
                Log.i("testing", result);

                JSONObject jsonObject = new JSONObject(result);

                //check if success is equal to 1
                int success = jsonObject.getInt("success");

                if (success == 1){

                    login_success = 1;

                    SQLiteDatabase sessionDB = context.openOrCreateDatabase("Sessions", Context.MODE_PRIVATE, null);
                    sessionDB.execSQL("CREATE TABLE IF NOT EXISTS failed_logout_sessions (id INTEGER PRIMARY KEY, username VARCHAR, key VARCHAR)");
                    sessionDB.execSQL("CREATE TABLE IF NOT EXISTS login_sessions (id INTEGER PRIMARY KEY, userID INTEGER, expire VARCHAR, username VARCHAR, key VARCHAR)");

                    //if successful, delete all existing sesion information and save the new one

                    sessionDB.execSQL("DELETE FROM login_sessions");

                    JSONArray userInfoArray = jsonObject.getJSONArray("user");

                    JSONObject object2 = userInfoArray.getJSONObject(0);


                    String username = object2.getString("username");
                    String userID = object2.getString("userID");
                    String expire = object2.getString("expire");
                    String key = object2.getString("key");

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("session_key",key);
                    editor.putString("userID", userID);
                    editor.putString("expire", expire);
                    editor.putString("username", username);
                    editor.commit();


                    //save session in local persistent storage
                    //lets do sqlite, that way we can also use this on other types of devices
                    String sql = "INSERT INTO login_sessions (userID, expire, username, key) VALUES (?, ?, ?, ?)";

                    SQLiteStatement statement = sessionDB.compileStatement(sql);

                    statement.bindString(1, userID);
                    statement.bindString(2, expire);
                    statement.bindString(3, username);
                    statement.bindString(4, key);

                    statement.execute();
                    sessionDB.close();

                } else {

                    // display error message
                    error_message = jsonObject.getString("error_msg");
                    login_success = 0;

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }




            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            spinnerContainer.setAlpha(0);

            if(!error_message.matches("") && login_success == 0) {

                Toast.makeText(context, error_message, Toast.LENGTH_SHORT).show();

            } else {

                if(login_success == 1) {

                    if(loginToCalValue.equals("master")) {

                        Intent i = new Intent(context, MasterActivity.class);
                        context.startActivity(i);

                    }else {

                        loginToCalValue = "";
                        Intent i = new Intent(context, CalDueList.class);
                        context.startActivity(i);

                    }

                }

            }



        }
    }





    public class Logout extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            error_message = "";
            logout_success = 0;


        }

        @Override
        protected String doInBackground(String... params) {

            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            String username = sharedPreferences.getString("username", "");
            String key = sharedPreferences.getString("session_key", "");

            if(netInfo != null) {

                RequestBody formBody = new FormBody.Builder()
                        .add("username", username)
                        .add("key", key)
                        .add("tag", "logout")
                        .build();


                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(logoutURL).post(formBody).build();
                Response response = null;
                String result = null;

                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {

                    result = response.body().string();
                    JSONObject jsonObject = new JSONObject(result);

                    //check if success is equal to 1
                    int success = jsonObject.getInt("success");

                    if (success == 1) {

                        logout_success = 1;

                        SQLiteDatabase sessionDB = context.openOrCreateDatabase("Sessions", Context.MODE_PRIVATE, null);
                        sessionDB.execSQL("DELETE FROM login_sessions");

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("session_key", "");
                        editor.putString("userID", "");
                        editor.putString("expire", "");
                        editor.putString("username", "");
                        editor.commit();
                        sessionDB.close();


                    } else {

                        // display error message
                        error_message = jsonObject.getString("message");
                        logout_success = 0;

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {

                SQLiteDatabase sessionDB = context.openOrCreateDatabase("Sessions", Context.MODE_PRIVATE, null);
                //let's keep track of failed session deletion and when the internet comes back we can
                //check if that session wasn't deleted and have the server delete it.
                String sql = "INSERT INTO failed_logout_sessions (username, key) VALUES (?, ?)";
                SQLiteStatement statement = sessionDB.compileStatement(sql);
                statement.bindString(1, username);
                statement.bindString(2, key);
                statement.execute();



                sessionDB.execSQL("DELETE FROM login_sessions");

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("session_key", "");
                editor.putString("userID", "");
                editor.putString("expire", "");
                editor.putString("username", "");
                editor.commit();

                sessionDB.close();
                logout_success = 1;

            }



            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(!error_message.matches("") && logout_success == 0) {

                Toast.makeText(context, error_message, Toast.LENGTH_SHORT).show();

            } else {

                if(logout_success == 1) {

                    Activity thisActivity = (Activity) context;

                    if(!thisActivity.getClass().getSimpleName().toString().matches("Login")) {
                        Intent i = new Intent(context, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                    }

                }

            }


        }
    }


    public class UpdateSession extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            isInternetActive = isOnline();
            if(!isInternetActive){
                this.cancel(true);
            }
            error_message = "";
            login_success = 0;



        }

        @Override
        protected String doInBackground(String... params) {

            SQLiteDatabase sessionDB = context.openOrCreateDatabase("Sessions", Context.MODE_PRIVATE, null);
            sessionDB.execSQL("CREATE TABLE IF NOT EXISTS failed_logout_sessions (id INTEGER PRIMARY KEY, username VARCHAR, key VARCHAR)");

            sharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
            String mUsername = sharedPreferences.getString("username", "");
            String mKey = sharedPreferences.getString("session_key", "");

            RequestBody formBody = new FormBody.Builder()
                    .add("username", mUsername)
                    .add("key", mKey)
                    .add("tag", "update_session")
                    .build();


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(updateSessionURL).post(formBody).build();
            Response response = null;
            String result = null;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                result = response.body().string();

                JSONObject jsonObject = new JSONObject(result);

                //check if success is equal to 1
                int success = jsonObject.getInt("success");

                if (success == 1){

                    login_success = 1;

                    JSONArray userInfoArray = jsonObject.getJSONArray("user");
                    JSONObject object2 = userInfoArray.getJSONObject(0);

                    String newExpire = object2.getString("expire");
                    String updateSQL = "UPDATE login_sessions SET expire = ? WHERE username = ? AND key = ?";
                    SQLiteStatement updateSQLStatement = sessionDB.compileStatement(updateSQL);
                    updateSQLStatement.bindString(1, newExpire);
                    updateSQLStatement.bindString(2, mUsername);
                    updateSQLStatement.bindString(3, mKey);
                    updateSQLStatement.execute();


                    //if successful, update expire time
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("expire", newExpire);
                    editor.commit();


                    //check if any failed logout sessions exists then check if they are still available
                    //online then have them deleted



                    String selectFailedLogoutSessionsQuery = "SELECT * FROM failed_logout_sessions";
                    Cursor c = sessionDB.rawQuery(selectFailedLogoutSessionsQuery, null);

                    if(c!=null && c.getCount()>0){

                        int fusername = c.getColumnIndex("username");
                        int fkey = c.getColumnIndex("age");

                        c.moveToFirst();

                        while (!c.isLast()) {

                            RequestBody failedLogoutFormBody = new FormBody.Builder()
                                    .add("username", c.getColumnName(fusername))
                                    .add("key", c.getColumnName(fkey))
                                    .add("tag", "logout")
                                    .build();


                            Request failedLogoutRequest = new Request.Builder().url(logoutURL).post(failedLogoutFormBody).build();
                            Response failedLogoutResponse = null;
                            String failedLogoutResult = null;

                            try {
                                failedLogoutResponse = client.newCall(failedLogoutRequest).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            c.moveToNext();

                        }

                        c.close();

                        sessionDB.execSQL("DELETE FROM failed_logout_sessions");
                    }





                } else {

                    // display error message
                    error_message = jsonObject.getString("error_msg");
                    login_success = 0;

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            sessionDB.close();

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if((!error_message.matches("")) && login_success == 0) {

                Toast.makeText(context, error_message, Toast.LENGTH_SHORT).show();

            } else {

                if(login_success == 1) {

                    Activity thisActivity = (Activity) context;

                    if(thisActivity.getClass().getSimpleName().toString().matches("Login")) {
                        Intent intent = new Intent(context, MasterActivity.class);
                        context.startActivity(intent);
                    }

                }



            }



        }
    }


    public Boolean isSessionValid() {

        sharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        String expired = sharedPreferences.getString("expire", "");
        Long tsLong = System.currentTimeMillis()/1000;
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Log.i("testing_expired", expired);

        try {
            Date expireTimeStamp = formatter.parse(expired);
            Date currentTimeStamp = new Date();

            if(expireTimeStamp.before(currentTimeStamp)){



                // the session is expired, return to login page and ask for credentials


                Activity thisActivity = (Activity) context;


                if(!thisActivity.getClass().getSimpleName().toString().matches("Login")) {

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("session_key","");
                    editor.putString("userID", "");
                    editor.putString("expire", "");
                    editor.putString("username", "");
                    editor.commit();



                    Intent i = new Intent(context, Login.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                    Toast.makeText(context, "Your session is expired, Please login to proceed." , Toast.LENGTH_LONG).show();


                }
            } else {

                UpdateSession updateSession = new UpdateSession();
                updateSession.execute();

            }



        } catch (ParseException e) {
            e.printStackTrace();
        }


        return false;

    }

    public Void updateExpireDate(String newExpiryDate){

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("expire", newExpiryDate);
        editor.commit();

        String mUsername = sharedPreferences.getString("username", "");
        String mKey = sharedPreferences.getString("session_key", "");

        SQLiteDatabase sessionDB = context.openOrCreateDatabase("Sessions", Context.MODE_PRIVATE, null);
        String updateSQL = "UPDATE login_sessions SET expire = ? WHERE username = ? AND key = ?";
        SQLiteStatement updateSQLStatement = sessionDB.compileStatement(updateSQL);
        updateSQLStatement.bindString(1, newExpiryDate);
        updateSQLStatement.bindString(2, mUsername);
        updateSQLStatement.bindString(3, mKey);
        updateSQLStatement.execute();
        sessionDB.close();


        return null;

    }




}
