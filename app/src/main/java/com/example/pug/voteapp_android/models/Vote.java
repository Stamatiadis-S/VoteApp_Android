package com.example.pug.voteapp_android.models;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("votes")
public class Vote {

    @Id
    private String id;
    @Relationship("poll")
    private Poll poll;
    private String timeUpdated;
    private String timeCreated;
    //private List<Option> options = new ArrayList<>();

    public Vote() {
    }

    public Vote(Poll poll) {
        this.poll = poll;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Poll getPoll() {
        return poll;
    }
    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public String getTimeUpdated() {
        return timeUpdated;
    }
    public void setTimeUpdated(String timeUpdated) {
        this.timeUpdated = timeUpdated;
    }

    public String getTimeCreated() {
        return timeCreated;
    }
    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    /*public List<Option> getOptions() {
        return options;
    }
    public void setOptions(List<Option> options) {
        this.options = options;
    }*/
}
