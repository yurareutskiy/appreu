package styleru.it_lab.reaschedule.Operations;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateOperations {

    public static final String DEBUG_TAG = "DateOperations_DEBUG";
    private static final String FIRST_STUDY_WEEK_MONDAY = "31.08.2015";

    public static int getCurrentWeekNum()
    {
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        try {
            Calendar c = Calendar.getInstance();
            long currentTime = c.getTimeInMillis() / 1000L;

            c.setTime(df.parse(FIRST_STUDY_WEEK_MONDAY));
            long startTime = c.getTimeInMillis() / 1000L;

            double weekFloat = ((currentTime - startTime) / 3600 / 24 / 7) + 1;
            return (int)weekFloat;
        }
        catch (java.text.ParseException e)
        {
            Log.i(DEBUG_TAG, "ERROR PARSING THE DATE: " + e.getMessage());
            return 1;
        }
    }

    public static int getCurrentDayNum()
    {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK) - 2;
        if (day < 0)
            day = 5; // 0 - понедельник; 5 - суббота

        return day;
    }

    public static int getCurrentLessonNumber()
    {
        final String[] lessonStartTime = {"08:30", "10:10", "11:50", "14:00", "15:40", "17:20", "19:00", "20:50"};
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        Calendar c = Calendar.getInstance();
        String currentTime = df.format(c.getTime());

        int currentLesson = 10;

        for (int i = 0; i < lessonStartTime.length; i++)
        {
            if (lessonStartTime[i].compareTo(currentTime) < 0)
            {
                currentLesson = i + 1;
            }
            else break;
        }

        return currentLesson;
    }
}
