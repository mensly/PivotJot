package ly.mens.pivotjot;

import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ly.mens.pivotjot.model.Project;

/**
 * Display the list of projects that a user has access to
 * Created by mensly on 3/05/2015.
 */
public class ProjectAdapter extends ArrayAdapter<Project> {
    private static final String KEY_PROJECTS = "projects";
    private static final Gson GSON = new Gson();

    private final List<Project> projects;

    public ProjectAdapter(Context context) {
        this(context, new ArrayList<Project>());
    }

    private ProjectAdapter(Context context, List<Project> list) {
        super(context, android.R.layout.simple_spinner_dropdown_item, list);
        this.projects = list;
        String projectsJson = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_PROJECTS, "[]");
        try {

            Type listType = new TypeToken<List<Project>>() {}.getType();
            List<Project> projects = GSON.fromJson(projectsJson, listType);
            this.projects.addAll(projects);
        }
        catch (JsonParseException e) {
            // Ignore json parsing errors
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    public void replaceAll(Collection<Project> projects) {
        this.projects.clear();
        this.projects.addAll(projects);
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putString(KEY_PROJECTS, GSON.toJson(this.projects))
                .apply();
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        super.clear();
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .remove(KEY_PROJECTS)
                .apply();
    }

    public int indexOf(int projectId) {
        int index = 0;
        for (Project project : projects) {
            if (project.getId() == projectId) {
                return index;
            }
            index++;
        }
        return 0; // Not found
    }
}
