package com.mobile.takoumbo.travelmantics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminActivity extends AppCompatActivity {

    // Variables for firebase connection

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    // Variables for our travel deals

    EditText txtTitle;
    EditText txtPrice;
    EditText txtDescription;
    private TravelDeals newDeal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        // Initialising references

        //FirebaseUtile.opendFirebaseReference("traveldeals", this);
        firebaseDatabase = FirebaseUtile.firebaseDatabase;
        databaseReference = FirebaseUtile.databaseReference;

        txtTitle = findViewById(R.id.txtTitle);
        txtPrice = findViewById(R.id.txtPrice);
        txtDescription = findViewById(R.id.txtDescription);

        getSelectedItem();


    }


    // this method is to handle the travel deal selected by a user

    public void getSelectedItem()
    {
        Intent intent = getIntent();

        TravelDeals selectedDeal = (TravelDeals) intent.getSerializableExtra("selectedDeal");

        if(selectedDeal == null)
        {
            newDeal = new TravelDeals();
        }
        else
        {
            this.newDeal = selectedDeal;
            txtTitle.setText(newDeal.getTitle());
            txtDescription.setText(newDeal.getDescription());
            txtPrice.setText(newDeal.getPrice());
        }

    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.save_menu);
        MenuItem menuItemDelete = menu.findItem(R.id.delete_menu);

        menuItemDelete.setVisible(FirebaseUtile.isAdmin);
        menuItem.setVisible(FirebaseUtile.isAdmin);

        enableTextFields(FirebaseUtile.isAdmin);


        return  true;
    }

    // This method enables me to save when the user clics the save button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.save_menu :
                saveDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                clean();
                return  true;

            case R.id.delete_menu :
                deleteDeal();
                Toast.makeText(this, "Deal deleted", Toast.LENGTH_LONG).show();
                backToListOfDeals();
                return true;

            default :
                return super.onOptionsItemSelected(item);
        }

    }

    private void clean() {
        txtTitle.setText("");
        txtPrice.setText("");
        txtDescription.setText("");
    }

    // Method to enable and disable edit text fields
    private void enableTextFields(boolean isEnabled)
    {
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
    }

    // Method for saving a deal

    private void saveDeal() {
        newDeal.setTitle(txtTitle.getText().toString());
        newDeal.setDescription(txtDescription.getText().toString());
        newDeal.setPrice(txtPrice.getText().toString());

        // inserting data in the database with push method

        // If the id is null, it means we are creating a new deal else we are updating a deal
        if(newDeal.getId() == null)
            databaseReference.push().setValue(newDeal);
        else
            databaseReference.child(newDeal.getId()).setValue(newDeal);
    }


    private void deleteDeal()
    {
        if(newDeal == null)
        {
            Toast.makeText(this, "Please save the deal before dealiting", Toast.LENGTH_LONG).show();

            return;
        }

        // Method to remove deal from firebase realtime database

        databaseReference.child(newDeal.getId()).removeValue();
    }

    // Method to return back to list of deals after succesfully deleting a deal

    private void backToListOfDeals()
    {
        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
    }

    // Inserting menu options in my activity

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save_menu, menu);

        return true;
    }
}
