package kz.itsolutions.businformatordraft.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import kz.itsolutions.businformatordraft.R;

public class AboutAppActivity extends SherlockActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_acivity);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_launcher);
        actionBar.setDisplayHomeAsUpEnabled(true);
        try {
            ((TextView) findViewById(R.id.tv_version))
                    .setText(this.getPackageManager().getPackageInfo(
                            this.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

//        findViewById(R.id.btn_share).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent sendIntent = new Intent();
//                sendIntent.setAction(Intent.ACTION_SEND);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
//                sendIntent.setType("text/plain");
//                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share)));
//            }
//        });

//        View btnGetPaidVersion = findViewById(R.id.btn_get_paid_version);
//        btnGetPaidVersion.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String appPackageName = getPackageName() + "_paid";
//                try {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
//                } catch (android.content.ActivityNotFoundException anfe) {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
//                }
//            }
//        });
//        btnGetPaidVersion.setVisibility(Consts.IS_FREE ? View.VISIBLE : View.GONE);

//        findViewById(R.id.btn_vote).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String appPackageName = getPackageName();
//                try {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
//                } catch (android.content.ActivityNotFoundException anfe) {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
//                }
//            }
//        });

//        findViewById(R.id.btn_mail).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //startActivity(new Intent(AboutAppActivity.this, FeedbackActivity.class));
//                sendMail("astanabus01@gmail.com");
//            }
//        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendMail(String email) {
        final Intent emailIntent = new Intent(
                android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[]{email});
        String version = "";
        try {
            version = this.getPackageManager().getPackageInfo(
                    this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "AstanaBus для Android v. " + version);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        this.startActivity(Intent.createChooser(emailIntent, "Отправить через..."));
    }
}