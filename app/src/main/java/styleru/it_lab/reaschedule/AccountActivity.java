package styleru.it_lab.reaschedule;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import styleru.it_lab.reaschedule.Adapters.SamplePageAdapter;
import styleru.it_lab.reaschedule.Operations.DateOperations;
import styleru.it_lab.reaschedule.Operations.MemoryOperations;
import styleru.it_lab.reaschedule.Operations.NetworkOperations;
import styleru.it_lab.reaschedule.Operations.OtherOperations;

public class AccountActivity extends AppCompatActivity {

    public static final String DEBUG_TAG = "ACCOUNT_ACTIVITY_DEBUG";

    int memberID = 0;
    int currentWeek = 26;
    String memberName = "";
    String memberWho = "";
    TextView txtMemberName;
    TextView txtWeek;
    Map<Integer, String> membersGroups = new HashMap<Integer, String>();
    Map<Integer, String> membersLectors = new HashMap<Integer, String>();
    ProgressDialog dialog;
    String whoIsEmpty;
    Context thisContext;
    TabHost tabHost;
    LayoutInflater inflater;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        thisContext = this;
        inflater = LayoutInflater.from(getApplicationContext());
        currentWeek = DateOperations.getCurrentWeekNum();

        if (!setupSharedPreferences())
            return;

        viewPager = (ViewPager) findViewById(R.id.accountPager);
        tabHost = (TabHost) findViewById(R.id.accTabHost);

        setHeaderText();
        setHeaderTabs();
        setViewPagerContent();
    }

    private void setHeaderTabs()
    {
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("changings");
        View tabView = inflater.inflate(R.layout.account_tab_header, null);
        TextView tv = (TextView) tabView.findViewById(R.id.txtAccTabName);
        tv.setText("Изменения");
        tabSpec.setIndicator(tabView);
        tabSpec.setContent(R.id.txtEmpty);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("messages");
        tabView = inflater.inflate(R.layout.account_tab_header, null);
        tv = (TextView) tabView.findViewById(R.id.txtAccTabName);
        tv.setText("Сообщения");
        tabSpec.setIndicator(tabView);
        tabSpec.setContent(R.id.txtEmpty);
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);
        tabHost.setOnTabChangedListener(onTabChangeListener);
    }

    TabHost.OnTabChangeListener onTabChangeListener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            viewPager.setCurrentItem(tabHost.getCurrentTab());
        }
    };

    private void setViewPagerContent()
    {
        View page;
        List<View> pages = new ArrayList<View>();

        page = inflater.inflate(R.layout.account_changings, null);
        pages.add(page);

        page = inflater.inflate(R.layout.account_messages, null);
        pages.add(page);

        SamplePageAdapter pagerAdapter = new SamplePageAdapter(pages);

        viewPager.setAdapter(pagerAdapter);
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(pageChangeListener);
        viewPager.setCurrentItem(0);

    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            tabHost.setCurrentTab(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private boolean setupSharedPreferences()
    {
        Map<String, String> result = MemoryOperations.getSharedPreferences(getApplicationContext());
        memberID = Integer.parseInt(result.get("ID"));
        memberName = result.get("name");
        memberWho = result.get("who");
        Log.i(DEBUG_TAG, "Got results! ID: " + Integer.toString(memberID) + "; Name: " + memberName + " is " + memberWho);

        if (memberID == 0)
        {
            goToLoginActivity();
            return false;
        }

        return true;
    }

    private void goToLoginActivity()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    NetworkOperations.RequestTask.AsyncResponse response = new NetworkOperations.RequestTask.AsyncResponse() {
        @Override
        public void processFinish(Object result, String response)
        {
            if (result != null)
            {
                if (membersLectors.isEmpty())
                    membersLectors = (HashMap<Integer, String>) result;
                else if (membersGroups.isEmpty())
                    membersGroups = (HashMap<Integer, String>) result;
            }

            dialog.cancel();
            if (membersLectors.isEmpty() || membersGroups.isEmpty())
            {
                Log.i(DEBUG_TAG, "Пришел пустой результат!");
                if (result == null)
                    Toast.makeText(getApplicationContext(), "Невозможно установить интернет-соединение.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Неверный ответ сервера. Попробуйте позже.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if (whoIsEmpty.equals("groups"))
                    MemoryOperations.DBMembersSet(thisContext, membersGroups, MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_GROUPS);
                else if (whoIsEmpty.equals("lectors"))
                    MemoryOperations.DBMembersSet(thisContext, membersLectors, MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_LECTORS);

                new ChangeIDDialogFragment().show(getSupportFragmentManager(), "DialogChangeID");
            }
        }
    };

    public void onChangeIDClick(View v)
    {
        dialog = ProgressDialog.show(this, "", "Загрузка...", true, true);
        if (membersGroups.isEmpty() || membersLectors.isEmpty())
        {
            membersGroups = MemoryOperations.DBMembersGet(getApplicationContext(), MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_GROUPS);
            membersLectors = MemoryOperations.DBMembersGet(getApplicationContext(), MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_LECTORS);
        }

        final NetworkOperations.RequestTask asyncTask;
        if (membersGroups.isEmpty() || membersLectors.isEmpty())
        {
            asyncTask = new NetworkOperations.RequestTask(response, "members");
        }
        else
        {
            dialog.cancel();
            new ChangeIDDialogFragment().show(getSupportFragmentManager(), "DialogChangeID");
            return;
        }

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                asyncTask.cancel(true);
            }
        });

        whoIsEmpty = membersGroups.isEmpty() ? "groups" : "lectors";
        String stringUrl = getString(R.string.API_get_url) + whoIsEmpty + "/";
        asyncTask.execute(stringUrl);

    }

    public void setHeaderText()
    {
        txtMemberName = (TextView) findViewById(R.id.acc_txtGroup);
        txtWeek = (TextView) findViewById(R.id.acc_txtWeek);
        txtWeek.setText(Integer.toString(currentWeek) + " неделя");

        if (memberWho.equals(getString(R.string.WHO_LECTOR))) {
            txtMemberName.setText(OtherOperations.shortName(memberName));
        }
        else
        {
            txtMemberName.setText(memberName);
        }
    }

    @SuppressLint("ValidFragment")
    public class ChangeIDDialogFragment extends DialogFragment {

        private AutoCompleteTextView txtChangeID;
        Map<Integer, String> members = new HashMap<>();
        RadioGroup rgWho;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            View dialogView = inflater.inflate(R.layout.dialog_change_settings, null);
            txtChangeID = (AutoCompleteTextView) dialogView.findViewById(R.id.acc_txtSelectID);
            rgWho = (RadioGroup) dialogView.findViewById(R.id.radioGroupWho);
            rgWho.clearCheck();
            rgWho.setOnCheckedChangeListener(onCheckedChangeListener);

            if (memberWho.equals("lectors"))
            {
                txtChangeID.setHint(getString(R.string.login1_lector_placeholder));
                rgWho.check(R.id.radBLector);
            }
            else if (memberWho.equals("groups"))
            {
                txtChangeID.setHint(getString(R.string.login1_student_placeholder));
                rgWho.check(R.id.radBStudent);
            }

            builder.setTitle("Изменить");
            builder.setView(dialogView)
                    .setPositiveButton("Сохранить", null)
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ChangeIDDialogFragment.this.getDialog().cancel();
                        }
                    });

            return builder.create();
        }

        @Override
        public void onStart()
        {
            super.onStart();
            final AlertDialog d = (AlertDialog)getDialog();
            if(d != null)
            {
                Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Boolean wantToCloseDialog = false;
                        String tmpName = txtChangeID.getText().toString();
                        int checkedRadioID = rgWho.getCheckedRadioButtonId();
                        if (members.containsValue(tmpName)) {
                            for (Map.Entry<Integer, String> e : members.entrySet())
                            {
                                int key = e.getKey();
                                String value = e.getValue();
                                if (value.equals(tmpName))
                                {
                                    memberName = value;
                                    memberID = key;
                                    memberWho = checkedRadioID == R.id.radBStudent ? "groups" : "lectors";
                                    MemoryOperations.putSharedPreferences(getApplicationContext(), memberID, memberName, memberWho);
                                    wantToCloseDialog = true;
                                    setHeaderText();
                                    break;
                                }
                            }
                        }

                        if(wantToCloseDialog)
                            d.dismiss();
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Введите существующее значение!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

        RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.radBStudent:
                        Log.i(DEBUG_TAG, "CHECHKED STUDENT");
                        txtChangeID.setHint(getString(R.string.login1_student_placeholder));
                        txtChangeID.setText("");
                        members = membersGroups;
                        Log.i(DEBUG_TAG, "Students size: " + membersGroups.size());
                        break;
                    case R.id.radBLector:
                        Log.i(DEBUG_TAG, "CHECHKED LECTOR");
                        txtChangeID.setHint(getString(R.string.login1_lector_placeholder));
                        txtChangeID.setText("");
                        members = membersLectors;
                        Log.i(DEBUG_TAG, "Lectors size: " + membersLectors.size());
                        break;
                    default:
                        return;
                }

                List<String> membersValues = new ArrayList<String>(members.values());
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.login_list_item, membersValues);
                txtChangeID.setAdapter(adapter);
            }
        };
    }

    public void onButtonBackClick(View v)
    {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
