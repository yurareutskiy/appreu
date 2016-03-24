package styleru.it_lab.reaschedule.Operations;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import styleru.it_lab.reaschedule.Adapters.ScheduleAdapter;
import styleru.it_lab.reaschedule.CustomFontViews.TextViewCustomFont;
import styleru.it_lab.reaschedule.R;
import styleru.it_lab.reaschedule.Schedule.Lesson;
import styleru.it_lab.reaschedule.Schedule.Week;

public class ScheduleUIManager
{
    public static final String[] weekDays = new String[] {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб",};

    private String DEBUG_TAG = "";
    private Context context;
    private LayoutInflater inflater;
    private SparseArray<Week> weeks;
    private TextViewCustomFont txtWeek;
    private ViewPager viewPager;
    private int currentWeek;
    private int currentDay;
    private int weekCount;
    private int daySelected;

    private String forWho = "";

    public ScheduleUIManager (Context _context, String _DEBUG_TAG)
    {
        context = _context;
        inflater = LayoutInflater.from(context);
        DEBUG_TAG = _DEBUG_TAG;
        weeks = new SparseArray<>();
        currentWeek = DateOperations.getCurrentWeekNum();
        currentDay = DateOperations.getCurrentDayNum();
        daySelected = currentDay;
        weekCount = 0;
    }

    public void setViewPager(ViewPager _viewPager)
    {
        viewPager = _viewPager;
    }

    public void setTxtWeek(TextViewCustomFont _txt)
    {
        txtWeek = _txt;
    }

    public int getDaySelected(){return daySelected;}

    public int getCurrentWeekNum()
    {
        return currentWeek;
    }

    public String getDate(int weekNum, int dayNum)
    {
        return getWeek(weekNum).getDay(dayNum).getDate();
    }

    public String getDate()
    {
        return getWeek(currentWeek).getDay(currentDay).getDate();
    }

    public int getCurrentDay()
    {
        return currentDay;
    }

    public void setWeeks(SparseArray<Week> _weeks)
    {
        weeks = _weeks;
        weekCount = weeks.size();
    }

    public Week getWeek(int index)
    {
        return weeks.get(index);
    }

    public int getWeeksSize()
    {
        return weeks.size();
    }

    public int currentWeekNumToIndex()
    {
        for (int i = 0; i < weeks.size(); i++)
        {
            int key = weeks.keyAt(i);
            Week week = weeks.get(key);
            if (week.getWeekNum() == currentWeek)
            {
                return key;
            }
        }

        return 0;
    }

    public List<View> getScheduleAsUI(String _forWho)
    {
        forWho = _forWho;

        //делишки с контетом для табов
        List<View> pages = new ArrayList<View>();
        View page;
        TabHost tabHost;
        TabHost.TabSpec tabSpec;

        for (int weekIterator = 0; weekIterator < weekCount; weekIterator++)
        {
            page = inflater.inflate(R.layout.week_schedule, null);
            tabHost = (TabHost) page.findViewById(R.id.tabHost);
            tabHost.setup();
            tabHost.setTag("tabHost"+ weekIterator);

            int tmpWeekNum = weeks.get(weekIterator).getWeekNum();

            for (int dayIterator = 0; dayIterator < 6; dayIterator++)
            {
                tabSpec = tabHost.newTabSpec(Integer.toString(weekIterator)+"."+Integer.toString(dayIterator)+"."+Integer.toString(tmpWeekNum));

                View tabView = inflater.inflate(R.layout.tab_header, null);
                TextView tv = (TextView) tabView.findViewById(R.id.tabTitleText);
                tv.setText(ScheduleUIManager.weekDays[dayIterator]);

                tabSpec.setIndicator(tabView);
                tabSpec.setContent(tabFactory); //контент текущего таба

                tabHost.addTab(tabSpec);
            }
            //if (tmpWeekNum == currentWeek)
            tabHost.setCurrentTab(currentDay);
            //else
            //    tabHost.setCurrentTab(0);

            tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                @Override
                public void onTabChanged(String tabId) {
                    String[] data = tabId.split("\\.");
                    daySelected = Integer.parseInt(data[1]);
                    int position = viewPager.getCurrentItem();
                    Week selectedWeek = getWeek(position);

                    txtWeek.setText(selectedWeek.getWeekNum() + " неделя, " + selectedWeek.getDay(daySelected).getDate());
                }
            });
            pages.add(page);
        }

        return pages;
    }

    TabHost.TabContentFactory tabFactory = new TabHost.TabContentFactory() {
        @Override
        public View createTabContent(String tag) {

            String[] splitString = tag.split("\\.");
            int weekNum = Integer.parseInt(splitString[0]);
            int dayNum = Integer.parseInt(splitString[1]);
            int currentWeekNum = Integer.parseInt(splitString[2]);
            boolean isCurrent = currentWeekNum == currentWeek && dayNum == currentDay;

            ArrayList<Lesson> lessons = getLessonsData(tag, weekNum, dayNum);
            if (lessons.size() != 0)
            {
                ScheduleAdapter schAdapter = new ScheduleAdapter(context, lessons, forWho, isCurrent);

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

    private ArrayList<Lesson> getLessonsData(String tag, int weekNum, int dayNum)
    {
        Lesson[] lessonsAtThisDay = weeks.get(weekNum).getDay(dayNum).getLessons();
        ArrayList<Lesson> list = new ArrayList<Lesson>();
        for (int i = 0; i < lessonsAtThisDay.length; i++)
        {
            if (!lessonsAtThisDay[i].empty)
                list.add(lessonsAtThisDay[i]);
        }

        return list;
    }




}
