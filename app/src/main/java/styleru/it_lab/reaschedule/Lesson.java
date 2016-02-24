package styleru.it_lab.reaschedule;

import java.util.List;

public class Lesson {

    int number;
    String auditoty;
    int housing;

    String name;
    String beginsAt;
    String endsAt;
    String type;
    String lector;
    String building;
    List<String> groups;
    boolean empty;

    public Lesson (int _number, String _auditory, int _housing, String _name, String _beginsAt,
                   String _endsAt, String _type, String _lector, List<String> _groups, String _building)
    {
        number = _number;
        auditoty = _auditory;
        housing = _housing;
        name = _name;
        beginsAt = _beginsAt;
        endsAt = _endsAt;
        type = _type;
        groups = _groups;
        building = _building;
        empty = false;

        if (type.equals("Л"))
            type = "Лекция";
        else if (type.equals("С"))
            type = "Семинар";
        else if (type.equals("П"))
            type = "Практическое занятие";

        if (_lector.equals("null"))
            lector = "";
        else
            lector = _lector;
    }

    public Lesson(boolean _empty)
    {
        empty = _empty;
    }


}
