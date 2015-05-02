package ly.mens.pivotjot.util;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.lang.reflect.Array;

/**
 * Simplifies implementing Parcelable using Gson
 * Created by mensly on 3/05/2015.
 */
public class GsonCreator<T> implements Parcelable.Creator<T> {
    private static final Gson GSON = new Gson();
    private final Class<T> cls;

    public GsonCreator(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public T createFromParcel(Parcel source) {
        return GSON.fromJson(source.readString(), cls);
    }

    @Override
    public T[] newArray(int size) {
        return (T[]) Array.newInstance(cls, size);
    }

    public static void writeToParcel(Parcel dest, Object obj) {
        dest.writeString(GSON.toJson(obj));
    }
}
