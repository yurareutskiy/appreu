package styleru.it_lab.reaschedule;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class activity_account extends AppCompatActivity {

    public static final String DEBUG_TAG = "ACCOUNT_ACTIVITY_DEBUG";

    int memberID = 0;
    String memberName = "";
    String memberWho = "";
    TextView txtMemberName;
    TextView txtWeek;
    Map<Integer, String> membersGroups = new HashMap<Integer, String>();
    Map<Integer, String> membersLectors = new HashMap<Integer, String>();
    ProgressDialog dialog;
    String whoIsEmpty;
    Context thisContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        thisContext = this;

        if (!setupSharedPreferences())
            return;

        setHeaderText();
    }

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
        Intent intent = new Intent(this, loginActivity.class);
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
                    Toast.makeText(getApplicationContext(), "Невозможно установить интернет-соединение.", Toast.LENGTH_SHORT);
                else
                    Toast.makeText(getApplicationContext(), "Неверный ответ сервера. Попробуйте позже.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if (whoIsEmpty == "groups")
                    MemoryOperations.DBMembersSet(thisContext, membersGroups, MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_GROUPS);
                else if (whoIsEmpty == "lectors")
                    MemoryOperations.DBMembersSet(thisContext, membersGroups, MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_LECTORS);

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
                String whatToSearch = "";
                Log.i(DEBUG_TAG, "CHECHKED " + checkedId);

                switch (checkedId) {
                    case R.id.radBStudent:
                        whatToSearch = MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_GROUPS;
                        txtChangeID.setHint(getString(R.string.login1_student_placeholder));
                        txtChangeID.setText("");
                        members = membersGroups;
                        break;
                    case R.id.radBLector:
                        whatToSearch = MemoryOperations.ScheduleDBHelper.DATABASE_TABLE_LECTORS;
                        txtChangeID.setHint(getString(R.string.login1_lector_placeholder));
                        txtChangeID.setText("");
                        members = membersLectors;
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
}
