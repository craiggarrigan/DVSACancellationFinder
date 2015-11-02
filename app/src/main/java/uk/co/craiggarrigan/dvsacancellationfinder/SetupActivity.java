package uk.co.craiggarrigan.dvsacancellationfinder;

import android.app.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;


/**
 * A login screen that offers login via email/password.
 */
public class SetupActivity extends Activity {

    // UI references.
    private EditText mLicenseNumberView;
    private EditText mApplicationRefNumberView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        // Set up the login form.
        mLicenseNumberView = (EditText) findViewById(R.id.license_number);

        mApplicationRefNumberView = (EditText) findViewById(R.id.application_ref_number);
        mApplicationRefNumberView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSaveButton = (Button) findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.setup_form_layout);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mLicenseNumberView.setError(null);
        mApplicationRefNumberView.setError(null);

        // Store values at the time of the login attempt.
        String licenseNumber = mLicenseNumberView.getText().toString();
        String applicationRefNumber = mApplicationRefNumberView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid license number.
        if (TextUtils.isEmpty(licenseNumber)) {
            mLicenseNumberView.setError(getString(R.string.error_field_required));
            focusView = mLicenseNumberView;
            cancel = true;
        } else if (!isLicenseNumberValid(licenseNumber)) {
            mLicenseNumberView.setError(getString(R.string.error_invalid_license_number));
            focusView = mLicenseNumberView;
            cancel = true;
        }

        // Check for a valid application ref number.
        if (TextUtils.isEmpty(applicationRefNumber)) {
            mApplicationRefNumberView.setError(getString(R.string.error_field_required));
            focusView = mApplicationRefNumberView;
            cancel = true;
        } else if (!isApplicationRefNumberValid(applicationRefNumber)){
            mApplicationRefNumberView.setError(getString(R.string.error_invalid_application_ref_number));
            focusView = mApplicationRefNumberView;
            cancel = true;
        }

        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE);

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            // Clear prefs
            prefs.edit().clear().commit();
        } else {
            prefs
                    .edit()
                    .putString(getString(R.string.prefs_licenseNumber), licenseNumber)
                    .putString(getString(R.string.prefs_applicationRefNumber), applicationRefNumber)
                    .putBoolean(getString(R.string.prefs_isSetupComplete), true)
                    .commit();
            finish();
        }
    }

    private boolean isLicenseNumberValid(String licenseNumber) {
        return !TextUtils.isEmpty(licenseNumber);
    }

    private boolean isApplicationRefNumberValid(String applicationRefNumber) {
        return !TextUtils.isEmpty(applicationRefNumber) && TextUtils.isDigitsOnly(applicationRefNumber);
    }

}

