package styleru.it_lab.reaschedule;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import styleru.it_lab.reaschedule.Adapters.SamplePageAdapter;
import styleru.it_lab.reaschedule.CustomFontViews.AutoCompleteTextViewCustomFont;
import styleru.it_lab.reaschedule.CustomFontViews.TextViewCustomFont;
import styleru.it_lab.reaschedule.Operations.MemoryOperations;
import styleru.it_lab.reaschedule.Operations.NetworkOperations;
import styleru.it_lab.reaschedule.Operations.ScheduleUIManager;
import styleru.it_lab.reaschedule.Schedule.Week;

@SuppressWarnings("unchecked")
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
    ScheduleUIManager scheduleManager;
    TextViewCustomFont actionBarWeek;
    SamplePageAdapter pagerAdapter;
    ViewPager viewPager;

    //TODO 3. Implement RefreshButton onClick Listener! Now it Causes Crash!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.i(DEBUG_TAG, "Created SearchActivity");

        scheduleManager = new ScheduleUIManager(this, DEBUG_TAG);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(myToolbar);
        setupActionBar();

        getActionBarWeek();
        scheduleManager.setTxtWeek(actionBarWeek);

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

        Log.i(DEBUG_TAG, groups.size() + " " + lectors.size());

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
        switch (missing) {
            case "groups":
                groups.putAll(members);
                break;
            case "lectors":
                lectors.putAll(members);
                members.putAll(groups);
                break;
            default:
                members.putAll(groups);
                break;
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
        dialog.dismiss();
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
            Log.i(DEBUG_TAG, "If groups ID = " + searchID );
        }
        else if (lectors.containsValue(searchText))
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
            Log.i(DEBUG_TAG, "Search lector: ID - " + searchID);
        }

        Log.i(DEBUG_TAG, "Attempt to get cached schedule");
        dialog = ProgressDialog.show(this, "", "Загрузка...", true, false);

        final Handler h = new Handler()
        {
            public void handleMessage(android.os.Message msg)
            {
                getDataForSchedulePost((SparseArray<Week>) msg.obj);
            }
        };

        Thread newT = new Thread(new Runnable() {
            @Override
            public void run() {
                SparseArray<Week> tmpWeeks = MemoryOperations.getCachedSchedule(getApplicationContext(), searchWho, searchID);
                Message msg = h.obtainMessage(1, tmpWeeks);
                h.sendMessage(msg);
            }
        });
        newT.start();
    }

    private void getDataForSchedulePost(SparseArray<Week> tmpWeeks)
    {
        if (tmpWeeks.size() != 0)
        {
            Log.i(DEBUG_TAG, "Loaded schedule from cache! Vot tak!");
            scheduleManager.setWeeks(tmpWeeks);
            fillScheduleWithData();
            dialog.cancel();
        }
        else if (NetworkOperations.isConnectionAvailable(this))
        {
            String stringUrl = getString(R.string.API_url) + "lessons/?who=" +  searchWho + "&id=" + searchID + "&timestamp=0";

            Log.i(DEBUG_TAG, "Все в поряде!");
            scheduleManager.setWeeks(new SparseArray<Week>());

            final NetworkOperations.RequestTask asyncTask = new NetworkOperations.RequestTask(scheduleResponse, "schedule");

            dialog.setCancelable(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    asyncTask.cancel(true);
                }
            });

            asyncTask.execute(stringUrl);
        }
        else
        {
            Log.i(DEBUG_TAG, "Нет соединения!");
            Toast.makeText(getApplicationContext(), "Нет соединения с Интернетом!", Toast.LENGTH_SHORT).show();
            findViewById(R.id.pager).setVisibility(View.GONE);
            findViewById(R.id.refreshLinLay).setVisibility(View.VISIBLE);
            dialog.cancel();
        }
    }

    NetworkOperations.RequestTask.AsyncResponse scheduleResponse = new NetworkOperations.RequestTask.AsyncResponse() {
        @Override
        public void processFinish(Object result, String response) {
            if (result != null)
                scheduleManager.setWeeks((SparseArray<Week>) result);

            if (scheduleManager.getWeeksSize() == 0)
            {
                Log.i(DEBUG_TAG, "Пришел пустой результат!");
                if (result == null)
                    Toast.makeText(getApplicationContext(), "Невозможно установить интернет-соединение.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Неверный ответ сервера. Попробуйте позже.", Toast.LENGTH_SHORT).show();

                fillScheduleWithEmpty();
            }
            else
            {
                MemoryOperations.cacheSchedule(getApplicationContext(), response, searchWho, searchID);

                fillScheduleWithData();
            }

            dialog.cancel();
        }
    };

    private void fillScheduleWithEmpty()
    {
        findViewById(R.id.pager).setVisibility(View.GONE);
        findViewById(R.id.refreshLinLay).setVisibility(View.VISIBLE);
    }

    private void fillScheduleWithData()
    {
        List<View> pages = scheduleManager.getScheduleAsUI(searchWho + "s");

        //делишки со слайдингом для недель
        pagerAdapter = new SamplePageAdapter(pages);
        viewPager = (ViewPager) findViewById(R.id.pager);

        viewPager.setVisibility(View.VISIBLE);
        findViewById(R.id.refreshLinLay).setVisibility(View.GONE);

        viewPager.setAdapter(pagerAdapter);
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(pageChangeListener);
        viewPager.setCurrentItem(scheduleManager.currentWeekNumToIndex());

        actionBarWeek.setVisibility(View.VISIBLE);
        scheduleManager.setViewPager(viewPager);
    }

    ViewPager.OnPageChangeListener pageChangeListener = new  ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            Week selectedWeek = scheduleManager.getWeek(position);
            TabHost tabHost = (TabHost)viewPager.findViewWithTag("tabHost" + position);

            if (tabHost != null) {
                tabHost.setCurrentTab(scheduleManager.getDaySelected());
            }

            String date = selectedWeek.getDay(scheduleManager.getDaySelected()).getDate();
            if (actionBarWeek != null)
                actionBarWeek.setText(Integer.toString(selectedWeek.getWeekNum()) + " неделя, " + date);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private void getActionBarWeek()
    {
        RelativeLayout actionBarView = (RelativeLayout) actionBar.getCustomView();
        int childCount = actionBarView.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            View child = actionBarView.getChildAt(i);
            if (child.getId() == R.id.linLayInfo)
            {
                LinearLayout linearLayout = (LinearLayout) child;
                actionBarWeek = (TextViewCustomFont) linearLayout.getChildAt(1);
            }
        }
    }

    public void onRefreshClick (View v)
    {

    }
}
