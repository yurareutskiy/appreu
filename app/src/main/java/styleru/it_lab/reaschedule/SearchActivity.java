package styleru.it_lab.reaschedule;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import styleru.it_lab.reaschedule.CustomFontViews.AutoCompleteTextViewCustomFont;
import styleru.it_lab.reaschedule.Operations.MemoryOperations;

public class SearchActivity extends AppCompatActivity {

    public static final String DEBUG_TAG = "SearchAct_DEBUG";
    ActionBar actionBar;
    Dialog dialog;
    AutoCompleteTextViewCustomFont searchTxt;
    Map<Integer, String> groups;
    Map<Integer, String> lectors;
    Map<Integer, String> members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.i(DEBUG_TAG, "Created SearchActivity");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(myToolbar);
        setupActionBar();

        searchTxt = (AutoCompleteTextViewCustomFont) findViewById(R.id.searchTxt);
        if (searchTxt == null)
            Log.i(DEBUG_TAG, "SEARCH TXT NE NAYDEN!");

        getData();
    }

    private void getData()
    {
        dialog = ProgressDialog.show(this, "", "Загрузка...", true, false);

        Map<Integer, String> members = MemoryOperations.DBMembersGet(this, MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_GROUPS);
        groups = members;

        Map<Integer, String> lectors = MemoryOperations.DBMembersGet(this, MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_LECTORS);
        int key = members.size();
        for (Map.Entry<Integer, String> e : lectors.entrySet())
        {
            key++;
            String value = e.getValue();
            members.put(key, value);
        }

        List<String> membersValues = new ArrayList<String>(members.values());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.login_list_item, membersValues);
        searchTxt.setAdapter(adapter);

        dialog.cancel();
    }

    private void setupActionBar()
    {
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
        actionBar.setHomeAsUpIndicator(upArrow);

        RelativeLayout actionBarView = (RelativeLayout) getLayoutInflater().inflate(R.layout.search_actionbar, null);
        actionBar.setCustomView(actionBarView);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_search, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
