package ca.on.tradeport.calibrationnetwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.onesignal.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Login extends AppCompatActivity {


    public JSONObject data;
    public Context context;

    ProgressWheel pwOne;



    private static boolean activityStarted;
    private static String MY_LOGIN_PREFS = "credentials";

    private String regID = "";
    private String oneSignalUser = "";

    public RelativeLayout spinnerContainer;
    EditText username, password;
    CheckBox rememberme;
    Snackbar snackbar;
    public int loginToCal;

    Button toRegistration;
    CoordinatorLayout coordinatorLayout;

    String error_message = "";
    Boolean isInternetActive = false;
    SessionManager sessionManager, spinnerManager;
    SessionManager.LoginBackground lgTask;
    int isremembered = 0;
    SharedPreferences rememberPreferences;
    public String additionalNotificationData = "";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.WARN);

        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {

            @Override
            public void idsAvailable(String userId, String registrationId) {

                oneSignalUser = userId;
                if (registrationId != null)
                    regID = registrationId;
                Log.i("testing_one", oneSignalUser+ " || "+regID );
            }
        });

        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new MyNotificationOpened())
                .autoPromptLocation(true)
                .init();



/*

        if (activityStarted
                && getIntent() != null
                && (getIntent().getFlags() & Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) != 0) {

            return;
        }

        activityStarted = true;

*/

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.loginCoordinatorLayout);
        sessionManager = new SessionManager(this, coordinatorLayout);
        spinnerContainer = (RelativeLayout) findViewById(R.id.spinnerContainer);
        spinnerManager = new SessionManager(this, spinnerContainer, coordinatorLayout);
        pwOne = (ProgressWheel) findViewById(R.id.progressBarTwo);
        pwOne.startSpinning();
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        rememberme = (CheckBox) findViewById(R.id.rememberMeCheckbox);
        toRegistration = (Button) findViewById(R.id.toRegistration);

        toRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterChoices.class);
                startActivity(intent);
            }
        });


        rememberPreferences = getSharedPreferences(MY_LOGIN_PREFS, Context.MODE_PRIVATE);
        isremembered = rememberPreferences.getInt("rememberme", 0);

        if(isremembered == 1) {

            username.setText(rememberPreferences.getString("username", ""));
            password.setText(rememberPreferences.getString("password", ""));


            if(!rememberme.isChecked()){
                rememberme.setChecked(true);
            }
        }



    }

    public void login(View view){


        isInternetActive = sessionManager.isOnline();

        String userString = String.valueOf(username.getText());
        String passString = String.valueOf(password.getText());

        if (rememberme.isChecked()) {

            //if rememberme is checked
            SharedPreferences.Editor editor = rememberPreferences.edit();
            editor.putString("username", userString);
            editor.putString("password", passString);
            editor.putInt("rememberme", 1);
            editor.commit();

        } else {

            //if rememberme is not checked
            SharedPreferences.Editor editor = rememberPreferences.edit();
            editor.putString("username", "");
            editor.putString("password", "");
            editor.putInt("rememberme", 0);
            editor.commit();

        }



        if(userString.matches("") || passString.matches("")) {
            Toast.makeText(getApplicationContext(), "To login, both Username and Password are required.", Toast.LENGTH_LONG).show();

        } else {

            if(isInternetActive) {

                lgTask = new SessionManager(this, spinnerContainer, coordinatorLayout).new LoginBackground();

                SQLiteDatabase instrumentsdb = this.openOrCreateDatabase("Instruments", Context.MODE_PRIVATE, null);
                instrumentsdb.execSQL("CREATE TABLE IF NOT EXISTS instrumentsDueForCal (id INTEGER PRIMARY KEY, instrumentID VARCHAR)");
                Cursor c = instrumentsdb.rawQuery("SELECT * FROM instrumentsDueForCal", null);

                if(c != null && c.getCount() > 0) {
                    lgTask.execute(userString, passString, oneSignalUser, regID, "cal");
                } else {
                    lgTask.execute(userString, passString, oneSignalUser, regID, "master");
                }


            } else {
                snackbar = Snackbar.make(coordinatorLayout,
                        "To continue please make sure your device is connected to the internet.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                            }
                        });
                snackbar.setActionTextColor(getResources().getColor(R.color.cn_green));
                View snackBarView = snackbar.getView();
                snackBarView.setBackgroundColor(Color.DKGRAY);
                TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snackbar.show();
            }
        }


    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.i("testing_methods", "onresume");
        Log.i("testing_methods", "logintocal " + String.valueOf(loginToCal));

        sessionManager.isSessionValid();

        isremembered = rememberPreferences.getInt("rememberme", 0);

        if(isremembered == 1) {

            username.setText(rememberPreferences.getString("username", ""));
            password.setText(rememberPreferences.getString("password", ""));

            if(!rememberme.isChecked()){
                rememberme.isSelected();
            }
        }


    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i("testing_methods", "onstop");
        Log.i("testing_methods", "logintocal " + String.valueOf(loginToCal));
    }



    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        savedInstanceState.putInt("loginToCal", 1);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        loginToCal = savedInstanceState.getInt("loginToCal");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }





    public class MyNotificationOpened implements OneSignal.NotificationOpenedHandler {
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {


            Log.e("OneSignalExample", "message: " + result.notification.payload.body);
            Log.e("OneSignalExample", "additional data: " + result.notification.payload.additionalData);
            loginToCal = 1;
            additionalNotificationData = result.notification.payload.additionalData.toString();
            SQLiteDatabase instrumentsDB = Login.this.openOrCreateDatabase("Instruments", Context.MODE_PRIVATE,null);
            instrumentsDB.execSQL("CREATE TABLE IF NOT EXISTS instrumentsDueForCal (id INTEGER PRIMARY KEY, instrumentID VARCHAR)");
            instrumentsDB.execSQL("DELETE FROM instrumentsDueForCal");



            String sql = "INSERT INTO instrumentsDueForCal (instrumentID) VALUES (?)";

            try {
                JSONArray jsonArray = result.notification.payload.additionalData.getJSONArray("instruments");
                JSONArray instrumentArray = jsonArray.getJSONArray(0);

                for(int i = 0; i < instrumentArray.length(); i++){
                    Log.i("instrumentsAdded", instrumentArray.getString(i));
                    SQLiteStatement statement = instrumentsDB.compileStatement(sql);
                    statement.bindString(1, instrumentArray.getString(i));
                    statement.execute();

                }

                instrumentsDB.close();


            } catch (JSONException e) {
                e.printStackTrace();
            }



            Log.i("testing_onesignal", result.toString());
        }

    }
}


