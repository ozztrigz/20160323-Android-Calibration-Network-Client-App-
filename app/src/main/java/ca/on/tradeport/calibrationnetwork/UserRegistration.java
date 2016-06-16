package ca.on.tradeport.calibrationnetwork;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserRegistration extends AppCompatActivity {

    private final String registerURL = "http://com.tradeport.on.ca/register_user.php";

    EditText name, email, address1, address2, province, city, country, postalcode, username, password;
    String sName, sEmail, sAddress1, sAddress2, sProvince, sCity, sCountry, sPostalcode, sUsername, sPassword;
    Button register, cancel;



    int registration_success = 0;
    String error_message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        //customer information
        name = (EditText) findViewById(R.id.registrationName);
        email = (EditText) findViewById(R.id.registrationEmail);
        address1 = (EditText) findViewById(R.id.registrationAddress1);
        address2 = (EditText) findViewById(R.id.registrationAddress2);
        province = (EditText) findViewById(R.id.registrationProvince);
        city = (EditText) findViewById(R.id.registrationCity);
        country = (EditText) findViewById(R.id.registrationCountry);
        postalcode = (EditText) findViewById(R.id.registrationPostalCode);

        //customer login information
        username = (EditText) findViewById(R.id.registrationUsername);
        password = (EditText) findViewById(R.id.registrationPassword);

        register = (Button) findViewById(R.id.registerButton);
        cancel = (Button) findViewById(R.id.cancelRegisterButton);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validateFields();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }


    public class ProcessRegistration extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            error_message = "";
            registration_success = 0;

        }

        @Override
        protected String doInBackground(String... params) {



            RequestBody formBody = new FormBody.Builder()
                    .add("username", sUsername)
                    .add("email", sEmail)
                    .add("address1", sAddress1)
                    .add("address2", sAddress2)
                    .add("city", sCity)
                    .add("province", sProvince)
                    .add("country", sCountry)
                    .add("postalcode", sPostalcode)
                    .add("username", sUsername)
                    .add("password", sPassword)
                    .add("tag", "register_new_user")
                    .build();


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(registerURL).post(formBody).build();
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

                    registration_success = 1;


                } else {

                    // display error message
                    error_message = jsonObject.getString("error_msg");
                    registration_success = 0;

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

            if(!error_message.matches("") && registration_success == 0) {

                Toast.makeText(getApplicationContext(), error_message, Toast.LENGTH_SHORT).show();

            } else {

                if(registration_success == 1) {

                    Toast.makeText(getApplicationContext(), "Registration successful. Check your email to activate your account.", Toast.LENGTH_LONG).show();

                    Intent i = new Intent(getApplicationContext(), Login.class);
                    startActivity(i);
                }

            }

        }
    }

    public void validateFields() {

        //customer information
        sName = name.getText().toString();
        sEmail = email.getText().toString();
        sAddress1 = address1.getText().toString();
        sAddress2 = address2.getText().toString();
        sProvince = province.getText().toString();
        sCity = city.getText().toString();
        sCountry = country.getText().toString();
        sPostalcode = postalcode.getText().toString();

        //customer login information
        sUsername = username.getText().toString();
        sPassword = password.getText().toString();

        if(!sName.matches("") && !sEmail.matches("") && !sAddress1.matches("") &&
                !sProvince.matches("") && !sCity.matches("") && !sCountry.matches("") &&
                !sPostalcode.matches("") && !sUsername.matches("") && !sPassword.matches("")) {

            ProcessRegistration registration = new ProcessRegistration();
            registration.execute();


        } else {

            Toast.makeText(getApplicationContext(), "Some required fields are missing. The missing fields have been highlited.", Toast.LENGTH_LONG).show();

            //highlight missing fields
            if(sName.matches("")){
                name.setBackgroundResource(R.drawable.highlited_textfield);
            } else {
                name.setBackgroundResource(R.drawable.white_textfield);
            }

            if(sEmail.matches("")){
                email.setBackgroundResource(R.drawable.highlited_textfield);
            } else {
                email.setBackgroundResource(R.drawable.white_textfield);
            }

            if(sAddress1.matches("")){
                address1.setBackgroundResource(R.drawable.highlited_textfield);
            } else {
                address1.setBackgroundResource(R.drawable.white_textfield);
            }

            if(sProvince.matches("")){
                province.setBackgroundResource(R.drawable.highlited_textfield);
            } else {
                province.setBackgroundResource(R.drawable.white_textfield);
            }

            if(sCity.matches("")){
                city.setBackgroundResource(R.drawable.highlited_textfield);
            } else {
                city.setBackgroundResource(R.drawable.white_textfield);
            }

            if(sCountry.matches("")){
                country.setBackgroundResource(R.drawable.highlited_textfield);
            } else {
                country.setBackgroundResource(R.drawable.white_textfield);
            }

            if(sPostalcode.matches("")){
                postalcode.setBackgroundResource(R.drawable.highlited_textfield);
            } else {
                postalcode.setBackgroundResource(R.drawable.white_textfield);
            }

            if(sUsername.matches("")){
                username.setBackgroundResource(R.drawable.highlited_textfield);
            } else {
                username.setBackgroundResource(R.drawable.white_textfield);
            }

            if(sPassword.matches("")){
                password.setBackgroundResource(R.drawable.highlited_textfield);
            } else {
                password.setBackgroundResource(R.drawable.white_textfield);
            }

        }


    }

}
