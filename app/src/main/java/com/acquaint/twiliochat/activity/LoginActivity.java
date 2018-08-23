package com.acquaint.twiliochat.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;

import com.acquaint.twiliochat.R;


public class LoginActivity extends Activity implements View.OnClickListener {
    private EditText et_identity;
    private FloatingActionButton bt_join;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initWidgets();
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isWhitespace(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        et_identity.setFilters(new InputFilter[]{filter});
        initListeners();
    }

    private void initListeners() {
        bt_join.setOnClickListener(this);
    }

    private void initWidgets() {
        et_identity=findViewById(R.id.et_identity);
        bt_join=findViewById(R.id.bt_join);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_join:
                if(validation()){
                    Intent intent=new Intent(LoginActivity.this,SelectChannelActivity.class);
                    intent.putExtra("identity",et_identity.getText().toString().trim());
                    startActivity(intent);
                    et_identity.setText("");
                }
                break;
        }
    }

    private boolean validation() {
        if(et_identity.getText().toString().length()<=0){
            et_identity.setError(getString(R.string.er_identity));
            return false;
        }
        else {
            return true;
        }
    }
}
