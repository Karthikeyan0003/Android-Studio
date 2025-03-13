package com.android.iunoob.bloodbank.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.iunoob.bloodbank.R;
import com.android.iunoob.bloodbank.adapters.SearchDonorAdapter;
import com.android.iunoob.bloodbank.viewmodels.DonorData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchDonorFragment extends Fragment {

    private View view;

    private FirebaseAuth mAuth;
    private FirebaseDatabase fdb;
    private DatabaseReference db_ref;

    private Spinner bloodgroup, division;
    private Button btnsearch;
    private ProgressDialog pd;
    private List<DonorData> donorItem;
    private RecyclerView recyclerView;
    private SearchDonorAdapter sdadapter;

    public SearchDonorFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_donor_fragment, container, false);

        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading...");
        pd.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();
        fdb = FirebaseDatabase.getInstance();
        db_ref = fdb.getReference("donors");

        bloodgroup = view.findViewById(R.id.btngetBloodGroup);
        division = view.findViewById(R.id.btngetDivison);
        btnsearch = view.findViewById(R.id.btnSearch);

        getActivity().setTitle("Find Blood Donor");

        btnsearch.setOnClickListener(v -> searchDonors());

        return view;
    }

    private void searchDonors() {
        pd.show();
        donorItem = new ArrayList<>();
        sdadapter = new SearchDonorAdapter(donorItem);
        recyclerView = view.findViewById(R.id.showDonorList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(sdadapter);

        String selectedDivision = division.getSelectedItem().toString();
        String selectedBloodGroup = bloodgroup.getSelectedItem().toString();

        Query qpath = db_ref.child(selectedDivision).child(selectedBloodGroup);
        qpath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                donorItem.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot singleItem : dataSnapshot.getChildren()) {
                        DonorData donorData = singleItem.getValue(DonorData.class);
                        donorItem.add(donorData);
                    }
                    sdadapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), "No donors found!", Toast.LENGTH_LONG).show();
                }
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SearchDonorFragment", "Database error: " + databaseError.getMessage());
                Toast.makeText(getActivity(), "Failed to load data", Toast.LENGTH_LONG).show();
                pd.dismiss();
            }
        });
    }
}
