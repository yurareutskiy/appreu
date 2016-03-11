package styleru.it_lab.reaschedule;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import styleru.it_lab.reaschedule.Adapters.SamplePageAdapter;
import styleru.it_lab.reaschedule.Operations.MemoryOperations;
import styleru.it_lab.reaschedule.Operations.NetworkOperations;
import styleru.it_lab.reaschedule.Operations.OtherOperations;
import styleru.it_lab.reaschedule.Operations.ScheduleUIManager;
import styleru.it_lab.reaschedule.Schedule.Week;

@SuppressWarnings("unchecked")
public class MainMenuActivity extends AppCompatActivity {

    public static final String DEBUG_TAG = "MAIN_MENU_DEBUG";

    TextView actionBarWeek;
    ActionBar actionBar;
    int memberID = 0;
    String memberName = "";
    String memberWho = "";
    String memberWhoUrl = "";

    ScheduleUIManager scheduleManager;
    Context thisContext = this;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Log.i(DEBUG_TAG, "CREATED MAIN_MENU");

        scheduleManager = new ScheduleUIManager(this, DEBUG_TAG);

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
        dialog = ProgressDialog.show(thisContext, "", "Загрузка...", true, false);

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
                SparseArray<Week> tmpWeeks = MemoryOperations.getCachedSchedule(getApplicationContext(), memberWho, memberID);
                Message msg = h.obtainMessage(1, tmpWeeks);
                h.sendMessage(msg);
            }
        });
        newT.start();
    }

    private void getDataForSchedulePost(SparseArray<Week> tmpWeeks)
    {
        if (tmpWeeks.size() == 0)
        {
            //получение расписания
            getSchedule();
        }
        else
        {
            Log.i(DEBUG_TAG, "Loaded schedule from cache! Vot tak!");
            scheduleManager.setWeeks(tmpWeeks);
            fillActionBarWithData();
            fillScheduleWithData();
            dialog.cancel();
        }
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
                actionBarWeek.setText(Integer.toString(scheduleManager.getCurrentWeek()) + " неделя");
            }
        }
    }

    private void fillScheduleWithData()
    {
        List<View> pages = scheduleManager.getScheduleAsUI(memberWho);

        //делишки со слайдингом для недель
        SamplePageAdapter pagerAdapter = new SamplePageAdapter(pages);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        viewPager.setVisibility(View.VISIBLE);
        findViewById(R.id.refreshLinLay).setVisibility(View.GONE);

        viewPager.setAdapter(pagerAdapter);
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(pageChangeListener);
        viewPager.setCurrentItem(scheduleManager.currentWeekNumToIndex());
    }

    private void getSchedule()
    {
        String stringUrl = getString(R.string.API_url) + "lessons/?who=" +  memberWhoUrl + "&id=" + memberID + "&timestamp=0";

        if (NetworkOperations.isConnectionAvailable(getApplicationContext()))
        {
            Log.i(DEBUG_TAG, "Все в поряде!");
            scheduleManager.setWeeks(new SparseArray<Week>());

            final NetworkOperations.RequestTask asyncTask = new NetworkOperations.RequestTask(response, "schedule");

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
        }
    }

    NetworkOperations.RequestTask.AsyncResponse response = new NetworkOperations.RequestTask.AsyncResponse() {
        @Override
        public void processFinish(Object result, String response) {
            if (result != null)
                scheduleManager.setWeeks((SparseArray<Week>) result);

            dialog.cancel();
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
                MemoryOperations.cacheSchedule(getApplicationContext(), response, memberWho, memberID);

                fillActionBarWithData();
                fillScheduleWithData();
            }
        }
    };

    private void setupActionBar()
    {
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        RelativeLayout actionBarView = (RelativeLayout) getLayoutInflater().inflate(R.layout.schedule_actionbar, null);
        actionBar.setCustomView(actionBarView);
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

    ViewPager.OnPageChangeListener pageChangeListener = new  ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            Log.i(DEBUG_TAG, "PAGE CHANGED TO " + position);
            actionBarWeek.setText(Integer.toString(scheduleManager.getWeek(position).getWeekNum()) + " неделя");
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
            Intent accountIntent = new Intent(this, AccountActivity.class);
            startActivity(accountIntent);
        }
        else if (id == R.id.imgSearch)
        {
            //Toast.makeText(getApplicationContext(), "Поиск скоро будет доступен!", Toast.LENGTH_SHORT).show();
            Intent searchIntent = new Intent(this, SearchActivity.class);
            startActivity(searchIntent);
        }
    }
}
