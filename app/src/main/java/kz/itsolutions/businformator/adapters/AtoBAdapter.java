package kz.itsolutions.businformator.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.HashMap;
import java.util.List;

import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.model.Route;
import kz.itsolutions.businformator.model.RouteStatistic;


/**
 * Created by jean on 3/18/2016.
 */
public class AtoBAdapter extends RecyclerView.Adapter<AtoBAdapter.ContactViewHolder>
        implements RecyclerView.OnItemTouchListener {

    Context context;
    Activity activity;
    List<Route> data;
    GestureDetector mGestureDetector;
    private OnItemClickListener listener;

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && listener != null && mGestureDetector.onTouchEvent(e)) {
            listener.onItemClick(childView, view.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface OnItemClickListener {

        void onItemClick(View childView, int childAdapterPosition);
    }

    public AtoBAdapter(List<Route> routes) {
        data = routes;
    }

    public AtoBAdapter(Activity context, List<Route> data,  OnItemClickListener listener) {
        this.context = context.getBaseContext();
        this.activity = context;
        this.listener = listener;
        this.data = data;

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.a_b_layout, parent, false);

        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {

        Route route = data.get(position);
        holder.foundRouteBtn.setText(String.valueOf(route.getNumber()));

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        protected Button foundRouteBtn;

        public ContactViewHolder(View itemView) {
            super(itemView);
            foundRouteBtn = (Button) itemView.findViewById(R.id.found_route_btn);

//            if (itemView.isSelected()){
//                foundRouteBtn.setBackgroundDrawable(itemView.getResources().getDrawable(R.drawable.rounded_red_button));
//            }else{
//                foundRouteBtn.setBackgroundDrawable(itemView.getResources().getDrawable(R.drawable.rounded_button));
//            }
        }

    }
}

