package com.example.mplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mplayer.Model.UploadSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    TextView textViewImage;
    Uri audioUri;
    ProgressBar progressBar;
    StorageReference Storageref;
    StorageTask Uploadstask;
    DatabaseReference referencesongs;
    String songcategory;
    MediaMetadataRetriever metadataRetriever;
    byte [] art;
    String Title,Artist,Album="",Duration;
    TextView title,artist,duration,album,dataa;
    ImageView Album1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewImage = findViewById(R.id.textviewsongselected);
        progressBar = findViewById(R.id.progressbar);
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artish);
        duration = findViewById(R.id.duration);
        dataa = findViewById(R.id.dataa);
        album = findViewById(R.id.album);
        Album1 = findViewById(R.id.imageview);

        metadataRetriever = new MediaMetadataRetriever();
        referencesongs = FirebaseDatabase.getInstance().getReference().child("song");
        Storageref = FirebaseStorage.getInstance().getReference().child("song");

        Spinner spinner = findViewById(R.id.spinner);

        spinner.setOnItemSelectedListener(this);

        List<String> categories = new ArrayList<>();

        categories.add("Birthday Songs");
        categories.add("Party Songs");
        categories.add("Sad Songs");
        categories.add("God Songs");
        categories.add("Love Songs");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

    }

    /**
     * <p>Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.</p>
     * <p>
     * Implementers can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        songcategory = parent.getItemAtPosition(position).toString();
        Toast.makeText(this,"Selected: "+songcategory,Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    public void openaudiofile (View v)
    {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("autio/*");
        startActivityForResult(i,101);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK && data.getData() != null)
        {
            audioUri = data.getData();
            String filename = getfilename(audioUri);
            textViewImage.setText(filename);
            metadataRetriever.setDataSource(this,audioUri);

            art = metadataRetriever.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
            Album1.setImageBitmap(bitmap);
            album.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            artist.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            dataa.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
            duration.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            title.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));

            Artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            Title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            Duration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        }
    }
    private String getfilename(Uri uri)
    {
        String result = null;
        if(uri.getScheme().equals("content"))
        {
            Cursor cursor = getContentResolver().query(uri,null,null,null,null);
            try {
                if (cursor != null && cursor.moveToFirst())
                {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            finally {
                cursor.close();
            }
        }
        if(result == null)
        {
            result = uri.getPath();
            int out = result.lastIndexOf('/');
            if(out != -1)
            {
                result=result.substring(out +1);
            }
        }
        return result;
    }

    public void uploadfiletobase (View v)
    {
        if(textViewImage.equals("No file Selected"))
        {
            Toast.makeText(this,"Please selected an image",Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(Uploadstask != null && Uploadstask.isInProgress())
            {
                Toast.makeText(this,"Song Upload in allready progress",Toast.LENGTH_SHORT).show();
            }
            else {
                UploadFile();
            }
        }
    }

    private void UploadFile()
    {
        if(audioUri != null)
        {
            Toast.makeText(this,"Uploading Please Wait...",Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);
            final StorageReference storageReference = Storageref.child(System.currentTimeMillis()+"."+getfileextension(audioUri));
            Uploadstask = storageReference.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            UploadSource uploadSource = new UploadSource(songcategory,Title,Artist,Album,Duration,uri.toString());
                            String uploadId = referencesongs.push().getKey();
                            referencesongs.child(uploadId).setValue(uploadSource);
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progess = (100.0 * snapshot.getBytesTransferred() /snapshot.getTotalByteCount());
                    progressBar.setProgress((int) progess);
                }
            });
        }
        else
        {
            Toast.makeText(this,"No file Selected to Upload",Toast.LENGTH_SHORT).show();

        }
    }

    private String getfileextension(Uri audioUri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(audioUri));
    }
    public void  openAlbumUploadsActivity(View v)
    {
        Intent in =new Intent(MainActivity.this,online_music_player.class);
        startActivity(in);
    }


}