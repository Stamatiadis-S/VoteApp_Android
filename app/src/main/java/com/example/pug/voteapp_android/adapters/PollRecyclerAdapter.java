package com.example.pug.voteapp_android.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pug.voteapp_android.activities.PollDetailsActivity;
import com.example.pug.voteapp_android.models.Poll;
import com.example.pug.voteapp_android.R;

import java.util.ArrayList;
import java.util.List;

public class PollRecyclerAdapter extends RecyclerView.Adapter<PollRecyclerAdapter.PollViewHolder> {

    private List<Poll> pollList;

    public PollRecyclerAdapter() {
        pollList = new ArrayList<Poll>();
    }

    public List<Poll> getPollList() {
        return pollList;
    }

    public void setPollList(List<Poll> pollList) {
        this.pollList = pollList;
    }

    public void swapList(ArrayList<Poll> newPollList) {
        pollList.clear();
        pollList.addAll(newPollList);
        notifyDataSetChanged();
    }

    public void addList(ArrayList<Poll> morePolls) {
        pollList.addAll(morePolls);
        notifyDataSetChanged();
    }

    @Override
    public PollViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerlist_poll, parent, false);
        PollRecyclerAdapter.PollViewHolder viewHolder = new PollRecyclerAdapter.PollViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PollViewHolder holder, int position) {
        Poll poll = pollList.get(position);
        holder.poll = poll;
        holder.title.setText(poll.getQuestion());
        holder.creator.setText(poll.getCreator().getUsername());
        holder.mode.setText(poll.getMode());
        holder.expiration.setText(poll.getExpiration().toString());
    }

    @Override
    public int getItemCount() {
        return pollList == null ? 0 : pollList.size();
    }

    class PollViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Poll poll;
        private TextView title;
        private TextView creator;
        private TextView mode;
        private TextView expiration;

        public PollViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            this.title = (TextView) itemView.findViewById(R.id.poll_title);
            this.creator = (TextView) itemView.findViewById(R.id.poll_creator);
            this.mode = (TextView) itemView.findViewById(R.id.poll_mode);
            this.expiration = (TextView) itemView.findViewById(R.id.poll_expiration);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), PollDetailsActivity.class);
            intent.putExtra("poll", this.poll);
            v.getContext().startActivity(intent);
        }
    }
}
