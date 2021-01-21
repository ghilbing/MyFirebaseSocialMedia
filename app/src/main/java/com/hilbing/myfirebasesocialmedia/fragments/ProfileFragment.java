package com.hilbing.myfirebasesocialmedia.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hilbing.myfirebasesocialmedia.MainActivity;
import com.hilbing.myfirebasesocialmedia.R;
import com.hilbing.myfirebasesocialmedia.activities.AddPostActivity;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.security.Key;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.firebase.database.FirebaseDatabase.getInstance;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    //Firebase
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseDatabase mDatabase;
    DatabaseReference mDatabaseReference;
    //Storage
    StorageReference mStorageReference;
    //path where images of user profile and cover will be stored
    String storagePath = "Users_Profile_Cover_Imgs/";

    //views
    @BindView(R.id.avatar_profile_iv)
    ImageView mAvatar;
    @BindView(R.id.cover_profile_iv)
    ImageView mCover;
    @BindView(R.id.name_profile_tv)
    TextView mName;
    @BindView(R.id.email_profile_tv)
    TextView mEmail;
    @BindView(R.id.phone_profile_tv)
    TextView mPhone;
    @BindView(R.id.fab)
    FloatingActionButton mFab;

    ProgressDialog mPd;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    //uri of picked image
    Uri image_uri;

    //for checking profile or cover photo
    String profileOrCoverPhoto;

    Context context;



    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init Firebase
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = getInstance();
        mDatabaseReference = mDatabase.getReference("Users");
        mStorageReference = FirebaseStorage.getInstance().getReference();

        //init views
        ButterKnife.bind(this, view);

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init ProgressDialog
        mPd = new ProgressDialog(getActivity());

        //search in database
        Query query =  mDatabaseReference.orderByChild("email").equalTo(mUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for(DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    //set data
                    mName.setText(name);
                    mEmail.setText(email);
                    mPhone.setText(phone);
                    try{
                        //if image is received then set
                        Glide.with(context).load(image).centerCrop().placeholder(R.drawable.ic_default_image).into(mAvatar);
                       // Picasso.get().load(image).fit().centerInside().placeholder(R.drawable.ic_default_image).into(mAvatar);
                    } catch (Exception e){
                        //if there is any exception while getting image, then set default
                        Glide.with(context).load(R.drawable.ic_default_image).into(mAvatar);
                       // Picasso.get().load(R.drawable.ic_default_image).into(mAvatar);
                    }

                    try{
                        //if image is received then set
                        Glide.with(getContext().getApplicationContext()).load(cover).placeholder(R.drawable.ic_default_cover).into(mCover);
                       // Picasso.get().load(cover).fit().centerInside().placeholder(R.drawable.ic_default_image).into(mCover);
                    } catch (Exception e){
                        //if there is any exception while gettin image, then set default
                       // Picasso.get().load(R.drawable.ic_default_cover).into(mCover);
                 //       Glide.with(getContext().getApplicationContext()).load(R.drawable.ic_default_cover).into(mCover);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //fab click
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });

        return view;
    }

    private boolean checkStoragePermission(){
        //check if storage permission is enabled or not
        //return true or false accordingly
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        //check if camera permission is enabled or not
        //return true or false accordingly
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //This method called when user press Allow or Deny from permission request dialog
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                //picking from camera
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        //permissions enabled
                        pickFromCamera();
                    }
                    else {
                        //permissions denied
                        Toast.makeText(getActivity(), getString(R.string.please_enable_camera_and_storage_permissions), Toast.LENGTH_LONG).show();

                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                //picking from Gallery
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //permissions enabled
                        pickFromGallery();
                    }
                    else {
                        //permissions denied
                        Toast.makeText(getActivity(), getString(R.string.please_enable_storage_permissions), Toast.LENGTH_LONG).show();

                    }
                }
            }
            break;
        }

    }

    private void pickFromCamera() {
        //intent of picking image from device camera
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, getString(R.string.temp_pic));
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.temp_description));
        // put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery(){
        //pick from Gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void uploadProfileCoverPhoto(Uri uri) {
        //path and name of image to be stored in firebase storage
        //show progress
        mPd.show();
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + mUser.getUid();
        StorageReference storageReference2nd = mStorageReference.child(filePathAndName);
        storageReference2nd.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image is uploaded to storage, new get it's url and store in user's database
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();
                // check if image is uploaded or not and uri is received
                if(uriTask.isSuccessful()){
                    //image uploaded
                    //add/update url in user's database
                    HashMap<String, Object> results = new HashMap<>();
                    results.put(profileOrCoverPhoto, downloadUri.toString());
                    mDatabaseReference.child(mUser.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mPd.dismiss();
                            Toast.makeText(getActivity(), getString(R.string.image_updated), Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mPd.dismiss();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    //error
                    mPd.dismiss();
                    Toast.makeText(getActivity(), getString(R.string.some_error_occurred), Toast.LENGTH_LONG).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //get error and dismiss progress dialog
                mPd.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void showEditProfileDialog() {
        //Show options in dialog
        String options[] = {getString(R.string.edit_profile_picture), getString(R.string.edit_cover_photo), getString(R.string.edit_name), getString(R.string.edit_phone)};
        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.choose_action));
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //handle clicks
                switch (i){
                    case 0:
                        //Edit profile picture
                        mPd.setMessage(getString(R.string.updating_profile_picture));
                        profileOrCoverPhoto = getString(R.string.image);
                        showImagePictureDialog();
                        break;
                    case 1:
                        //Edit cover photo
                        mPd.setMessage(getString(R.string.updating_cover_photo));
                        profileOrCoverPhoto = getString(R.string.cover);
                        showImagePictureDialog();
                        break;
                    case 2:
                        //Edit name
                        mPd.setMessage(getString(R.string.updating_name));
                        showNamePhoneUpdateDialog(getString(R.string.name));

                        break;
                    case 3:
                        //Edit phone
                        mPd.setMessage(getString(R.string.updating_phone));
                        showNamePhoneUpdateDialog(getString(R.string.phone));

                        break;

                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(String data) {
        //custom Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.update) + " " + data);
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        EditText editText = new EditText(getActivity());
        editText.setHint(getString(R.string.enter) + " " + data);
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        //add buttons
        builder.setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                if(!TextUtils.isEmpty(value)){
                    mPd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(data, value);
                    mDatabaseReference.child(mUser.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mPd.dismiss();
                            Toast.makeText(getActivity(), getString(R.string.name_updated), Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mPd.dismiss();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), getString(R.string.please_enter) + " " + data, Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        //create and show dialog
        builder.create().show();
    }

    private void showImagePictureDialog() {
        //Show dialog containing options Camera and Gallery to pick the image
        String options[] = {getString(R.string.camera), getString(R.string.gallery)};
        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.pick_image_from));
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //handle clicks
                switch (i){
                    case 0:
                        //Camera
                        if(!checkCameraPermission()){
                            requestCameraPermission();
                        } else {
                            pickFromCamera();
                        }
                        break;
                    case 1:
                        //Gallery
                        if(!checkStoragePermission()){
                            requestStoragePermission();
                        } else {
                            pickFromGallery();
                        }


                        break;

                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if(id == R.id.action_logout){
            mAuth.signOut();
            checkUserStatus();
        }
        if(id == R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            //user is signed in stay here

        } else {
            //user not signed in, go to MainActivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }


}