package kz.itsolutions.businformator.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.model.News;

/**
 * Created by jean on 7/12/2016.
 */


public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {

    private List<News> newsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView date, message;

        public MyViewHolder(View view) {
            super(view);
            date = (TextView) view.findViewById(R.id.date);
            message = (TextView) view.findViewById(R.id.message);
        }
    }


    public NewsAdapter(List<News> newsList) {
        this.newsList = newsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_layout_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.date.setText(news.getDate());
        holder.message.setText(news.getMessage());
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }


    // Clean all elements of the recycler
    public void clear() {
        newsList.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<News> list) {
        newsList.addAll(list);
        notifyDataSetChanged();
    }

}