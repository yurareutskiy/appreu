package styleru.it_lab.reaschedule;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import styleru.it_lab.reaschedule.Adapters.SamplePageAdapter;
import styleru.it_lab.reaschedule.Adapters.ScheduleAdapter;
import styleru.it_lab.reaschedule.Operations.MemoryOperations;
import styleru.it_lab.reaschedule.Operations.NetworkOperations;
import styleru.it_lab.reaschedule.Operations.OtherOperations;
import styleru.it_lab.reaschedule.Schedule.Lesson;
import styleru.it_lab.reaschedule.Schedule.Week;

public class MainMenuActivity extends AppCompatActivity {

    public static final String DEBUG_TAG = "MAIN_MENU_DEBUG";
    public static final String[] weekDays = new String[] {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб",};

    TextView actionBarWeek;
    LayoutInflater inflater;
    ActionBar actionBar;
    int memberID = 0;
    String memberName = "";
    String memberWho = "";
    String memberWhoUrl = "";
    int currentWeek = 26;
    int weekCount = 0;
    Context thisContext = this;
    SparseArray<Week> weeks = new SparseArray<Week>();
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Log.i(DEBUG_TAG, "CREATED MAIN_MENU");

        //Делишки с actionbar'ом
        Toolbar myToolbar = (Toolbar) findViewById(R.id.schToolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setContentInsetsAbsolute(0, 0);
        setupActionBar();


        getDataForSchedule();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(DEBUG_TAG, "Checking preferences for differences.");
        if (checkIfPreferencesChanged())
        {
            getDataForSchedule();
        }
    }

    private void getDataForSchedule()
    {
        //Делишки с SharedPreferences
        if (!setupSharedPreferences())
            return;

        Log.i(DEBUG_TAG, "Attempt to get cached schedule");
        weeks = MemoryOperations.getCachedSchedule(getApplicationContext(), memberWho, memberID);

        if (weeks.size() == 0)
        {
            //получение расписания
            getSchedule();
        }
        else
        {
            Log.i(DEBUG_TAG, "Loaded schedule from cache! Vot tak!");
            weekCount = weeks.size();
            fillActionBarWithData();
            fillScheduleWithData();
        }
    }

    private void fillScheduleWithData()
    {
        //делишки с получением текущей недели и текущего дня

        //делишки с контетом для табов
        List<View> pages = new ArrayList<View>();
        inflater = LayoutInflater.from(getApplicationContext());
        View page;
        TabHost tabHost;
        TabHost.TabSpec tabSpec;

        for (int weekIterator = 0; weekIterator < weekCount; weekIterator++)
        {
            page = inflater.inflate(R.layout.week_schedule, null);
            tabHost = (TabHost) page.findViewById(R.id.tabHost);
            tabHost.setup();

            for (int dayIterator = 0; dayIterator < 6; dayIterator++)
            {
                tabSpec = tabHost.newTabSpec(Integer.toString(weekIterator)+"."+Integer.toString(dayIterator));

                View tabView = getLayoutInflater().inflate(R.layout.tab_header, null);
                TextView tv = (TextView) tabView.findViewById(R.id.tabTitleText);
                tv.setText(weekDays[dayIterator]);

                tabSpec.setIndicator(tabView);
                tabSpec.setContent(tabFactory); //контент текущего таба

                tabHost.addTab(tabSpec);
            }
            tabHost.setCurrentTab(0);
            pages.add(page);
        }

        //делишки со слайдингом для недель
        SamplePageAdapter pagerAdapter = new SamplePageAdapter(pages);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        viewPager.setVisibility(View.VISIBLE);
        findViewById(R.id.refreshLinLay).setVisibility(View.GONE);

        viewPager.setAdapter(pagerAdapter);
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(pageChangeListener);
        viewPager.setCurrentItem(weekNumToIndex(currentWeek));
    }

    private void getSchedule()
    {
        String stringUrl = getString(R.string.API_url) + "lessons/?who=" +  memberWhoUrl + "&id=" + memberID + "&timestamp=0";

        if (NetworkOperations.isConnectionAvailable(getApplicationContext()))
        {
            Log.i(DEBUG_TAG, "Все в поряде!");
            weekCount = 0;
            weeks.clear();
            dialog = ProgressDialog.show(thisContext, "", "Загрузка...", true, true);
            final NetworkOperations.RequestTask asyncTask = new NetworkOperations.RequestTask(response, "schedule");

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
        }
    }

    NetworkOperations.RequestTask.AsyncResponse response = new NetworkOperations.RequestTask.AsyncResponse() {
        @Override
        public void processFinish(Object result, String response) {
            if (result != null)
                weeks = (SparseArray<Week>) result;

            dialog.cancel();
            if (weeks.size() == 0)
            {
                Log.i(DEBUG_TAG, "Пришел пустой результат!");
                if (result == null)
                    Toast.makeText(getApplicationContext(), "Невозможно установить интернет-соединение.", Toast.LENGTH_SHORT);
                else
                    Toast.makeText(getApplicationContext(), "Неверный ответ сервера. Попробуйте позже.", Toast.LENGTH_SHORT).show();

                fillScheduleWithEmpty();
            }
            else
            {
                weekCount = weeks.size();
                MemoryOperations.cacheSchedule(getApplicationContext(), response, memberWho, memberID);

                fillActionBarWithData();
                fillScheduleWithData();
            }
        }
    };

    TabHost.TabContentFactory tabFactory = new TabHost.TabContentFactory() {
        @Override
        public View createTabContent(String tag) {

            ArrayList<Lesson> lessons = getLessonsData(tag);
            if (lessons.size() != 0)
            {
                ScheduleAdapter schAdapter = new ScheduleAdapter(getApplicationContext(), lessons);

                ListView listContent = (ListView) inflater.inflate(R.layout.day_list_view, null);
                listContent.setAdapter(schAdapter);

                return listContent;
            }
            else
            {
                View emptyDay = inflater.inflate(R.layout.day_empty, null);
                return emptyDay;
            }

        }
    };

    private ArrayList<Lesson> getLessonsData(String tag)
    {
        String[] splitString = tag.split("\\.");
        int weekNum = Integer.parseInt(splitString[0]);
        int dayNum = Integer.parseInt(splitString[1]);

        Lesson[] lessonsAtThisDay = weeks.get(weekNum).getDay(dayNum).getLessons();
        ArrayList<Lesson> list = new ArrayList<Lesson>();
        for (int i = 0; i < lessonsAtThisDay.length; i++)
        {
            if (!lessonsAtThisDay[i].empty)
                list.add(lessonsAtThisDay[i]);
        }

        return list;
    }

    private void setupActionBar()
    {
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        RelativeLayout actionBarView = (RelativeLayout) getLayoutInflater().inflate(R.layout.schedule_actionbar, null);
        actionBar.setCustomView(actionBarView);
    }

    private void fillActionBarWithData()
    {
        RelativeLayout actionBarView = (RelativeLayout) actionBar.getCustomView();
        int childCount = actionBarView.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            View child = actionBarView.getChildAt(i);
            if (child.getId() == R.id.linLayInfo)
            {
                LinearLayout linearLayout = (LinearLayout) child;

                TextView textView = (TextView) linearLayout.getChildAt(0);

                if (memberWho.equals(getString(R.string.WHO_LECTOR))) {
                    textView.setText(OtherOperations.shortName(memberName));
                }
                else
                {
                    textView.setText(memberName);
                }

                actionBarWeek = (TextView) linearLayout.getChildAt(1);
                actionBarWeek.setText(Integer.toString(currentWeek) + " неделя");
            }
        }
    }

    private boolean checkIfPreferencesChanged()
    {
        Map<String, String> result = MemoryOperations.getSharedPreferences(getApplicationContext());
        int tmpMemberID = Integer.parseInt(result.get("ID"));
        String tmpMemberName = result.get("name");
        String tmpMemberWho = result.get("who");

        if (tmpMemberID != memberID || !tmpMemberWho.equals(memberWho))
        {
            Log.i(DEBUG_TAG, "Preferences are different! " + memberID + "/" + memberWho + " was changed to " + tmpMemberID + "/" + tmpMemberWho);
            MemoryOperations.putSharedPreferences(getApplicationContext(), tmpMemberID, tmpMemberName, tmpMemberWho);
            return true;
        }
        return false;
    }

    private boolean setupSharedPreferences()
    {
        Map<String, String> result = MemoryOperations.getSharedPreferences(getApplicationContext());
        memberID = Integer.parseInt(result.get("ID"));
        memberName = result.get("name");
        memberWho = result.get("who");
        Log.i(DEBUG_TAG, "Got results! ID: " + Integer.toString(memberID) + "; Name: " + memberName + " is " + memberWho);

        if (memberID == 0)
        {
            goToLoginActivity();
            return false;
        }

        memberWhoUrl = memberWho.substring(0, memberWho.length() - 1);
        return true;
    }

    private void goToLoginActivity()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private int weekNumToIndex(int weekNum)
    {
        for (int i = 0; i < weeks.size(); i++)
        {
            int key = weeks.keyAt(i);
            Week week = weeks.get(key);
            if (week.getWeekNum() == weekNum)
            {
                return key;
            }
        }

        return 0;
    }

    ViewPager.OnPageChangeListener pageChangeListener = new  ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            Log.i(DEBUG_TAG, "PAGE CHANGED TO " + position);
            actionBarWeek.setText(Integer.toString(weeks.get(position).getWeekNum()) + " неделя");
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
        int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private void fillScheduleWithEmpty()
    {
        findViewById(R.id.pager).setVisibility(View.GONE);
        findViewById(R.id.refreshLinLay).setVisibility(View.VISIBLE);
    }

    public void onRefreshClick (View v)
    {
        getSchedule();
    }

    public void onActionBarClick(View v)
    {
        int id = v.getId();
        if (id == R.id.imgAccount)
        {
            //Toast.makeText(getApplicationContext(), "Личный кабинет скоро будет доступен!", Toast.LENGTH_SHORT).show();
            Intent accountIntent = new Intent(this, AccountActivity.class);
            startActivity(accountIntent);
        }
        else if (id == R.id.imgSearch)
        {
            Toast.makeText(getApplicationContext(), "Поиск скоро будет доступен!", Toast.LENGTH_SHORT).show();
        }
    }
}
