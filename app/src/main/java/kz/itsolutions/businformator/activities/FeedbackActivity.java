package kz.itsolutions.businformator.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import kz.itsolutions.businformator.R;

public class FeedbackActivity extends Activity {

    /* Views */
    EditText etEmail, etFeedbackMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_activity);
        etFeedbackMessage = (EditText) findViewById(R.id.et_message);
        etEmail = (EditText) findViewById(R.id.et_email);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.feedback));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(getResources().getDrawable(R.drawable.ic_menu_bus));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.complaint_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.send:
                sendFeedback();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void sendFeedback() {
        if (TextUtils.isEmpty(etFeedbackMessage.getText().toString().trim())) {
            Toast.makeText(this, getString(R.string.need_complaint), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ComplaintActivity.isValidEmail(etEmail.getText().toString())) {
            Toast.makeText(this, getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
        }
    }
}