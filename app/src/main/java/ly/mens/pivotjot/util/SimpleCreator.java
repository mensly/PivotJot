package ly.mens.pivotjot.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;

/**
 * Simplifies implementing Parcelable using a constructor
 * Created by mensly on 3/05/2015.
 */
public class SimpleCreator<T> implements Parcelable.Creator<T> {
    private final Class<T> cls;
    private final Constructor<T> constructor;

    public SimpleCreator(Class<T> cls) {
        this.cls = cls;
        try {
            this.constructor = cls.getConstructor(Parcel.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    String.format("Class %s does not implement a constructor taking a Parcel as a single argument",
                            cls.getName()), e);
        }
    }

    @Override
    public T createFromParcel(Parcel source) {
        try {
            return constructor.newInstance(source);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred in Parcelable constructor", e);
        }
    }

    @Override
    public T[] newArray(int size) {
        return (T[]) Array.newInstance(cls, size);
    }
}
