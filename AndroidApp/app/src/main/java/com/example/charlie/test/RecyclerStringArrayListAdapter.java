package com.example.charlie.test;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerStringArrayListAdapter extends RecyclerView.Adapter<RecyclerStringArrayListAdapter.ViewHolder> {
    private ArrayList<String> mDataset;
    private ItemListFragment.OnListFragmentInteractionListener mListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you pro vide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView primaryStringView;
        public ViewHolder(View v) {
            super(v);
            primaryStringView = (TextView) v.findViewById(R.id.primary_string);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerStringArrayListAdapter(ArrayList<String> myDataset, ItemListFragment.OnListFragmentInteractionListener listener) {
        this.mDataset = myDataset;
        this.mListener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerStringArrayListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.string_list_row, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.primaryStringView.setText(mDataset.get(position));

        // Set on click listener
        holder.primaryStringView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(mDataset.get(position));
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
