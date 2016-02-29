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
        String str = "Jun 13 2003 23:11:52.454 UTC";
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
            return 26;
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

}
