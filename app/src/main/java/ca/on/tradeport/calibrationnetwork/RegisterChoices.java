package ca.on.tradeport.calibrationnetwork;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class RegisterChoices extends AppCompatActivity {

    Button existingUserButton, newUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_choices);

        existingUserButton = (Button) findViewById(R.id.existingUserButton);
        newUserButton = (Button) findViewById(R.id.newUserButton);

        existingUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent existingIntent = new Intent(getApplicationContext(), ExistingUserRegistration.class);
                startActivity(existingIntent);

            }
        });

        newUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent newIntent = new Intent(getApplicationContext(), UserRegistration.class);
            }
        });



    }
}
