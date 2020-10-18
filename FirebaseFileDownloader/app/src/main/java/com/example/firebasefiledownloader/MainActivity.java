package com.example.firebasefiledownloader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebasefiledownloader.util.encryption;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_PICTURES;

public class MainActivity extends AppCompatActivity {

    //UI Components
    Button downloadbtn;
    Button showbtn;
    ImageView imageView;
    TextView textView;
    ProgressBar progressBar;

    //FirebaseStorage
    StorageReference mStorageReference;


    //Encryption Keys
    String my_key = "TjcRW97OoIEw6UPr";
    String my_spec_key = "r9K7z8Cbkw07iEvq";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        downloadbtn = findViewById(R.id.downloadbtn);
        showbtn = findViewById(R.id.showbtn);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.textView);


        progressBar.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);



        //Show File Function
        showbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);

                File localeFileDec = new File(getExternalFilesDir(DIRECTORY_DOWNLOADS), "naruto_dec.jpg");

                File localeFileEnc = new File(getExternalFilesDir(DIRECTORY_DOWNLOADS), "naruto.jpg");

                try {
                    encryption.decryptToFile(my_key, my_spec_key, new FileInputStream(localeFileEnc), new FileOutputStream(localeFileDec));


                    //
                    ((ImageView) findViewById(R.id.imageView)).setImageURI(Uri.fromFile(localeFileDec));
                    localeFileDec.delete();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                }

                progressBar.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
                downloadbtn.setVisibility(View.INVISIBLE);
                showbtn.setVisibility(View.INVISIBLE);

            }
        });



        //Download Image Button
        downloadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile();

                // The file is downloaded in this directory - MyFiles/InternalStorage/Android/data/com.example.firebasefiledownloader/files/Download

            }
        });



        //App Permissions
        Dexter.withContext(this)
                .withPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                        }).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }
        }).check();

    }


    //Download File Function
    public void downloadFile(){
        try {

            final File localeFile = new File(getExternalFilesDir(DIRECTORY_DOWNLOADS), "naruto.jpg");
            mStorageReference = FirebaseStorage.getInstance().getReference().child("images/naruto.jpg");


            progressBar.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);

            mStorageReference.getFile(localeFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(localeFile.getAbsolutePath());

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();

                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                            InputStream is = new ByteArrayInputStream(stream.toByteArray());




                            try {
                                encryption.encryptToFile(my_key, my_spec_key, is, new FileOutputStream(localeFile));


                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (InvalidKeyException e) {
                                e.printStackTrace();
                            } catch (InvalidAlgorithmParameterException e) {
                                e.printStackTrace();
                            } catch (NoSuchPaddingException e) {
                                e.printStackTrace();
                            }

                            progressBar.setProgress(0);
                            textView.setText("Download Complete");

                        }
                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {



                    int progress = Math.toIntExact((100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int) progress);
                    textView.setText(progress + "%");


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}


