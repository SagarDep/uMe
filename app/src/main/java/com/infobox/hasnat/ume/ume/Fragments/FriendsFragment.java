package com.infobox.hasnat.ume.ume.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infobox.hasnat.ume.ume.Models.Friends;
import com.infobox.hasnat.ume.ume.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFrRecyclerView;
    private View mView;

    private DatabaseReference friendsDatabaseReference;
    private DatabaseReference userDatabaseReference;
    private FirebaseAuth mAuth;

    String current_user_id;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFrRecyclerView = (RecyclerView)mView.findViewById(R.id.friendList);


        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        friendsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friends").child(current_user_id);
        friendsDatabaseReference.keepSynced(true); // for offline

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        userDatabaseReference.keepSynced(true); // for offline

        mFrRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return mView;
    }



    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>
                (
                        Friends.class,
                        R.layout.all_peoples_profile_display,
                        FriendsViewHolder.class,
                        friendsDatabaseReference
                ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {

                viewHolder.setDate(model.getDate());

                String user_id_list = getRef(position).getKey();

                userDatabaseReference.child(user_id_list).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("user_name").getValue().toString();
                        String userThumbPhoto = dataSnapshot.child("user_thumb_image").getValue().toString();

                        viewHolder.setUserName(userName);
                        viewHolder.setUserThumbPhoto(userThumbPhoto, getContext());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mFrRecyclerView.setAdapter(friendsRecyclerAdapter);
    }



    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View m_view;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            m_view = itemView;
        }

        public void setDate(String date){
            TextView friendshipDate = (TextView)m_view.findViewById(R.id.all_user_status);
            friendshipDate.setText(date);
        }

        public void setUserName(String userName){
            TextView user_name = (TextView)m_view.findViewById(R.id.all_user_name);
            user_name.setText(userName);
        }

        public void setUserThumbPhoto(final String userThumbPhoto, final Context ctx){

            final CircleImageView thumb_photo = (CircleImageView)m_view.findViewById(R.id.all_user_profile_img);

            if(!thumb_photo.equals("default_image")) { // default image condition for new user

                Picasso.get()
                        .load(userThumbPhoto)
                        .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                        .placeholder(R.drawable.default_profile_image)
                        .into(thumb_photo, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(userThumbPhoto)
                                        .placeholder(R.drawable.default_profile_image)
                                        .into(thumb_photo);
                            }
                        });
            }

        }


    }
}
