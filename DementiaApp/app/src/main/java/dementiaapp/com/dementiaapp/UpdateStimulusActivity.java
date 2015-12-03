package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class UpdateStimulusActivity extends Activity {

    private File stimulusPathGlobal;
    private String newStimulusFolderPath;
    private ImageView stimulusImage;
    final Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_stimulus);

        Intent myIntent = getIntent(); // gets the previously created intent
        final String path = myIntent.getStringExtra("path");
        final File stimulusPath = new File(path);
        stimulusPathGlobal = stimulusPath;

        Button questionButton = (Button) findViewById(R.id.changeQuestionButton);

        Button answerButton = (Button) findViewById(R.id.changeAnswerButton);
        final Button imageButton = (Button) findViewById(R.id.changePictureButton);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(),
                        "Position :" + path + "  ListItem : ", Toast.LENGTH_LONG)
                        .show();

                Intent intent = new Intent();
                intent.setClass(UpdateStimulusActivity.this, UploadPhotoWithNameActivity.class);
                Bundle bundle = new Bundle();
//                bundle.putString("newStimulusFolder", newStimulusFolderPath);
//                intent.putExtras(bundle);
                startActivity(intent);

                String stimuliMainDirPath = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/stimuli/";

                imageButton.setEnabled(false);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder;
                        builder = new AlertDialog.Builder(context);

                        builder.setTitle("Confirm");
                        builder.setMessage("Do you wish to see the image?");

                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing but close the dialog

                                dialog.dismiss();
                                displayPic();
                            }

                        });

                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }, 1000);

            }
        });

    }
    private void displayPic() {
//        Bitmap stimulusBitmap;
//
//        if (newStimulusFolderPath.endsWith("/"))
//            stimulusBitmap = BitmapFactory.decodeFile(newStimulusFolderPath + "photo.jpg");
//        else
//            stimulusBitmap = BitmapFactory.decodeFile(newStimulusFolderPath + "/photo.jpg");
//
//        stimulusImage.setImageBitmap(stimulusBitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update_stimulus, menu);
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
