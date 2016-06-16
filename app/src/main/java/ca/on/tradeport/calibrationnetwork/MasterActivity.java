package ca.on.tradeport.calibrationnetwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MasterActivity extends AppCompatActivity implements TasksFragment.OnFragmentInteractionListener, JobsFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener, InstrumentFragment.OnFragmentInteractionListener {

    private static String MY_PREFS_NAME = "session";

    int whichTab = 0;

    SessionManager sessionManager;
    CoordinatorLayout coordinatorLayout;

    Boolean isOnline = false;
    ImageView footerLogoImage;
    LinearLayout logoutButton;

    SharedPreferences sharedPreferences;

    TasksFragment tasksFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);

        sessionManager = new SessionManager(this, coordinatorLayout);
        isOnline = sessionManager.isOnline();

        sharedPreferences = getApplicationContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.masterCoordinatorLayout);
        logoutButton = (LinearLayout) findViewById(R.id.logout_footer_button);
        footerLogoImage = (ImageView) findViewById(R.id.footer_logo_image);

        tasksFragment = new TasksFragment().newInstance(true, "whatever");


        Intent i = getIntent();
        int section = i.getIntExtra("section", 99);



        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.task_tab));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.jobs_tab));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.instruments_tab));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.settings_tab));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        LinearLayout logoutButton = (LinearLayout) findViewById(R.id.logout_footer_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SessionManager.Logout logoutTask = sessionManager.new Logout();
                logoutTask.execute();

            }
        });

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerActivity adapter = new PagerActivity(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        if(section != 99){
            viewPager.setCurrentItem(section);
        }

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                whichTab = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


    }





    @Override
    public void onFragmentInteraction(Uri uri) {
    }




    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
}


    @Override
    protected void onResume() {
        super.onResume();
        isOnline = sessionManager.isSessionValid();
    }



}
