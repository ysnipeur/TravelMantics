package com.mobile.takoumbo.travelmantics;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DaelViewHolder> implements View.OnClickListener {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ArrayList<TravelDeals> listOfDeals;
    private ChildEventListener childEventListener;
    private static  UserActivity callerActivity;


    public DealAdapter(UserActivity caller)
    {
        callerActivity = caller;
        FirebaseUtile.opendFirebaseReference("traveldeals", callerActivity);
        firebaseDatabase = FirebaseUtile.firebaseDatabase;
        databaseReference = FirebaseUtile.databaseReference;

        listOfDeals = FirebaseUtile.listOfDeals;

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                // this method is for getting values from firebase database
                TravelDeals travelDeals = dataSnapshot.getValue(TravelDeals.class);

                Log.d("Deal", travelDeals.getTitle());
                travelDeals.setId(dataSnapshot.getKey());

                // Adding current deal to list of deals

                listOfDeals.add(travelDeals);

                // We need to notify for any data change for our view to be updated

                notifyDataSetChanged();

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

        databaseReference.addChildEventListener(childEventListener);
    }

    @NonNull
    @Override
    public DaelViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.deals_item_layout, viewGroup, false);

        return new DaelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DaelViewHolder holder, int position) {
            TravelDeals currentDeal = listOfDeals.get(position);

            holder.bind(currentDeal);
    }

    @Override
    public int getItemCount() {
        return listOfDeals.size();
    }

    @Override
    public void onClick(View v) {

    }


    public  class DaelViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView description;
        private TextView price;


        public DaelViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewTitle);
            description = itemView.findViewById(R.id.textViewDescription);
            price = itemView.findViewById(R.id.textViewPrice);

            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeals travelDeals)
        {
            title.setText(travelDeals.getTitle());
            description.setText(travelDeals.getDescription());
            price.setText(travelDeals.getPrice());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Log.d("Click position : ", String.valueOf(position));

            TravelDeals selectedDeal = listOfDeals.get(position);

            Intent intent = new Intent(v.getContext(), AdminActivity.class);
            intent.putExtra("selectedDeal", selectedDeal);

            v.getContext().startActivity(intent);
        }
    }
}
