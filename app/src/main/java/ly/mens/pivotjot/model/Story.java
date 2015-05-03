package ly.mens.pivotjot.model;

import android.os.Parcel;
import android.os.Parcelable;

import ly.mens.pivotjot.util.GsonCreator;

/**
 * Used to create a story
 * Created by mensly on 3/05/2015.
 */
public class Story implements Parcelable {
    public static Creator<Story> CREATOR = new GsonCreator<>(Story.class);

    private String name;
    private String storyType;
    private String description;

    public Story(String name, StoryType storyType, String description) {
        this.name = name;
        this.storyType = storyType.name().toLowerCase();
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        GsonCreator.writeToParcel(dest, this);
    }
}
