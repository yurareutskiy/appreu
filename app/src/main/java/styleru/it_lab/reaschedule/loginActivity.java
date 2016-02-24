package styleru.it_lab.reaschedule;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class loginActivity extends Activity {

    LinearLayout loginLayout;
    LinearLayout login1Layout;
    AutoCompleteTextView editID;
    FrameLayout darkLayout;
    Map<Integer, String> members = new HashMap<>();

    String whatClicked = "";
    int memberID = 0;
    String memberName = "";
    String DBTable = "";

    public static final String DEBUG_TAG = "LOGIN_ACTIVITY_DEBUG";

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            editID.setText("");
            darkLayout.setVisibility(View.VISIBLE);

            switch (v.getId()) {
                case R.id.loginBtnStudent:
                    editID.setHint(getString(R.string.login1_student_placeholder));
                    whatClicked = "groups";
                    DBTable = MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_GROUPS;
                    break;
                case R.id.loginBtnLector:
                    editID.setHint(getString(R.string.login1_lector_placeholder));
                    whatClicked = "lectors";
                    DBTable = MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_LECTORS;
                    break;
            }

            members = MemoryOperations.DBMembersGet(getApplicationContext(), DBTable);
            if (members.isEmpty())
            {
                //в БД ничего не нашли, надо загружать
                Log.i(DEBUG_TAG, "Из таблицы " + DBTable + " ничего не пришло!");
                String stringUrl = getString(R.string.API_get_url) + whatClicked + "/";
                if (NetworkOperations.isConnectionAvailable(getApplicationContext())) {
                    Log.i(DEBUG_TAG, "Все в поряде!");
                    //new RequestTask().execute(stringUrl);
                    new NetworkOperations.RequestTask(response, "members").execute(stringUrl);
                } else {
                    Log.i(DEBUG_TAG, "Нет соединения!");
                    darkLayout.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Нет соединения с Интернетом!", Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                putMembersToList(false);
            }
        }
    };

    NetworkOperations.RequestTask.AsyncResponse response = new NetworkOperations.RequestTask.AsyncResponse() {
        @Override
        public void processFinish(Object result) {
            //запускается, когда запрос вернул ответ.
            if (result != null)
                members = (HashMap<Integer, String>) result;

            darkLayout.setVisibility(View.GONE);

            if (members.isEmpty())
            {
                Log.i(DEBUG_TAG, "Пришел пустой результат!");
                if (result == null)
                    Toast.makeText(getApplicationContext(), "Невозможно установить интернет-соединение.", Toast.LENGTH_SHORT);
                else
                    Toast.makeText(getApplicationContext(), "Неверный ответ сервера. Попробуйте позже.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Log.i(DEBUG_TAG, "Пришли результаты. Размер: " + members.size());
                putMembersToList(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Map<String, String> result = MemoryOperations.getSharedPreferences(getApplicationContext());
        memberID = Integer.parseInt(result.get("ID"));
        memberName = result.get("name");
        whatClicked = result.get("who");

        if (memberID != 0)
        {
            goToMainMenu();
            return;
        }

        //Шрифты
        Typeface HelvLight = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueCyr-Light.otf");
        Typeface HelvMedium = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueCyr-Medium.otf");
        Typeface HelvRoman = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueCyr-Roman.otf");

        Button btnLector = (Button) findViewById(R.id.loginBtnLector);
        Button btnStudent = (Button) findViewById(R.id.loginBtnStudent);
        TextView txtMadeBy = (TextView) findViewById(R.id.loginTxtMadeBy);
        TextView txtOr = (TextView) findViewById(R.id.loginTxtOr);
        Button btnEnter = (Button) findViewById(R.id.login1BtnEnter);
        editID = (AutoCompleteTextView) findViewById(R.id.login1EditID);
        TextView txt1MadeBy = (TextView) findViewById(R.id.login1MadeBy);

        btnLector.setTypeface(HelvLight);
        btnLector.setTransformationMethod(null);
        btnLector.setOnClickListener(onClickListener);

        btnStudent.setTypeface(HelvLight);
        btnStudent.setTransformationMethod(null);
        btnStudent.setOnClickListener(onClickListener);

        btnEnter.setTypeface(HelvRoman);
        btnEnter.setTransformationMethod(null);

        txtMadeBy.setTypeface(HelvLight);
        txtOr.setTypeface(HelvMedium);
        editID.setTypeface(HelvLight);
        txt1MadeBy.setTypeface(HelvLight);

        //
        loginLayout = (LinearLayout) findViewById(R.id.loginBottomLayout);
        login1Layout = (LinearLayout) findViewById(R.id.login1BottomLayout);
        darkLayout = (FrameLayout) findViewById(R.id.darkLayout);

    }

    public void putMembersToList(boolean writeToDB)
    {
        List<String> membersValues = new ArrayList<String>(members.values());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.login_list_item, membersValues);
        editID.setAdapter(adapter);

        if (writeToDB)
        {
            MemoryOperations.DBMembersSet(getApplicationContext(), members ,DBTable);
        }

        darkLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.GONE);
        login1Layout.setVisibility(View.VISIBLE);
    }

    public void onEnterClick(View v)
    {
        String editValue = editID.getText().toString();
        if (members.containsValue(editValue))
        {
            for (Map.Entry<Integer, String> e : members.entrySet())
            {
                int key = e.getKey();
                String value = e.getValue();
                if (value.equals(editValue))
                {
                    memberName = value;
                    memberID = key;
                    Log.i(DEBUG_TAG, "Data picked: ID - " + Integer.toString(memberID) + " NAME - " + memberName);
                    break;
                }
            }

            MemoryOperations.putSharedPreferences(getApplicationContext(), memberID, memberName, whatClicked);

            goToMainMenu();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Введите существующее значение!", Toast.LENGTH_LONG).show();
            return;
        }
    }

    public void goToMainMenu()
    {
        Log.i(DEBUG_TAG, "Starting MainMenuActivity");

        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
    }


}
