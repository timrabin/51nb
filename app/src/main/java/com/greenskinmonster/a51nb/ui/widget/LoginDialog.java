package com.greenskinmonster.a51nb.ui.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.async.LoginHelper;
import com.greenskinmonster.a51nb.async.TaskHelper;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.ui.adapter.KeyValueArrayAdapter;
import com.greenskinmonster.a51nb.utils.Constants;
import com.greenskinmonster.a51nb.utils.UIUtils;

/**
 * dialog for login
 * Created by GreenSkinMonster on 2015-04-18.
 */
public class LoginDialog extends Dialog {

    private Context mCtx;
    private HiProgressDialog progressDialog;

    public LoginDialog(Context context) {
        super(context);
        mCtx = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_login, null);

        final EditText etUsername = (EditText) view.findViewById(R.id.login_username);
        final EditText etPassword = (EditText) view.findViewById(R.id.login_password);
        final Spinner spSecQuestion = (Spinner) view.findViewById(R.id.login_question);
        final EditText etSecAnswer = (EditText) view.findViewById(R.id.login_answer);

        final KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(mCtx, R.layout.spinner_row);
        adapter.setEntryValues(mCtx.getResources().getStringArray(R.array.pref_login_question_list_values));
        adapter.setEntries(mCtx.getResources().getStringArray(R.array.pref_login_question_list_titles));
        spSecQuestion.setAdapter(adapter);

        etUsername.setText(HiSettingsHelper.getInstance().getUsername());
        etPassword.setText(HiSettingsHelper.getInstance().getPassword());
        if (!TextUtils.isEmpty(HiSettingsHelper.getInstance().getSecQuestion())
                && TextUtils.isDigitsOnly(HiSettingsHelper.getInstance().getSecQuestion())) {
            int idx = Integer.parseInt(HiSettingsHelper.getInstance().getSecQuestion());
            if (idx > 0 && idx < adapter.getCount())
                spSecQuestion.setSelection(idx);
        }
        etSecAnswer.setText(HiSettingsHelper.getInstance().getSecAnswer());

        Button btnLogin = (Button) view.findViewById(R.id.login_btn);
        btnLogin.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                if (mCtx instanceof Activity)
                    UIUtils.hideSoftKeyboard((Activity) mCtx);

                HiSettingsHelper.getInstance().setUsername(etUsername.getText().toString());
                HiSettingsHelper.getInstance().setPassword(etPassword.getText().toString());
                HiSettingsHelper.getInstance().setSecQuestion(adapter.getEntryValue(spSecQuestion.getSelectedItemPosition()));
                HiSettingsHelper.getInstance().setSecAnswer(etSecAnswer.getText().toString());
                HiSettingsHelper.getInstance().setUid("");

                progressDialog = HiProgressDialog.show(mCtx, "正在登录...");

                final LoginHelper loginHelper = new LoginHelper();

                new AsyncTask<Void, Void, Integer>() {

                    @Override
                    protected Integer doInBackground(Void... voids) {
                        return loginHelper.login(true);
                    }

                    @Override
                    protected void onPostExecute(Integer result) {
                        if (result == Constants.STATUS_SUCCESS) {
                            UIUtils.toast("登录成功");
                            TaskHelper.runDailyTask(true);
                        } else {
                            UIUtils.toast(loginHelper.getErrorMsg());
                            HiSettingsHelper.getInstance().setUsername("");
                            HiSettingsHelper.getInstance().setPassword("");
                            HiSettingsHelper.getInstance().setSecQuestion("");
                            HiSettingsHelper.getInstance().setSecAnswer("");
                        }
                        progressDialog.dismiss();
                    }
                }.execute();
            }
        });

        setContentView(view);
    }

}
