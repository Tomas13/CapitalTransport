package kz.itsolutions.astanabusinfo.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import kz.itsolutions.astanabusinfo.R;
import kz.itsolutions.astanabusinfo.db.DBHelper;
import kz.itsolutions.astanabusinfo.model.Route;
import kz.itsolutions.astanabusinfo.utils.AstanaBusRestClient;

import org.apache.http.Header;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ComplaintActivity extends Activity implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE = 2;

    String picturePath;
    Uri imageUri;

    ArrayAdapter mRouteAdapter;

    /* Views */
    EditText etEmail, etComplaintMessage;
    ImageView imgAttachment;
    ProgressDialog progressDialog;
    Spinner mSpinnerRoute;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complaint_activity);
        mSpinnerRoute = (Spinner) findViewById(R.id.spinner_routes);
        etComplaintMessage = (EditText) findViewById(R.id.et_complaint_message);
        etEmail = (EditText) findViewById(R.id.et_email);
        imgAttachment = (ImageView) findViewById(R.id.img_complaint_attachment);
        findViewById(R.id.btn_make_photo).setOnClickListener(this);
        findViewById(R.id.btn_choice_picture).setOnClickListener(this);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.make_complaint));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(getResources().getDrawable(R.drawable.ic_menu_bus));
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.loading));
        DBHelper dbHelper = DBHelper.getHelper();
        List<Route> parentStatuses = Route.getAllGroupByNumber(dbHelper);
        mRouteAdapter = new ArrayAdapter<Route>(this, android.R.layout.simple_list_item_1, parentStatuses);
        mSpinnerRoute.setAdapter(mRouteAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(Route.KEY_ROUTE_NUMBER)) {
                Route route = Route.getByNumber(dbHelper, intent.getIntExtra(Route.KEY_ROUTE_NUMBER, -1));
                int position = 0;
                for (int i = 0; i < parentStatuses.size(); i++) {
                    if (parentStatuses.get(i).getNumber() == route.getNumber()) {
                        position = i;
                        break;
                    }
                }
                mSpinnerRoute.setSelection(position);
            }
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
                sendComplaint();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_make_photo:
                dispatchTakePictureIntent();
                break;
            case R.id.btn_choice_picture:
                dispatchChoicePictureIntent();
                break;
        }
    }

    void sendComplaint() {
        String message = etComplaintMessage.getText().toString().trim();
        String email = etEmail.getText().toString();
        Route route = (Route) mSpinnerRoute.getSelectedItem();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, getString(R.string.need_complaint), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidEmail(email)) {
            Toast.makeText(this, getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        String url = "https://sapi.i-t.kz/astana/?city=astana&n=null&action=send_feedback_akimat";
        RequestParams params = new RequestParams();
        params.put("busid", 0);
        params.put("route_number", route != null ? route.getNumber() : -1);
        params.put("email", email);
        params.put("message", message);

        if (!TextUtils.isEmpty(picturePath)) {
            File myFile = new File(picturePath);
            try {
                params.put("pictures[]", myFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        AstanaBusRestClient.postWithFullUrl(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Toast.makeText(ComplaintActivity.this, getString(R.string.complaint_sent), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                finish();
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Bitmap bmp = null;
            if (requestCode == PICK_IMAGE) {
                if (data == null || data.getData() == null) return;
                Uri _uri = data.getData();
                //User had pick an image.
                Cursor cursor = getContentResolver().query(_uri, new String[]
                        {android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
                cursor.moveToFirst();

                //Link to the image
                picturePath = cursor.getString(0);
                cursor.close();

                FileInputStream in = null;
                try {
                    in = new FileInputStream(picturePath);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 10;
                    bmp = BitmapFactory.decodeStream(in, null, options);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                picturePath = getRealPathFromURI(imageUri);
                if (!TextUtils.isEmpty(picturePath)) {
                    FileInputStream in = null;
                    try {
                        in = new FileInputStream(picturePath);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 10;
                        bmp = BitmapFactory.decodeStream(in, null, options);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            if (bmp != null)
                imgAttachment.setImageBitmap(bmp);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getRealPathFromURI(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "Неудача :(", Toast.LENGTH_SHORT).show();
            return "";
        }
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    private void dispatchChoicePictureIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private void dispatchTakePictureIntent() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void start(Context c, Route route) {
        Intent intent = new Intent(c, ComplaintActivity.class);
        if (route != null) {
            intent.putExtra(Route.KEY_ROUTE_NUMBER, route.getNumber());
        }
        c.startActivity(intent);
    }
}