package com.hearth.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.R;
import com.hearth.methods.MyMethods;
import com.hearth.objects.FamilyMember;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.familyMemberViewHolder>{

    Context context;
    ArrayList<FamilyMember> familyMemberArrayList;
    private OnFamilyMemberListener mOnFamilyMemberListener;

    public FamilyMemberAdapter(Context context, ArrayList<FamilyMember> familyMemberArrayList, OnFamilyMemberListener onFamilyMemberListener) {
        this.context = context;
        this.familyMemberArrayList = familyMemberArrayList;
        this.mOnFamilyMemberListener = onFamilyMemberListener;
    }

    @NonNull
    @Override
    public familyMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_family_member, parent, false);
        return new familyMemberViewHolder(view, mOnFamilyMemberListener, familyMemberArrayList);
    }

    @Override
    public void onBindViewHolder(@NonNull familyMemberViewHolder holder, int position) {
        FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
        DatabaseReference memberScoreRef;

        FamilyMember familyMember = familyMemberArrayList.get(position);
        String fullname = familyMember.getFullname();
        String role = familyMember.getRole();
        String photourl = familyMember.getPhotourl();
        String uid = familyMember.getUid();

        memberScoreRef = hearthDB.getReference("user_"+uid+"_xp");
        memberScoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String score = snapshot.getValue().toString();
                holder.score.setText(score);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.fullname.setText(fullname);
        MyMethods.Generate.image(photourl, holder.photo);


        String currentUserRole = MyMethods.Cache.getString(context, "role");
        if (currentUserRole.equals("Admin")){
            if (role.equals("Admin")){
                holder.role.setText("("+role+")");
                holder.btnArrow.setVisibility(View.VISIBLE);
                holder.btnAdminToggle.setText("Remove admin");
                holder.btnAdminToggle.setIconResource(R.drawable.outline_remove_moderator_24);
            }
            else if (role.equals("Member")){
                holder.role.setVisibility(View.GONE);
                holder.btnAdminToggle.setText("Make admin");
                holder.btnArrow.setVisibility(View.VISIBLE);
                holder.btnAdminToggle.setIconResource(R.drawable.outline_add_moderator_24);
            }
            if (uid.equals(MyMethods.Cache.getString(context, "uid"))){
                holder.btnArrow.setVisibility(View.GONE);
                holder.fullname.setText("You");
            }
            else {
                holder.btnArrow.setVisibility(View.VISIBLE);
            }
        }
        else {
            holder.btnArrow.setVisibility(View.GONE);
            holder.role.setVisibility(View.GONE);
            if (uid.equals(MyMethods.Cache.getString(context, "uid"))){
                holder.fullname.setText("You");
            }
        }
    }

    @Override
    public int getItemCount() {
        return familyMemberArrayList.size();
    }

    public static class familyMemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        RoundedImageView photo;
        MaterialButton btnAdminToggle, btnArrow, btnKick;
        TextView fullname, role, score;
        OnFamilyMemberListener onFamilyMemberListener;
        public familyMemberViewHolder(@NonNull View itemView, OnFamilyMemberListener onFamilyMemberListener, ArrayList<FamilyMember> familyMemberArrayList) {
            super(itemView);

            FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();

            fullname = itemView.findViewById(R.id.textViewFamilyMemberFullName);
            role = itemView.findViewById(R.id.textViewFamilyMemberRole);
            score = itemView.findViewById(R.id.textViewFamilyMemberScore);
            photo = itemView.findViewById(R.id.imgProfilePicture);
            btnAdminToggle = itemView.findViewById(R.id.btnAdminToggle);
            btnArrow = itemView.findViewById(R.id.btnArrow);
            btnKick = itemView.findViewById(R.id.btnKick);
            this.onFamilyMemberListener = onFamilyMemberListener;

            String currentUserRole = MyMethods.Cache.getString(itemView.getContext(), "role");
            if (currentUserRole.equals("Admin")){
                itemView.setOnClickListener(view -> {
                    String familyMemberUid = familyMemberArrayList.get(getAbsoluteAdapterPosition()).getUid();
                    String currentUserId = MyMethods.Cache.getString(itemView.getContext(), "uid");

                    if (currentUserId.equals(familyMemberUid)){
                        return;
                    }

                    if (btnAdminToggle.getVisibility() == View.GONE){
                        btnArrow.setIconResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
                        btnKick.setVisibility(View.VISIBLE);
                        btnAdminToggle.setVisibility(View.VISIBLE);
                        btnAdminToggle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String familyCode = MyMethods.Cache.getString(itemView.getContext(), "familycode");
                                String selectedUser_role = familyMemberArrayList.get(getAbsoluteAdapterPosition()).getRole();
                                String selectedUser_uid = familyMemberArrayList.get(getAbsoluteAdapterPosition()).getUid();

                                if (selectedUser_role.equals("Member")){
                                    MaterialAlertDialogBuilder makeAdminAlert = new MaterialAlertDialogBuilder(itemView.getContext());
                                    makeAdminAlert.setTitle("Make this member an admin?");
                                    makeAdminAlert.setMessage("Admins are able to: \n\n" +
                                            "• Edit family details\n" +
                                            "• Manage missions\n" +
                                            "• Manage shop items and orders\n" +
                                            "• Assign new admins\n" +
                                            "• Remove admins");
                                    makeAdminAlert.setPositiveButton("Make admin", (dialogInterface, i) -> {
                                        toggleAdminStatus();
                                    });
                                    makeAdminAlert.setNeutralButton("Cancel", (dialogInterface, i) -> {
                                    });
                                    makeAdminAlert.show();
                                }
                                else if (selectedUser_role.equals("Admin")){
                                    MaterialAlertDialogBuilder removeAdminAlert = new MaterialAlertDialogBuilder(itemView.getContext());
                                    removeAdminAlert.setTitle("Remove admin privileges?");
                                    removeAdminAlert.setPositiveButton("Remove admin", (dialogInterface, i) -> {
                                        toggleAdminStatus();
                                    });
                                    removeAdminAlert.setNeutralButton("Cancel", (dialogInterface, i) -> {
                                    });
                                    removeAdminAlert.show();
                                }
                            }

                            private void toggleAdminStatus() {
                                String familyCode = MyMethods.Cache.getString(itemView.getContext(), "familycode");
                                String selectedUser_role = familyMemberArrayList.get(getAbsoluteAdapterPosition()).getRole();
                                String selectedUser_uid = familyMemberArrayList.get(getAbsoluteAdapterPosition()).getUid();

                                if (selectedUser_role.equals("Member")){
                                    hearthDB.getReference("family_"+familyCode+"_members").child("user_"+selectedUser_uid).child("role").setValue("Admin");
                                }
                                else if (selectedUser_role.equals("Admin")){
                                    hearthDB.getReference("family_"+familyCode+"_members").child("user_"+selectedUser_uid).child("role").setValue("Member");
                                }
                                familyMemberArrayList.clear();
                            }
                        });

                        btnKick.setOnClickListener(view1 -> {
                            MaterialAlertDialogBuilder kickDialog = new MaterialAlertDialogBuilder(itemView.getContext());
                            kickDialog.setTitle("Kick user");
                            kickDialog.setMessage("Did you want to kick this user?\n" +
                                    "They will lose access to this family and have their xp and gold reset.");
                            kickDialog.setPositiveButton("Kick", (dialogInterface, i) -> {
                                String familyCode = MyMethods.Cache.getString(itemView.getContext(), "familycode");
                                hearthDB.getReference("family_"+familyCode+"_members").child("user_"+familyMemberUid).removeValue();
                                hearthDB.getReference("user_"+familyMemberUid).child("familycode").removeValue();
                                hearthDB.getReference("user_"+familyMemberUid+"_gold").setValue(0);
                                hearthDB.getReference("user_"+familyMemberUid+"_xp").setValue(0);
                                hearthDB.getReference("user_"+familyMemberUid+"_alarms").removeValue();
                                hearthDB.getReference("user_"+familyMemberUid+"_missions_completed").removeValue();
                            });
                            kickDialog.setNeutralButton("Cancel", (dialogInterface, i) -> {
                            });
                            kickDialog.show();
                        });
                    }
                    else{
                        btnArrow.setIconResource(R.drawable.ic_arrow_down);
                        btnAdminToggle.setVisibility(View.GONE);
                        btnKick.setVisibility(View.GONE);
                    }
                });
            }
        }

        @Override
        public void onClick(View view) {
            onFamilyMemberListener.onFamilyMemberClick(getAbsoluteAdapterPosition());
        }
    }

    public interface OnFamilyMemberListener{
        void onFamilyMemberClick(int position);
    }
}