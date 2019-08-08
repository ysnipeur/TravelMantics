package com.mobile.takoumbo.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class AdminActivity extends AppCompatActivity {

    // Variables for firebase connection

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    // Variables for our travel deals

    EditText txtTitle;
    EditText txtPrice;
    EditText txtDescription;

    ImageView imageView;
    private TravelDeals newDeal;

    private static final int PICTURE_RESULT = 47;
    private Uri imageUri;

    private StorageTask uploadTask;
    private Button btnAddImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        // Initialising references
        firebaseDatabase = FirebaseUtile.firebaseDatabase;
        databaseReference = FirebaseUtile.databaseReference;

        txtTitle = findViewById(R.id.txtTitle);
        txtPrice = findViewById(R.id.txtPrice);
        txtDescription = findViewById(R.id.txtDescription);
        imageView = findViewById(R.id.imageSelected);

        btnAddImage = findViewById(R.id.btnSelectImage);

        btnAddImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                FileChooser();
            }

        });

        getSelectedItem();

    }

    private void FileChooser()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, PICTURE_RESULT);
    }

    private String getExtension(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void FileUploader()
    {
       final StorageReference reference = FirebaseUtile.storageReference.child(System.currentTimeMillis()+ "." + getExtension(imageUri));


        uploadTask = reference.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                String imgName = taskSnapshot.getStorage().getPath();
                newDeal.setImageName(imgName);
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        newDeal.setImageUrl(String.valueOf(uri));

                    }
                });



            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICTURE_RESULT && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);


            if(uploadTask != null && uploadTask.isInProgress())
            {
                Toast.makeText(this, "Upload Still in progress", Toast.LENGTH_LONG).show();
            }
            else
            {
                FileUploader();
            }

            showImage(newDeal.getImageUrl());
        }


    }

    private void showImage(String url)
    {
        if(url != null && !(url.isEmpty())) {

            Picasso.with(this)
                    .load(url)
                    .resize(0, 600)
                    .centerCrop()
                    .into(imageView);
        }
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

        showImage(newDeal.getImageUrl());

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
        btnAddImage.setEnabled(isEnabled);

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

        // Method to remove deal from firebase realtime database and the corresponding image from storage

        if(newDeal.getImageName() != null && !newDeal.getImageName().isEmpty())
        {
            StorageReference imageRef = FirebaseUtile.firebaseStorage.getReference().child(newDeal.getImageName());
            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }

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
