package ly.mens.pivotjot;

import android.content.Context;
import android.widget.ArrayAdapter;

import ly.mens.pivotjot.model.Project;

/**
 * Display the list of projects that a user has access to
 * Created by mensly on 3/05/2015.
 */
public class ProjectAdapter extends ArrayAdapter<Project> {
    public ProjectAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).projectId;
    }
}
