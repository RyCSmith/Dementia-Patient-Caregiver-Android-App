package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.File;

public class ViewBrowserImage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_browser_image);

        Intent myIntent = getIntent(); // gets the previously created intent
        String imagePath = myIntent.getStringExtra("path");
        File imgFile = new File(imagePath);
                Bitmap stimulusBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView myImage = (ImageView) findViewById(R.id.browserImageView);

                    if (imgFile.exists()) {

                        myImage.setImageBitmap(stimulusBitmap);
                    }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_browser_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
