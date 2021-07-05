package com.example.mplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mplayer.Model.Constants;
import com.example.mplayer.Model.Upload;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class online_music_player extends AppCompatActivity implements View.OnClickListener {
    private Button buttonChoose;
    private Button buttonUploade;
    private EditText edittextName;
    private ImageView imageview;
    String songCategory;
    private static final int PICK_IMAGE_REQUEST = 234;

    private Uri fileFath;
    StorageReference storageReference;
    DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_music_player);

        buttonChoose = findViewById(R.id.buttonChoose);
        buttonUploade = findViewById(R.id.buttonupload);
        edittextName = findViewById(R.id.edit_text);
        imageview = findViewById(R.id.imageview);

        storageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOAD);

        Spinner spinner = findViewById(R.id.spinner);

        buttonChoose.setOnClickListener(this);
        buttonUploade.setOnClickListener(this);

        List<String> categories = new ArrayList<>();

        categories.add("Birthday Songs");
        categories.add("Party Songs");
        categories.add("Sad Songs");
        categories.add("God Songs");
        categories.add("Love Songs");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                songCategory = parent.getItemAtPosition(position).toString();
                Toast.makeText(online_music_player.this,"Selected: "+songCategory,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /**
     * Called when a view has been clicked.
     */
    @Override
    public void onClick(View view) {
        if(view ==buttonChoose)
        {
            showFileChoose();
        }
        else if(view == buttonUploade)
        {
            uploadFile();
        }
    }

    private void uploadFile() {

        if(fileFath != null)
        {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("uploading");
            progressDialog.show();
            final StorageReference sRef = storageReference.child(Constants.STORAGE_PATH_UPLOADS
                    +System.currentTimeMillis()+"."+getFileExtension(fileFath));
            sRef.putFile(fileFath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String url =uri.toString();
                            Upload upload = new Upload(edittextName.getText().toString().trim(),
                                    url,songCategory);
                            String uploadID = mDatabase.push().getKey();
                            mDatabase.child(uploadID).setValue(upload);
                            progressDialog.dismiss();
                            Toast.makeText(online_music_player.this,"File Upload",Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(online_music_player.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded "+((int)progress)+"%...");
                }
            });
        }
    }

    private void showFileChoose() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode ==RESULT_OK && data != null && data.getData() !=null)
        {
            fileFath = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileFath);
                imageview.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }



        }
    }
    public  String getFileExtension (Uri uri)
    {
        ContentResolver c = getContentResolver();
        MimeTypeMap mine =MimeTypeMap.getSingleton();
        return mine.getMimeTypeFromExtension(c.getType(uri));
    }
}