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
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import java.io.*;
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
        String stimulus;
        int number = 1;
        int count = 0;
        int numCorrect = 0;
        int numAsked = 0;

        if (allStimuli.length != 0) {

            for (File stimulusFolder : allStimuli) {
                if (stimulusFolder.isDirectory()) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(new File(stimulusFolder + "/name.txt")));
                        stimulus = br.readLine();
                        br.close();
                    } catch (IOException e) {
                        stimulus = "Stimulus " + number;
                    }
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(new File(stimulusFolder + "/metrics.txt")));
                        numCorrect = Integer.parseInt(br.readLine());
                        numAsked = Integer.parseInt(br.readLine());
                        br.close();
                    } catch (IOException e) {}
                    count++;
                    listOfStimuli.add(stimulus + " " + numCorrect + "/" + numAsked);
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
                    android.R.layout.simple_list_item_1, android.R.id.text1, values){

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view =super.getView(position, convertView, parent);

                    TextView textView=(TextView) view.findViewById(android.R.id.text1);

                    textView.setTextColor(Color.parseColor("#E7BB66"));
                    textView.setTypeface(null, Typeface.BOLD);

                    return view;
                }
            };

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
