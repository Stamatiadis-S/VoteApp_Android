package com.example.pug.voteapp_android.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.util.ArrayList;
import java.util.List;

@Type("polls")
public class Poll implements Parcelable {

    @Id
    private String id;
    @Relationship(value = "creator")
    private User creator;
    private String visibility;
    private String mode;
    private String question;
    private List<Option> options = new ArrayList<>();
    private String expiration;
    private String timeUpdated;
    private String timeCreated;
    @JsonIgnore
    private boolean voted = false;

    public Poll() {
    }

    public Poll(String visibility, String mode, String question, List<Option> options, String expiration) {
        this.visibility = visibility;
        this.mode = mode;
        this.question = question;
        this.options = options;
        this.expiration = expiration;
    }

    public Poll(User creator, String visibility, String mode, String id, String question, String expiration, List<Option> options) {
        this.id = id;
        this.creator = creator;
        this.visibility = visibility;
        this.mode = mode;
        this.options = options;
        this.question = question;
        this.expiration = expiration;
    }

    public Poll(String id, User creator, String visibility, String mode, String question, List<Option> options, String expiration, String timeUpdated, String timeCreated) {
        this.id = id;
        this.creator = creator;
        this.visibility = visibility;
        this.mode = mode;
        this.question = question;
        this.options = options;
        this.expiration = expiration;
        this.timeUpdated = timeUpdated;
        this.timeCreated = timeCreated;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public User getCreator() {
        return creator;
    }
    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getVisibility() {
        return visibility;
    }
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getMode() {
        return mode;
    }
    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }

    public String getExpiration() {
        return expiration;
    }
    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public List<Option> getOptions() {
        return this.options;
    }
    public void setOptions(List<Option> options) {
        this.options = options;
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

    public boolean isVoted() {
        return voted;
    }
    public void setVoted(boolean voted) {
        this.voted = voted;
    }

    //Parcelable implementation
    protected Poll(Parcel in) {
        id = in.readString();
        creator = new User(in.readString());
        visibility = in.readString();
        mode = in.readString();
        question = in.readString();
        expiration = in.readString();
        in.readTypedList(options, Option.CREATOR);
        timeUpdated = in.readString();
        timeCreated = in.readString();
        voted = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(creator.getUsername());
        dest.writeString(visibility);
        dest.writeString(mode);
        dest.writeString(question);
        dest.writeString(expiration);
        dest.writeTypedList(options);
        dest.writeString(timeUpdated);
        dest.writeString(timeCreated);
        dest.writeInt(voted ? 1:0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Poll> CREATOR = new Creator<Poll>() {
        @Override
        public Poll createFromParcel(Parcel in) {
            return new Poll(in);
        }

        @Override
        public Poll[] newArray(int size) {
            return new Poll[size];
        }
    };
}
