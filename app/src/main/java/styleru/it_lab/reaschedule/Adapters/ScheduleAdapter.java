package styleru.it_lab.reaschedule.Adapters;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import styleru.it_lab.reaschedule.Operations.DateOperations;
import styleru.it_lab.reaschedule.Operations.MemoryOperations;
import styleru.it_lab.reaschedule.R;
import styleru.it_lab.reaschedule.Schedule.Lesson;

public class ScheduleAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Lesson> objects;
    String memberWho = "";
    boolean current = false;

    public ScheduleAdapter(Context context, ArrayList<Lesson> lessons) {
        ctx = context;
        objects = lessons;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        memberWho = MemoryOperations.getSharedPreferences(ctx).get("who");
    }

    public ScheduleAdapter(Context context, ArrayList<Lesson> lessons, boolean _current) {
        ctx = context;
        objects = lessons;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        current = _current;
        memberWho = MemoryOperations.getSharedPreferences(ctx).get("who");
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Lesson p = getLesson(position);

        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null)
        {
            if (current && p.number == DateOperations.getCurrentLessonNumber())
            {
                view = lInflater.inflate(R.layout.schedule_list_item_current, parent, false);
            }
            else
            {
                view = lInflater.inflate(R.layout.schedule_list_item, parent, false);
            }
        }

        // заполняем View в пункте списка данными из класса
        if (!p.empty) {
            ((TextView) view.findViewById(R.id.sch_txtTime)).setText(p.beginsAt + " - " + p.endsAt);
            ((TextView) view.findViewById(R.id.sch_txtLesson)).setText(p.name);
            ((TextView) view.findViewById(R.id.sch_txtRoom)).setText("ауд " + p.auditoty + "\n"+ p.building + " к " + p.housing);

            String weeks = "";
            if (p.week_start == p.week_end) {
                weeks = Integer.toString(p.week_start) + " неделя";
            }
            else {
                weeks = Integer.toString(p.week_start) + " - " + Integer.toString(p.week_end) + " недели";
            }

            String lector = "";
            if (memberWho.equals("groups")) {
                lector = " " + p.lector;
            }
            else {
                for (int i = 0; i < p.groups.size(); i++)
                {
                    String group = p.groups.get(i);
                    if (i == p.groups.size() - 1) {
                        lector += " " + group;
                    }
                    else {
                        lector += " " + group + ", ";
                    }
                }
            }



            ((TextView) view.findViewById(R.id.sch_txtInfo)).setText(p.type + " | " + weeks + " |" + lector);
        }
        else
        {
            view.findViewById(R.id.sch_relLayout).setVisibility(View.INVISIBLE);
        }

        return view;
    }

    // урок по позиции
    Lesson getLesson(int position) {
        return ((Lesson) getItem(position));
    }
}