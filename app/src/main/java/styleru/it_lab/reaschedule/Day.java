package styleru.it_lab.reaschedule;

import java.util.ArrayList;
import java.util.List;

public class Day {

    private int dayNum;
    private Lesson[] lessons;

    public Day(int _dayNum)
    {
        dayNum = _dayNum;
    }

    public Day(int _dayNum, boolean _empty)
    {
        dayNum = _dayNum;
        List<Lesson> lessonList = new ArrayList<>();
        for (int i = 0; i < 8; i++)
            lessonList.add(new Lesson(true));

        lessons = lessonList.toArray(new Lesson[lessonList.size()]);
    }

    public void setLessons (Lesson[] _lessons)
    {
        lessons = _lessons;
    }

    public Lesson getLesson(int index)
    {
        if (index >= 0 && index < lessons.length)
        {
            return lessons[index];
        }
        else
            return null;
    }

    public Lesson[] getLessons()
    {
        return lessons;
    }
}
