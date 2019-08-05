package com.mobile.takoumbo.travelmantics;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AdminActivity extends AppCompatActivity {

    // Variables for firebase connection

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICTURE_RESULT && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            final StorageReference reference = FirebaseUtile.storageReference.child(imageUri.getLastPathSegment());
            reference.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // Let's get the downloaded image to store for our deal

                    String url = reference.getDownloadUrl().toString();
                    newDeal.setImageUrl(url);
                }
            });
        }
    }


// Variables for our travel deals

    EditText txtTitle;
    EditText txtPrice;
    EditText txtDescription;
    private TravelDeals newDeal;

    private static final int PICTURE_RESULT = 47;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        // Initialising references

        //FirebaseUtile.opendFirebaseReference("traveldeals", this);
        firebaseDatabase = FirebaseUtile.firebaseDatabase;
        databaseReference = FirebaseUtile.databaseReference;

        Button btnAddImage = findViewById(R.id.btnSelectImage);

        btnAddImage.setOnClickListener(new View.OnClickListener()


        {

            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "SELECT AN IMAGE"), PICTURE_RESULT);
            }

        });

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
