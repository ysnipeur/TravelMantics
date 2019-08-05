package com.mobile.takoumbo.travelmantics;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class FirebaseUtile {
    public static FirebaseDatabase firebaseDatabase;
    public static DatabaseReference databaseReference;
    public  static FirebaseAuth firebaseAuth;
    public static  FirebaseAuth.AuthStateListener authStateListener;

    public static FirebaseStorage firebaseStorage;
    public static StorageReference storageReference;

    private static FirebaseUtile firebaseUtile;
    private static UserActivity caller;
    private static final int RC_SIGN_IN = 123;
    public static ArrayList<TravelDeals> listOfDeals;

    public static boolean isAdmin;

    // Empty constructor to avoir the class to be instanciated
    private FirebaseUtile()
    {

    }

    public static void opendFirebaseReference(String ref, final UserActivity callerActivity)
    {
        // If this method has not yet been called

        if(firebaseUtile == null)
        {
            firebaseUtile = new FirebaseUtile();
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseAuth = FirebaseAuth.getInstance();

            caller = callerActivity;
            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                    if(firebaseAuth.getCurrentUser() == null) {
                        FirebaseUtile.signIn();
                    }
                    else {
                        String currentUserId = firebaseAuth.getUid();
                        
                        checkIfAdmin(currentUserId);

                        Toast.makeText(callerActivity.getBaseContext(), "Welcome back!!!!", Toast.LENGTH_LONG).show();

                    }

                }
            };

            connectStorage();


        }
        listOfDeals = new ArrayList<>();
        databaseReference = firebaseDatabase.getReference().child(ref);

    }

    private static void checkIfAdmin(String currentUserId) {

        FirebaseUtile.isAdmin = false;
        DatabaseReference reference = firebaseDatabase.getReference().child("administrators").child(currentUserId);

        ChildEventListener childEventListener =  new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtile.isAdmin = true;

                //Log.d("Admin", "Your are an administrator");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        reference.addChildEventListener(childEventListener);

        caller.showMenus();
    }

    private static void signIn()
    {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()

        );

        // Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    public static void attachListener()
    {
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    public static void detachListener()
    {
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    public static void connectStorage()
    {
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("deals_pictures");
    }
}
