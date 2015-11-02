package uk.co.craiggarrigan.dvsacancellationfinder;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.List;

public class MainActivity extends ListActivity {

    public static final int SETUP_REQUEST_CODE = 1056311;
    private final CancellationService service = new CancellationService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!isSetupComplete()){
            startSetup();
        } else {
            updateList();
        }
    }

    private void startSetup() {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivityForResult(intent, SETUP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SETUP_REQUEST_CODE){
            updateList();
        }
    }

    private boolean isSetupComplete() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE);
        return prefs.getBoolean(getString(R.string.prefs_isSetupComplete), false);
    }

    private void updateList() {
        ArrayAdapter<String> loadingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new String[]{"Loading..."});
        setListAdapter(loadingAdapter);

        //AsyncTask<Void, Void, List<String>> task;

        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE);
        String licenseNumber = prefs.getString(getString(R.string.prefs_licenseNumber), "");
        String applicationRefNumber = prefs.getString(getString(R.string.prefs_applicationRefNumber), "");

        String[] values = service.getCancellations(licenseNumber, applicationRefNumber);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            updateList();
            return true;
        } else if (id == R.id.action_setup){
            startSetup();
            return true;
        } else if (id == R.id.action_reset){
            SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE);
            prefs.edit().clear().commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
