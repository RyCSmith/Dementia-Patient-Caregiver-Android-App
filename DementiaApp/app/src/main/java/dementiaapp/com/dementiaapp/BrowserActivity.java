package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BrowserActivity extends Activity {

    private List<newStimulus> stimulusList;
    private String stimuliMainDirPath;
    File[] realFolders;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        //get the listView object.
        listView = (ListView) findViewById(R.id.list);

        stimuliMainDirPath = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/stimuli/";

        File stimuliMainFolder = new File(stimuliMainDirPath);
        File[] allStimuli = stimuliMainFolder.listFiles();

        ArrayList<String> listOfStimuli = new ArrayList<String>();
        ArrayList<File> fileList = new ArrayList<File>();
        String stimulus = "Stimulus";
        int number = 1;
        int count = 0;

        if (allStimuli.length != 0) {

            for (File stimulusFolder : allStimuli) {
                if (stimulusFolder.isDirectory()) {
                    count++;
                    listOfStimuli.add(stimulus + " " + number);
                    fileList.add(stimulusFolder);
                    number++;
                }
            }
            String[] values = new String[listOfStimuli.size()];
            realFolders = new File[fileList.size()];
            for (int i = 0; i < realFolders.length; i++) {
                realFolders[i] = fileList.get(i);
            }

            for (int i = 0; i < values.length; i++) {
                values[i] = listOfStimuli.get(i);

            }
            // Define a new Adapter
            // First parameter - Context
            // Second parameter - Layout for the row
            // Third parameter - ID of the TextView to which the data is written
            // Forth - the Array of data

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, values);


            // Assign adapter to ListView
            listView.setAdapter(adapter);

            // ListView Item Click Listener
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    // ListView Clicked item index
                    int itemPosition = position;

                    // ListView Clicked item value
                    String itemValue = (String) listView.getItemAtPosition(position);

                    // Show Alert
                    Toast.makeText(getApplicationContext(),
                            "Position :" + realFolders[itemPosition].getPath() + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                            .show();
                    // Bring to that particular folder
//                    realFolders[itemPosition];// THIS IS THE PATH TO THE CLICKED FOLDER.
                    Intent intent = new Intent();
                    intent.putExtra("path", realFolders[itemPosition].getPath().toString());
                    intent.setClass(BrowserActivity.this, SecondBrowser.class);
                    startActivity(intent);


                }

            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_browser, menu);
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
