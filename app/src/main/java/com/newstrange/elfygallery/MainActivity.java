package com.newstrange.elfygallery;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import androidx.appcompat.widget.Toolbar;

import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    LoadAlbum loadAlbumTask;
    GridView galleryGridView;
    ArrayList<HashMap<String, String>> albumList = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mTopToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);
        //FirebaseApp.InitializeApp(Application.Context);
        //FirebaseApp.initializeApp(this);
        // gallery view set column width
        galleryGridView = findViewById(R.id.galleryGridView);
        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels;
        Resources resources = getApplicationContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = iDisplayWidth / (metrics.densityDpi / 160f);
        if (dp < 360) {
            dp = (dp - 17) / 2;
            float px = Function.convertDpToPixel(dp, getApplicationContext());
            galleryGridView.setColumnWidth(Math.round(px));
        }

        getPermissionAndStartGallery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.open_camera) {
            Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
//        else if (id == R.id.settings) {
////            Intent intent = new Intent(getApplicationContext(), ShowSettings.class);
////            startActivity(intent);
////            finish();
//        } else if (id == android.R.id.home) {
//            finish();
//        }
        return super.onOptionsItemSelected(item);
    }

    class LoadAlbum extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            albumList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            String path = null;
            String album = null;
            String timestamp = null;
            String countPhoto = null;
            Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.MediaColumns.DATE_MODIFIED};

            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                    null, null);
            Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                    null, null);

            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});

            while (cursor.moveToNext()) {
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                countPhoto = Function.getCount(getApplicationContext(), album);

                albumList.add(Function.mappingInbox(album, path, timestamp, Function.convertToTime(timestamp), countPhoto));
            }

            cursor.close();
            Collections.sort(albumList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            AlbumAdapter adapter = new AlbumAdapter(MainActivity.this, albumList);
            galleryGridView.setAdapter(adapter);
            galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
                    intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                    startActivity(intent);
                }
            });
        }
    }

    public void getPermissionAndStartGallery() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                loadAlbumTask = new LoadAlbum();
                loadAlbumTask.execute();
            }

            //            @Override
//            public void onPermissionDenied(PermissionDeniedResponse response) {
//                // check for permanent denial of permission
//                if (response.isPermanentlyDenied()) {
//                    // navigate user to app settings
//                }
//            }
            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                Toast.makeText(MainActivity.this, "You must accept permissions to continue.", Toast.LENGTH_LONG).show();
                token.continuePermissionRequest();
            }
        }).check();
    }
}