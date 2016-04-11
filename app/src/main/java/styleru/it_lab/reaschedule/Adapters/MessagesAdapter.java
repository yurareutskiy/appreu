package styleru.it_lab.reaschedule.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import styleru.it_lab.reaschedule.CustomFontViews.TextViewCustomFont;
import styleru.it_lab.reaschedule.Operations.DateOperations;
import styleru.it_lab.reaschedule.R;
import styleru.it_lab.reaschedule.Schedule.Lesson;
import styleru.it_lab.reaschedule.Schedule.Message;

public class MessagesAdapter extends BaseAdapter {

    Context ctx;
    ArrayList<Message> objects;
    LayoutInflater lInflater;

    public MessagesAdapter(Context context, ArrayList<Message> messages) {
        ctx = context;
        objects = messages;
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

        Message m = getMessage(position);

        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null)
        {
            view = lInflater.inflate(R.layout.messages_list_item, parent, false);
        }

        // заполняем View в пункте списка данными из класса
        if (m != null)
        {
            ((TextViewCustomFont) view.findViewById(R.id.txtDate)).setText(m.getDate());
            ((TextViewCustomFont) view.findViewById(R.id.txtMsg)).setText(m.getMsg());
        }

        return view;
    }

    // урок по позиции
    private Message getMessage(int position) {
        return ((Message) getItem(position));
    }

}
