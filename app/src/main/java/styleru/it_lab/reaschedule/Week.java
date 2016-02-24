package styleru.it_lab.reaschedule;

public class Week {

    private int weekNum;
    private Day[] days;

    public Week(int _weekNum)
    {
        weekNum = _weekNum;
    }

    public void setDays(Day[] _days)
    {
        days = _days;
    }

    public Day getDay(int index)
    {
        if (index >= 0 && index < days.length)
        {
            return days[index];
        }
        else
            return null;
    }
    public int getWeekNum()
    {
        return weekNum;
    }
}
