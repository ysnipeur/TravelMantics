package com.mobile.takoumbo.travelmantics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class UserActivity extends AppCompatActivity {

    private RecyclerView dealsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeDisplayContent();

    }

    // This method enables me to save when the user clics the save b
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.insert_menu :

                Intent intent = new Intent(this, AdminActivity.class);
                startActivity(intent);
                return  true;

            case R.id.logout_menu :
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Logout", "User Logout");
                                FirebaseUtile.attachListener(); // Called to enable the signin screen to show up after log out
                            }
                        });

                FirebaseUtile.detachListener();
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.user_activity_menu, menu);

        return true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        FirebaseUtile.opendFirebaseReference("traveldeals", this);

        initializeDisplayContent();

        FirebaseUtile.attachListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtile.detachListener();
    }

    public void initializeDisplayContent()
    {
        dealsRecyclerView = findViewById(R.id.list_deals);

        // Setting the layout manager for the dealsRecyclerView

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        dealsRecyclerView.setLayoutManager(layoutManager);

        // Creating our adapter

        DealAdapter dealAdapter = new DealAdapter(this);

        dealsRecyclerView.setHasFixedSize(true);
        dealsRecyclerView.setAdapter(dealAdapter);
    }

    public void showMenus()
    {
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.insert_menu);

        menuItem.setVisible(FirebaseUtile.isAdmin);

        return  true;
    }
}
