package styleru.it_lab.reaschedule.Adapters;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import styleru.it_lab.reaschedule.R;
import styleru.it_lab.reaschedule.Schedule.Lesson;

public class ScheduleAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Lesson> objects;

    public ScheduleAdapter(Context context, ArrayList<Lesson> lessons) {
        ctx = context;
        objects = lessons;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.schedule_list_item, parent, false);
        }

        Lesson p = getLesson(position);

        // заполняем View в пункте списка данными из класса
        if (!p.empty) {
            ((TextView) view.findViewById(R.id.sch_txtTime)).setText(p.beginsAt + " - " + p.endsAt);
            ((TextView) view.findViewById(R.id.sch_txtLesson)).setText(p.name);
            ((TextView) view.findViewById(R.id.sch_txtRoom)).setText("ауд " + p.auditoty + "\n"+ p.building + " к " + p.housing);
            ((TextView) view.findViewById(R.id.sch_txtInfo)).setText(p.type + " | " + "хх - хх недели" + " | " + p.lector);
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