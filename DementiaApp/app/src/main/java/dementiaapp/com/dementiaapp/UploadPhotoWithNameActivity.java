package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;


public class UploadPhotoWithNameActivity extends Activity {

    private Button recordNameButton;

    private static final int REQUEST_CODE_SPEECH_RECOGNITION = 100;
    private static final int REQUEST_CODE_PHOTO_SELECT = 101;
    private static final int REQUEST_CODE_TAKE_NEW_PHOTO = 102;

    private ArrayList<String> stimulusPossibilities;

    private String absolutePathOfSelectedPhoto = new String();
    private Bitmap photo = null;

    private String tempNewPhotoFilename;
    private Uri newPhotoUri;
    private boolean isTakingNewPhoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo_with_name);

        Button pickPhotoButton = (Button) findViewById(R.id.pick_photo_from_phone_button);
        Button takeNewPhotoButton = (Button) findViewById(R.id.take_new_photo_button);

        recordNameButton = (Button) findViewById(R.id.record_name_button);

        recordNameButton.setEnabled(false);

        pickPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_PHOTO_SELECT);
            }
        });

        takeNewPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    String externalPath = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath();

                    File file = new File(externalPath + "/MemAid/photo");
                    file.mkdirs();
                    tempNewPhotoFilename = UUID.randomUUID().toString();
                    absolutePathOfSelectedPhoto = file.getCanonicalPath() + "/" +tempNewPhotoFilename + ".jpg";
                    File newFile = new File(absolutePathOfSelectedPhoto);
                    newPhotoUri = Uri.fromFile(newFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, newPhotoUri);
                    isTakingNewPhoto = true;
                    startActivityForResult(intent, REQUEST_CODE_TAKE_NEW_PHOTO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_RECOGNITION) {
            if (resultCode == RESULT_OK && data != null) {
                stimulusPossibilities = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                for(String recoginzedName: stimulusPossibilities) {
                    Log.d("VISHWA", "Stimulus recorded:" + recoginzedName);
                }
                Log.d("VISHWA", "================================================================");

                if (photo != null) {
                    saveBitmap(photo, absolutePathOfSelectedPhoto);
                }
            }
        } else if (requestCode == REQUEST_CODE_PHOTO_SELECT) {
            if (resultCode == RESULT_OK) {
                Uri photoUri = data.getData();
                handlePhotoCompletion(photoUri);
            } else {
                Toast.makeText(getApplicationContext(), "Sorry, that photo could not be used, please use a different photo.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_CODE_TAKE_NEW_PHOTO) {
            if (resultCode == RESULT_OK) {
                handlePhotoCompletion(newPhotoUri);
            }
        }
    }

    private void handlePhotoCompletion(Uri photoUri) {
        if (!isTakingNewPhoto) {
            absolutePathOfSelectedPhoto = MemAidUtils.getAbsolutePathFromUri(getApplicationContext(), photoUri);
        }

        photo = MemAidUtils.decodeSampledBitmapFromFilePath(absolutePathOfSelectedPhoto, 700, 700);
        Matrix matrix = new Matrix();
        if (!isTakingNewPhoto) {
            matrix = MemAidUtils.getRotationMatrixForImage(getApplicationContext(), photoUri);
        } else {
            matrix = MemAidUtils.getRotationMatrixForImage(absolutePathOfSelectedPhoto);
        }
        photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);

        recordNameButton.setEnabled(true);
        recordNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 20);
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_RECOGNITION);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "YOUR DEVICE DOES NOT SUPPORT SPEECH RECOGNITION", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void saveBitmap(Bitmap photo, String originalPath) {
        String externalPath = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath();

        File file = new File(externalPath + "/MemAid/photo");
        file.mkdirs();

        String nameOfOriginalImageFile = new String();
        if (originalPath.contains("/")) {
            nameOfOriginalImageFile = originalPath.substring(originalPath.lastIndexOf('/'));
        } else {
            nameOfOriginalImageFile = originalPath;
        }
        String hashOfFileName = MemAidUtils.getMD5Hash(nameOfOriginalImageFile);
        String filePath = externalPath + "/MemAid/photo/" + hashOfFileName;

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();

                    try {
                        JSONObject possibleAnswers = new JSONObject();
                        JSONArray possibilities = new JSONArray(stimulusPossibilities);
                        Log.d("Vishwa", "stimulusPossibilities length = "+stimulusPossibilities.size());
                        Log.d("Vishwa", "JSON possibilities = "+possibilities.join(","));

                        possibleAnswers.put(Constants.STIMULUS_RESPONSE_JSON_KEY, possibilities);

                        Stimulus stimulus = new Stimulus();
                        stimulus.id = MemAidUtils.getMD5Hash(nameOfOriginalImageFile);
                        stimulus.type = Stimulus.TYPE_IMAGE;
                        stimulus.questionFilepath = filePath;
                        stimulus.possibleAnswers = possibleAnswers.toString();
                        stimulus.createdAt = new Date().getTime();
                        stimulus.save();

                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (photo != null) {
            photo.recycle();
            photo = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload_photo_with_name, menu);
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
