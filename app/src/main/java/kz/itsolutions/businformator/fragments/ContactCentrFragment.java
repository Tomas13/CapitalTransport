package kz.itsolutions.businformator.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import kz.itsolutions.businformator.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactCentrFragment extends Fragment {


    public ContactCentrFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_contact_centr, container, false);

        LinearLayout phoneLinear = (LinearLayout) rootView.findViewById(R.id.phone_linear_layout);
        phoneLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+77172" + getResources().getString(R.string.phone_number)));
                startActivity(intent);
            }
        });


       /* LinearLayout whatsappLinear = (LinearLayout) rootView.findViewById(R.id.whatsapp_linear);
        whatsappLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
*/

        LinearLayout facebookLinear = (LinearLayout) rootView.findViewById(R.id.facebook_linear);
        facebookLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://" + getResources().getString(R.string.facebook)));
                startActivity(intent);
            }
        });
        return rootView;
    }

}
