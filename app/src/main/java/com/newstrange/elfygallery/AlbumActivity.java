package com.newstrange.elfygallery;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.DisplayMetrics;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class AlbumActivity extends AppCompatActivity {

    GridView galleryGridView;
    ArrayList<HashMap<String, String>> imageList = new ArrayList<HashMap<String, String>>();
    String album_name = "";
    LoadAlbumImages loadAlbumTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        Toolbar mTopToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);

        Intent intent = getIntent();
        album_name = intent.getStringExtra("name");
        setTitle(album_name);


        galleryGridView = (GridView) findViewById(R.id.galleryGridView);
        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels;
        Resources resources = getApplicationContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = iDisplayWidth / (metrics.densityDpi / 160f);

        if (dp < 360) {
            dp = (dp - 17) / 2;
            float px = Function.convertDpToPixel(dp, getApplicationContext());
            galleryGridView.setColumnWidth(Math.round(px));
        }

        loadAlbumTask = new LoadAlbumImages();
        loadAlbumTask.execute();

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
            Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
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

    class LoadAlbumImages extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            String albumPath = null;
            String albumName = null;
            String timestamp = null;
            Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};

            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "bucket_display_name = \"" + album_name + "\"", null, null);
            Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "bucket_display_name = \"" + album_name + "\"", null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});
            while (cursor.moveToNext()) {

                albumPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

                imageList.add(Function.mappingInbox(albumName, albumPath, timestamp, Function.convertToTime(timestamp), null));
            }
            cursor.close();
            Collections.sort(imageList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            SingleAlbumAdapter adapter = new SingleAlbumAdapter(AlbumActivity.this, imageList);
            galleryGridView.setAdapter(adapter);
            galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {
                    Intent intent = new Intent(AlbumActivity.this, GalleryPreview.class);
                    intent.putExtra("path", imageList.get(+position).get(Function.KEY_PATH));
                    startActivity(intent);
                }
            });
        }
    }
}

