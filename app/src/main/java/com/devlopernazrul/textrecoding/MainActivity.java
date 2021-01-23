package com.devlopernazrul.textrecoding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {
    EditText mResult;
    ImageView mPreview;


private static final int CAMERA_REQUEST_CODE=200;
private static final int STORAGE_REQUEST_CODE=400;
private static final int IMAGE_PIC_GALLERY_CODE=1000;
private static final int IMAGE_PIC_CAMERA_CODE=1001;
String cameraPermission[];
String storagePermission[];
Uri image_uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setSubtitle("click Button OR add to insert Image");
       // https://www.youtube.com/watch?v=mmuz8qIWcL8
        mResult=(EditText)findViewById(R.id.result_edittext_id);
        mPreview=(ImageView)findViewById(R.id.image_v_id);
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
getMenuInflater().inflate(R.menu.manu_main,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       int id =item.getItemId();
       if (id == R.id.add_image)
       {
ShowImageImportDilog();
       }if (id == R.id.setting){

        }
return true;
    }

    private void ShowImageImportDilog() {
        String[] item={"Camera","Gallery"};
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("Select Image");
        dialog.setItems(item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which==0){
                    //camera option click
                    if (!checkCameraPermission())
                    {
                        requestCameraPermission();
                    }else {
                        picCamera();
                    }
                }if (which==1)
                {
                    //Gallery option click
                    if (!checkStoragePermission())
                    {
                        requestStoragePermission();
                    }else {
                        picGallery();
                    }
                }
            }
        });
        dialog.create().show();
    }

    private void picGallery() {

        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
       startActivityForResult(intent,IMAGE_PIC_GALLERY_CODE);

    }

    private void picCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image to text");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PIC_CAMERA_CODE);

    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result3= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result3;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {

        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
             return result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length>0){
                    boolean cameraAccept=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean     writeStorgeAccept=grantResults[0]==PackageManager.PERMISSION_GRANTED;

                    if (cameraAccept && writeStorgeAccept){
                        picCamera();
                    }else {
                        Toast.makeText(getApplicationContext(),"permissition deniy",Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length>0){

                    boolean     writeStorgeAccept=grantResults[0]==PackageManager.PERMISSION_GRANTED;

                    if ( writeStorgeAccept){
                        picGallery();
                    }else {
                        Toast.makeText(getApplicationContext(),"permissition deniy",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK )
        {
            if (requestCode==IMAGE_PIC_GALLERY_CODE)
            {
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);


            }if (requestCode==IMAGE_PIC_CAMERA_CODE)
        {
            CropImage.activity(image_uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);

        }
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK)
            {

                Uri resultUri=result.getUri();
                mPreview.setImageURI(resultUri);
                BitmapDrawable bitmapDrawable= (BitmapDrawable) mPreview.getDrawable();
                Bitmap bitmap=bitmapDrawable.getBitmap();
                TextRecognizer recognizer = new TextRecognizer.Builder(this).build();
                if (!recognizer.isOperational()){
                    Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
                }else {
                    Frame frame=new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock>item=recognizer.detect(frame);
                    StringBuilder sb=new StringBuilder();
                    for (int i=0;i<item.size();i++){
                        TextBlock myitem=item.valueAt(i);
                        sb.append(myitem.getValue());
                        sb.append("\n");

                    }
                    mResult.setText(sb.toString());
                }

            }else if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error=result.getError();
                Toast.makeText(getApplicationContext(),"Error"+error,Toast.LENGTH_LONG).show();
            }
        }
    }
}
