package ly.mens.pivotjot.adp;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.Arrays;

/**
 * Allow the user to choose from an enumerated type
 * Created by mensly on 3/05/2015.
 */
public class EnumAdapter<T extends Enum> extends ArrayAdapter<T> {

    public EnumAdapter(Context context, Class<T> cls) {
        super(context, android.R.layout.simple_spinner_dropdown_item);
        addAll(Arrays.asList(cls.getEnumConstants()));
    }

}
