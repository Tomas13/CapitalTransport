package kz.itsolutions.businformator.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.osmdroid.ResourceProxy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.adapters.ImageAdapter;
import kz.itsolutions.businformator.widgets.ExpandableGridView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ComplaintsFragment extends Fragment {

    String imgDecodableString;

    Bitmap thumbnail;
    public static final int CAMERA_PIC_REQUEST = 5;
    public static final int RESULT_LOAD_IMAGE = 6;
    Button sendContactCentrBtn;
    LinearLayout attachPicBtn;
    ImageView image;
    File pic;
    EditText editTextComplaint;
    ExpandableGridView gridView;
    ArrayList<Bitmap> thumbsList;
    String complaintMessageString;
    ImageAdapter adapter;
    String[] listDialog = {"Галерея", "Камера"};

    public ComplaintsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_complaints, container, false);

        thumbsList = new ArrayList<>(4);


        gridView = (ExpandableGridView) rootView.findViewById(R.id.gridview);
        gridView.setExpanded(true);
        sendContactCentrBtn = (Button) rootView.findViewById(R.id.btn_send_to_contact_centr);
        editTextComplaint = (EditText) rootView.findViewById(R.id.edit_text_complaints);
        attachPicBtn = (LinearLayout) rootView.findViewById(R.id.ll_attach_pic);
        attachPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (thumbsList.size() < 4) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Выполнить действие с помощью:")
                            .setItems(listDialog, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    if (which == 0) {
                                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                        galleryIntent.setType("image/*");

                                        startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);

//
                                    } else {

                                        Intent cameraIntent = new Intent(
                                                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
                                    }
                                }
                            });

                    // Create the AlertDialog object and return it
                    builder.create();
                    builder.show();

                } else {
                    Snackbar.make(editTextComplaint, "Нельзя приложить больше четырех фото", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        sendContactCentrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Жалоба");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"574777@astanalrt.com"});
                intent.putExtra(Intent.EXTRA_TEXT, editTextComplaint.getText());
                intent.setType("plain/text");


                ArrayList<Uri> uris = new ArrayList<>();

                for (int i = 0; i < thumbsList.size(); i++) {

                    String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                            thumbsList.get(i), "title", null);

                    Uri screenshotUri = Uri.parse(path);
                    uris.add(screenshotUri);


                }

                intent.putExtra(Intent.EXTRA_STREAM, uris);
//                String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
//                        thumbnail, "title", null);
//                Uri screenshotUri = Uri.parse(path);
//                intent.putExtra(Intent.EXTRA_STREAM, screenshotUri);


//                ByteArrayOutputStream bs = new ByteArrayOutputStream();
//                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bs);


                //Log.d("URI@!@#!#!@##!", Uri.fromFile(pic).toString() + "   " + pic.exists());
//                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(pic));

//                i.setType("image/png");

                startActivity(Intent.createChooser(intent, "Отправить жалобу"));

            }
        });
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CAMERA_PIC_REQUEST:
                if (data == null) {
                    return;
                }

                thumbnail = (Bitmap) data.getExtras().get("data");
                thumbsList.add(thumbnail);

                adapter = new ImageAdapter(getContext(), thumbsList, new ImageAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View childView, int childAdapterPosition) {
                        //check if list of images is not empty, then delete
                        if (thumbsList.size() > 0) {
                            thumbsList.remove(childAdapterPosition);
                            adapter.notifyDataSetChanged();
                        }

                    }
                });
                gridView.setAdapter(adapter);

                return;

            case RESULT_LOAD_IMAGE:

                if (data == null) {
                    return;
                }

                try {

                    // Get the Image from data

                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    // Get the cursor
                    Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();
                    // Set the Image in ImageView after decoding the String

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;

                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    options.inDither = true;

                    int px = 300;
//                    Bitmap bitmap = BitmapFactory.decodeFile(imgDecodableString, options);
                    Bitmap bitmap = decodeSampledBitmapFromResource(imgDecodableString, px, px);
                    thumbsList.add(bitmap);

                    adapter = new ImageAdapter(getContext(), thumbsList, new ImageAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View childView, int childAdapterPosition) {
                            //check if list of images is not empty, then delete
                            if (thumbsList.size() > 0) {
                                thumbsList.remove(childAdapterPosition);
                                adapter.notifyDataSetChanged();
                            }

                        }
                    });

                    gridView.setAdapter(adapter);


                } catch (Exception e) {
                    Toast.makeText(getContext(), "Что-то пошло не так - " + e.getMessage(), Toast.LENGTH_LONG)
                            .show();

                    Log.d("Complaints", e.getMessage());

                }
                return;
        }
    }


    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {

        // Читаем с inJustDecodeBounds=true для определения размеров
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Вычисляем inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Читаем с использованием inSampleSize коэффициента
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        // Реальные размеры изображения
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Вычисляем наибольший inSampleSize, который будет кратным двум
            // и оставит полученные размеры больше, чем требуемые
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


}
