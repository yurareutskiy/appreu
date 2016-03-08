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
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import styleru.it_lab.reaschedule.CustomFontViews.AutoCompleteTextViewCustomFont;
import styleru.it_lab.reaschedule.Operations.MemoryOperations;
import styleru.it_lab.reaschedule.Operations.NetworkOperations;
import styleru.it_lab.reaschedule.Schedule.Week;

public class SearchActivity extends AppCompatActivity {

    public static final String DEBUG_TAG = "SearchAct_DEBUG";
    ActionBar actionBar;
    Dialog dialog;
    AutoCompleteTextViewCustomFont searchTxt;
    Map<Integer, String> groups = new HashMap<>();
    Map<Integer, String> lectors = new HashMap<>();
    Map<Integer, String> members = new HashMap<>();
    String missing = "";
    String DBTable = "";
    String searchWho = "";
    int searchID = 0;
    SparseArray<Week> weeks = new SparseArray<Week>();

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

        groups = MemoryOperations.DBMembersGet(this, MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_GROUPS);
        lectors = MemoryOperations.DBMembersGet(this, MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_LECTORS);

        if (groups.size() == 0)
        {
            missing = "groups";
            DBTable = MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_GROUPS;
        }
        else if (lectors.size() == 0)
        {
            missing = "lectors";
            DBTable = MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_LECTORS;
        }

        if (!missing.equals(""))
        {
            if (NetworkOperations.isConnectionAvailable(this))
            {
                String stringUrl = getString(R.string.API_get_url) + missing + "/";
                new NetworkOperations.RequestTask(response, "members").execute(stringUrl);
            }
            else
            {
                Toast.makeText(this, "Невозможно установить интернет-соединение!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            fillListWithData();
        }

    }

    NetworkOperations.RequestTask.AsyncResponse response = new NetworkOperations.RequestTask.AsyncResponse() {
        @Override
        public void processFinish(Object result, String response) {
            //запускается, когда запрос вернул ответ.
            if (result != null)
                members = (HashMap<Integer, String>) result;

            if (members.isEmpty())
            {
                Log.i(DEBUG_TAG, "Пришел пустой результат!");
                if (result == null)
                    Toast.makeText(getApplicationContext(), "Невозможно установить интернет-соединение.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Неверный ответ сервера. Попробуйте позже.", Toast.LENGTH_SHORT).show();

                dialog.cancel();
            }
            else
            {
                Log.i(DEBUG_TAG, "Пришли результаты. Размер: " + members.size());
                MemoryOperations.DBMembersSet(getApplicationContext(), members, DBTable);
                fillListWithData();
            }
        }
    };

    private void fillListWithData()
    {
        if (missing.equals("groups"))
        {
            groups = members;
        }
        else if (missing.equals("lectors"))
        {
            lectors = members;
            members = groups;
        }
        else
        {
            members = groups;
        }

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

    public void onSearchStartPress(View v)
    {
        String searchText = searchTxt.getText().toString();
        if (!members.containsValue(searchText))
        {
            Toast.makeText(this, "Введите существующее значение!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (groups.containsValue(searchText))
        {
            searchWho = "group";
            for (Map.Entry<Integer, String> e : groups.entrySet())
            {
                int key = e.getKey();
                String value = e.getValue();
                if (value.equals(searchText))
                {
                    searchID = key;
                    break;
                }
            }
        }
        else
        {
            searchWho = "lector";
            for (Map.Entry<Integer, String> e : lectors.entrySet())
            {
                int key = e.getKey();
                String value = e.getValue();
                if (value.equals(searchText))
                {
                    searchID = key;
                    break;
                }
            }
        }

        if (NetworkOperations.isConnectionAvailable(this))
        {
            String stringUrl = getString(R.string.API_url) + "lessons/?who=" +  searchWho + "&id=" + searchID + "&timestamp=0";
            new NetworkOperations.RequestTask(response, "schedule").execute(stringUrl);
        }
        else
        {
            Toast.makeText(this, "Невозможно установить интернет-соединение!", Toast.LENGTH_SHORT).show();
        }
    }

//    NetworkOperations.RequestTask.AsyncResponse scheduleResponse = new NetworkOperations.RequestTask.AsyncResponse() {
//        @Override
//        public void processFinish(Object result, String response) {
//            if (result != null)
//                weeks = (SparseArray<Week>) result;
//
//            dialog.cancel();
//            if (weeks.size() == 0)
//            {
//                Log.i(DEBUG_TAG, "Пришел пустой результат!");
//                if (result == null)
//                    Toast.makeText(getApplicationContext(), "Невозможно установить интернет-соединение.", Toast.LENGTH_SHORT).show();
//                else
//                    Toast.makeText(getApplicationContext(), "Неверный ответ сервера. Попробуйте позже.", Toast.LENGTH_SHORT).show();
//
//                fillScheduleWithEmpty();
//            }
//            else
//            {
//                weekCount = weeks.size();
//                MemoryOperations.cacheSchedule(getApplicationContext(), response, memberWho, memberID);
//
//                fillActionBarWithData();
//                fillScheduleWithData();
//            }
//        }
//    };

}
