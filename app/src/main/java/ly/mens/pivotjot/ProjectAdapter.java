package ly.mens.pivotjot;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ly.mens.pivotjot.model.Project;

/**
 * Display the list of projects that a user has access to
 * Created by mensly on 3/05/2015.
 */
public class ProjectAdapter extends ArrayAdapter<Project> {
    private final List<Project> projects;

    public ProjectAdapter(Context context) {
        this(context, new ArrayList<Project>());
    }

    private ProjectAdapter(Context context, List<Project> list) {
        super(context, android.R.layout.simple_spinner_dropdown_item, list);
        this.projects = list;
        // TODO: Persist list of projects
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    public void replaceAll(Collection<Project> projects) {
        this.projects.clear();
        this.projects.addAll(projects);
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        super.clear();
        // TODO: Sync with persistent list
    }
}
