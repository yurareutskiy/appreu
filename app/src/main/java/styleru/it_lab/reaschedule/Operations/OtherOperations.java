package styleru.it_lab.reaschedule.Operations;


import android.app.Activity;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import styleru.it_lab.reaschedule.Schedule.Day;
import styleru.it_lab.reaschedule.Schedule.Lesson;
import styleru.it_lab.reaschedule.Schedule.Week;

public class OtherOperations {

    public static final String DEBUG_TAG = "OtherOperations_DEBUG";

    public static String shortName(String fullName)
    {
        String[] lec = fullName.split("\\s+");
        String res = lec[0];
        if (lec.length > 1)
        {
            lec[1] = lec[1].substring(0, 1);
            res += " " + lec[1] + ".";
        }
        if (lec.length > 2)
        {
            lec[2] = lec[2].substring(0, 1);
            res += lec[2] + ".";
        }

        return res;
    }

    public static Map<Integer, String> parseMembers(String result)
    {
        Map <Integer, String> members = new HashMap<>();

        try {
            JSONObject answer = new JSONObject(result);

            JSONObject successObj = answer.getJSONObject("success");
            JSONArray dataArray = successObj.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++)
            {
                JSONObject data = dataArray.getJSONObject(i);

                int id = data.getInt("ID");
                String name = data.getString("name");
                members.put(id, name);
            }

            //putMembersToList(true);

        } catch (JSONException e) {
            members.clear();
        } catch (NullPointerException e) {
            members = null;
        }

        return members;
    }

    public static SparseArray<Week> parseSchedule(String result)
    {
        final String[] fullWeekDaysEng = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        SparseArray<Week> weeks = new SparseArray<Week>();
        int weekCount = 0;

        try {
            JSONObject answer = new JSONObject(result);

            JSONObject successObj = answer.getJSONObject("success");
            JSONArray dataArray = successObj.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++)
            {
                //цикл по неделям
                JSONObject dataWeek = dataArray.getJSONObject(i);
                int weekNum = dataWeek.getInt("weekNum");

                Week currentWeek = new Week(weekNum);
                List<Day> daysList = new ArrayList<>(); //список дней в currentWeek

                JSONObject dataLessons = dataWeek.getJSONObject("lessons");
                for (int dayIterator = 0; dayIterator < 6; dayIterator++)
                {
                    //цикл по дням
                    JSONObject dataDay;

                    if (dataLessons.isNull(fullWeekDaysEng[dayIterator]))
                    {
                        //null, нет пар в этот день
                        daysList.add(new Day(dayIterator, true)); //добавил пустой день
                    }
                    else
                    {
                        //в этот день есть пары
                        dataDay = dataLessons.getJSONObject(fullWeekDaysEng[dayIterator]);

                        Day currentDay = new Day(dayIterator); // создал день
                        List<Lesson> lessonsList = new ArrayList<>(); // список пар в currentDay

                        for (int lessonIterator = 0; lessonIterator < 8; lessonIterator++)
                        {
                            //цикл по парам, текущая пара
                            JSONObject dataLesson;
                            if (dataDay.isNull(Integer.toString(lessonIterator)))
                            {
                                //null, нет lessonIterator'ой пары
                                lessonsList.add(new Lesson(true)); // добавил пустую пару
                            }
                            else
                            {
                                //эта пара есть!
                                dataLesson = dataDay.getJSONObject(Integer.toString(lessonIterator));

                                String hashID = dataLesson.getString("hash_id");
                                String lessonType = dataLesson.getString("lesson_type");
                                String room = dataLesson.getString("room");
                                String discipline = dataLesson.getString("discipline");
                                String building = dataLesson.getString("building");
                                String lector = dataLesson.getString("lector");
                                lector = OtherOperations.shortName(lector);
                                int housing = dataLesson.getInt("housing");
                                String lessonStart = dataLesson.getString("lesson_start");
                                String lessonEnd = dataLesson.getString("lesson_end");
                                int week_start = dataLesson.getInt("week_start");
                                int week_end = dataLesson.getInt("week_end");

                                JSONArray JSONGroups = dataLesson.getJSONArray("groups");
                                List<String> groups = new ArrayList<String>();


                                {
                                    for (int j = 0; j < JSONGroups.length(); j++) {
                                        String group = JSONGroups.getString(j);
                                        groups.add(group);
                                    }
                                }

                                Lesson curLesson = new Lesson(lessonIterator, room, housing, discipline, lessonStart, lessonEnd, lessonType, lector,
                                        groups, building, week_start, week_end);
                                lessonsList.add(curLesson);
                            }
                        }
                        //выход из цикла по парам
                        //запись в класс Day
                        currentDay.setLessons(lessonsList.toArray(new Lesson[lessonsList.size()]));
                        daysList.add(currentDay);
                    }
                }
                //выход из цикла по дням
                //запись в класс Week
                currentWeek.setDays(daysList.toArray(new Day[daysList.size()]));
                weeks.append(weekCount, currentWeek);
                weekCount++;
            }
            //выход из цикла по неделям


        } catch (JSONException e) {
            //Toast.makeText(getApplicationContext(), "Проблемы с интернет-соединением!", Toast.LENGTH_SHORT).show();
            //fillScheduleWithEmpty();
            weeks.clear();
        } catch (NullPointerException e) {
            //Toast.makeText(getApplicationContext(), "Неверный ответ сервера! Попробуйте позже.", Toast.LENGTH_SHORT).show();
            //fillScheduleWithEmpty();
            weeks = null;
        }

        return weeks;
    }

    public static boolean checkPlayServices(Activity activity) {
        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(DEBUG_TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }
}
