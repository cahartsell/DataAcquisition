package com.example.charlie.test;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerStringArrayListAdapter extends RecyclerView.Adapter<RecyclerStringArrayListAdapter.ViewHolder> {
    private ArrayList<String> mDataset;
    private ItemListFragment.OnListFragmentInteractionListener mListener;
    private List<View> mItemViewList;
    private List<Boolean> mItemSelectedList;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
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
        this.mItemViewList = new ArrayList<View>();
        this.mItemSelectedList = new ArrayList<Boolean>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerStringArrayListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view - Basic single string layout in this case
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.string_list_row, parent, false);

        // Add view to list
        // FIXME: Is the conditional check required or is this function only called once?
        if (!mItemViewList.contains(v)) {
            this.mItemViewList.add(v);
            this.mItemSelectedList.add(false);
        }
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.primaryStringView.setText(mDataset.get(position));

        // Set on click listener
        holder.primaryStringView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    Integer pos = holder.getAdapterPosition();

                    // Toggle item selected flag
                    mItemSelectedList.set(pos, !(mItemSelectedList.get(pos)));

                    // Set colors based on selected/unselected
                    if (mItemSelectedList.get(pos)) {
                        holder.primaryStringView.setBackgroundResource(R.color.selectedItemBackground);
                    } else {
                        holder.primaryStringView.setBackgroundResource(R.color.unselectedItemBackground);
                    }

                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(pos, mDataset.get(pos));
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
