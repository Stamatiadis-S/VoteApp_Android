package com.example.pug.voteapp_android.models;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("participations")
public class Participation {

    @Id
    private String id;
    @Relationship("poll")
    private Poll poll;
    @Relationship("user")
    private User user;
    private String timeUpdated;
    private String timeCreated;

    public Participation() {
    }

    public Participation(Poll poll) {
        this.poll = poll;
    }

    public Participation(Poll poll, User user) {
        this.poll = poll;
        this.user = user;
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

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
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
}
