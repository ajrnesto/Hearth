package com.hearth.authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.hearth.R;
import com.hearth.methods.MyMethods;

public class ResetPasswordActivity extends AppCompatActivity {

    FirebaseAuth auth;

    TextInputLayout tilEmail;
    TextInputEditText etEmail;
    MaterialButton btnReset, btnLogin;
    CircularProgressIndicator progressResetPassword;
    TextView tvResetTitle, tvResetMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        initializeVars();

        btnReset.setOnClickListener(view -> {
            resetPassword(view);
        });

        btnLogin.setOnClickListener(view -> {
            finish();
        });
    }

    private void resetPassword(View view) {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            MyMethods.Generate.warningDialog(ResetPasswordActivity.this, "Password reset failed", "Please enter your email address to recover your password.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            MyMethods.Generate.warningDialog(ResetPasswordActivity.this, "Invalid email", "Please enter a valid email address.");
            return;
        }

        startLoading(view);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                dismissLoading(view);
            }
            else {
                MyMethods.Generate.warningDialog(ResetPasswordActivity.this, "Unable to send reset instruction", "The email address you provided is not registered to Hearth.");
                resetVisibilities(view);
            }
        });
    }

    private void startLoading(View view) {
        progressResetPassword.setVisibility(View.VISIBLE);
        tvResetTitle.setVisibility(View.GONE);
        tvResetMessage.setVisibility(View.GONE);
        tilEmail.setVisibility(View.GONE);
        etEmail.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
        btnLogin.setVisibility(View.GONE);
        hideSoftKeyboard(view);
    }

    private void dismissLoading(View view) {
        progressResetPassword.setVisibility(View.GONE);
        tvResetTitle.setVisibility(View.VISIBLE);
        tvResetMessage.setVisibility(View.VISIBLE);
        tilEmail.setVisibility(View.GONE);
        etEmail.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
        btnLogin.setVisibility(View.VISIBLE);
        hideSoftKeyboard(view);
    }

    private void resetVisibilities(View view) {
        progressResetPassword.setVisibility(View.GONE);
        tvResetTitle.setVisibility(View.GONE);
        tvResetMessage.setVisibility(View.GONE);
        tilEmail.setVisibility(View.VISIBLE);
        etEmail.setVisibility(View.VISIBLE);
        btnReset.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.VISIBLE);
        hideSoftKeyboard(view);
    }

    private void hideSoftKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void initializeVars() {;
        tvResetTitle = findViewById(R.id.tvResetTitle);
        tvResetMessage = findViewById(R.id.tvResetMessage);
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        btnReset = findViewById(R.id.btnReset);
        btnLogin = findViewById(R.id.btnLogin);
        progressResetPassword = findViewById(R.id.progressResetPassword);

        auth = FirebaseAuth.getInstance();
    }
}