package ly.mens.pivotjot.model;

import android.os.Parcel;
import android.os.Parcelable;

import ly.mens.pivotjot.util.GsonCreator;

/**
 * Represents a project on Pivotal Tracker
 * Created by mensly on 3/05/2015.
 */
public class Project implements Parcelable {
    public static final Creator<Project> CREATOR = new GsonCreator<>(Project.class);

    private int id;
    private String name;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        GsonCreator.writeToParcel(dest, this);
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
