package com.wingscorp.basreport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateName, editTextUpdateDoB, editTextUpdatePhone, editTextUpdateAccessCode;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textFullName, textDoB, textGender, textPhone, textAccessCode;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;
    private DatePickerDialog datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Ubah Profil");
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBar);
        editTextUpdateName = findViewById(R.id.editText_update_profile_name);
        editTextUpdateDoB = findViewById(R.id.editText_update_profile_dob);
        editTextUpdatePhone = findViewById(R.id.editText_update_profile_mobile);
        editTextUpdateAccessCode = findViewById(R.id.editText_update_profile_access_code);

        radioGroupUpdateGender = findViewById(R.id.radio_group_update_profile_gender);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        showProfile(firebaseUser);

        Button buttonUpdateProfile = findViewById(R.id.button_update_profile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile(firebaseUser);
            }
        });

        editTextUpdateDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textSADoB[] = textDoB.split("/");

                //final Calendar calendar = Calendar.getInstance();
                int day = Integer.parseInt(textSADoB[0]);
                int month =Integer.parseInt(textSADoB[1]) - 1;
                int year = Integer.parseInt(textSADoB[2]);

                datePicker = new DatePickerDialog(UpdateProfileActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                                editTextUpdateDoB.setText(dayOfMonth + "/" +(month + 1) + "/" + year);
                            }
                        }, year, month, day);
                datePicker.show();
            }
        });

    }

    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonUpdateGenderSelected = findViewById(selectedGenderID);

        String phoneRegex = "[6-9][0-9]{9}";
        Matcher phoneMatcher;
        Pattern phonePattern = Pattern.compile(phoneRegex);
        phoneMatcher = phonePattern.matcher(textPhone);

        if (TextUtils.isEmpty(textFullName)){
            editTextUpdateName.setError("Nama lengkap tidak boleh kosong");
            editTextUpdateName.requestFocus();
        } else if (TextUtils.isEmpty(textDoB)){
            editTextUpdateDoB.setError("Tanggal lahir tidak boleh kosong");
            editTextUpdateDoB.requestFocus();
        } else if (TextUtils.isEmpty(textPhone)){
            editTextUpdatePhone.setError("Nomor telepon tidak boleh kosong");
            editTextUpdatePhone.requestFocus();
        } else if (textPhone.length() <= 10){
            editTextUpdatePhone.setError("Masukkan nomor telepon dengan benar");
            editTextUpdatePhone.requestFocus();
        } else if (!phoneMatcher.find()){
            editTextUpdatePhone.setError("Masukkan nomor telepon dengan benar");
            editTextUpdatePhone.requestFocus();
        } else if (TextUtils.isEmpty(textAccessCode)){
            editTextUpdateAccessCode.setError("Kode akses tidak boleh kosong");
            editTextUpdateAccessCode.requestFocus();
        } else{
            textGender = radioButtonUpdateGenderSelected.getText().toString();
            textFullName = editTextUpdateName.getText().toString();
            textDoB = editTextUpdateDoB.getText().toString();
            textPhone = editTextUpdatePhone.getText().toString();
            textAccessCode = editTextUpdateAccessCode.getText().toString();

            UserDetails userDetails = new UserDetails(textDoB, textGender, textPhone, textAccessCode);

            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("RegisteredUser");

            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);

            referenceProfile.child(userID).setValue(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                        firebaseUser.updateProfile(profileUpdates);

                        Toast.makeText(UpdateProfileActivity.this, "Pembaruan data berhasil!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e){
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void showProfile(FirebaseUser firebaseUser) {
        String userIDofRegistered = firebaseUser.getUid();
        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference referenceProfile = FirebaseDatabase.getInstance("https://bas-delivery-report-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("RegisteredUser");
        referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserDetails userDetails = snapshot.getValue(UserDetails.class);

                if (userDetails != null){
                    textFullName = firebaseUser.getDisplayName();
                    textDoB = userDetails.doB;
                    textGender = userDetails.gender;
                    textPhone = userDetails.phone;
                    textAccessCode = userDetails.accessCode;

                    editTextUpdateName.setText(textFullName);
                    editTextUpdateDoB.setText(textDoB);
                    editTextUpdatePhone.setText(textPhone);
                    editTextUpdateAccessCode.setText(textAccessCode);

                    if (textGender.equals("Laki-Laki")){
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_male);
                    } else {
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_female);
                    }
                    radioButtonUpdateGenderSelected.setChecked(true);

                } else {
                    Toast.makeText(UpdateProfileActivity.this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}