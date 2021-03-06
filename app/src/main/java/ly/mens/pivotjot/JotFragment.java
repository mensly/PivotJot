package ly.mens.pivotjot;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import ly.mens.pivotjot.adp.EnumAdapter;
import ly.mens.pivotjot.adp.ProjectAdapter;
import ly.mens.pivotjot.model.Project;
import ly.mens.pivotjot.model.StoryType;


/**
 * Interface to input text to create a Pivotal story
 * Created by mensly on 3/05/2015.
 */
public class JotFragment extends Fragment implements TextView.OnEditorActionListener, TextWatcher {

    private static final String KEY_SELECTED_ID = "selected";
    private static final String KEY_STORY_TYPE = "type";
    private static final String KEY_DESCRIPTION = "description";

    @InjectView(R.id.text)
    TextView titleText;
    @InjectView(R.id.project)
    Spinner projectSpinner;
    @InjectView(R.id.btn_submit)
    Button submitButton;
    @InjectView(R.id.hint_description)
    View descriptionHint;
    @InjectView(R.id.type)
    Spinner typeSpinner;
    @InjectView(R.id.description)
    CheckBox includeDescription;
    @InjectViews({R.id.text, R.id.type, R.id.description, R.id.project})
    List<View> actionViews;

    private ProjectAdapter projectAdp;
    private float visibleHintAlpha;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jot, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        if (savedInstanceState == null) {
            visibleHintAlpha = descriptionHint.getAlpha();
        }
        Context context = getActivity();
        titleText.setOnEditorActionListener(this);
        titleText.addTextChangedListener(this);
        projectAdp = new ProjectAdapter(context);
        projectSpinner.setAdapter(projectAdp);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (projectAdp.getCount() > 0) {
            int selectedId = prefs.getInt(KEY_SELECTED_ID, 0);
            projectSpinner.setSelection(projectAdp.indexOf(selectedId));
        }
        EnumAdapter<StoryType> typeAdp = new EnumAdapter<>(context, StoryType.class);
        typeSpinner.setAdapter(typeAdp);
        String typeString = prefs.getString(KEY_STORY_TYPE, null);
        if (typeString != null) {
            try {
                StoryType type = Enum.valueOf(StoryType.class, typeString);
                typeSpinner.setSelection(Arrays.asList(StoryType.values()).indexOf(type));
            }
            catch (IllegalArgumentException e) {
                // Ignore if type does not exist
            }
        }
        includeDescription.setChecked(prefs.getBoolean(KEY_DESCRIPTION, true));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Display keyboard
        // Listen for relevant events from back end service
        IntentFilter filter = new IntentFilter();
        filter.addAction(PivotalService.BROADCAST_PROJECT_LIST);
        filter.addAction(PivotalService.BROADCAST_POST_SUCCESS);
        filter.addAction(PivotalService.BROADCAST_AUTH_ERROR);
        filter.addAction(PivotalService.BROADCAST_NETWORK_ERROR);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(receiver, filter);
        resume();
    }

    public void onLoggedIn() {
        projectAdp.clear();
        titleText.setText(null);
        resume();
    }

    private void resume() {
        PivotalService.listProject(getActivity());
        titleText.requestFocus();
        titleText.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (titleText != null) {
                    titleText.requestFocus();
                    InputMethodManager input = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    input.showSoftInput(titleText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 300);
        submitButton.setEnabled(projectAdp.getCount() > 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(receiver);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();

        Object selected = projectSpinner.getSelectedItem();
        if (selected instanceof Project) {
            editor.putInt(KEY_SELECTED_ID, ((Project) selected).getId());
        }
        editor.putString(KEY_STORY_TYPE, typeSpinner.getSelectedItem().toString());
        editor.putBoolean(KEY_DESCRIPTION, includeDescription.isChecked());
        editor.apply();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        submitStory();
        return true;
    }

    @OnClick(R.id.btn_submit)
    public void submitStory() {
        // Handle invalid data
        String title = titleText.getText().toString();
        if (title.length() == 0) {
            Toast.makeText(getActivity(), R.string.error_no_story, Toast.LENGTH_SHORT).show();
            return;
        }
        Object selected = projectSpinner.getSelectedItem();
        if (!(selected instanceof Project)) {
            Toast.makeText(getActivity(), R.string.error_no_project, Toast.LENGTH_SHORT).show();
            return;
        }
        Object itemType = typeSpinner.getSelectedItem();
        if (!(itemType instanceof StoryType)) {
            Toast.makeText(getActivity(), R.string.error_no_type, Toast.LENGTH_SHORT).show();
            return;
        }
        // Submit to Pivotal via service
        for (View actionView : actionViews) {
            actionView.setEnabled(false);
        }
        submitButton.setEnabled(false);
        String description = null;
        int splitTitle = title.indexOf(':');
        if (0 < splitTitle && splitTitle < title.length() - 1) {
            description = title.substring(splitTitle + 1, title.length());
            title = title.substring(0, splitTitle);
        }
        PivotalService.postStory(getActivity(), ((Project) selected).getId(), title,
                (StoryType)itemType, description, includeDescription.isChecked());

    }

    void showError(int errorText) {
        if (getActivity() != null && getFragmentManager().findFragmentById(R.id.fragment_overlay) == null) {
            // Only show errors when no overlay
            Toast.makeText(getActivity(), errorText, Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                // Handle invalid data
                return;
            }
            for (View actionView : actionViews) {
                actionView.setEnabled(true);
            }
            switch (action) {
                case PivotalService.BROADCAST_AUTH_ERROR:
                    if (intent.getBooleanExtra(PivotalService.EXTRA_HAS_TOKEN, false)) {
                        // Error related to error in attempting a request
                        showError(R.string.error_auth);
                    }
                    submitButton.setEnabled(projectAdp.getCount() > 0);
                    Activity activity = getActivity();
                    if (activity instanceof MainActivity) {
                        ((MainActivity) activity).showLogin();
                    }
                    break;
                case PivotalService.BROADCAST_NETWORK_ERROR:
                    // Display network error message
                    showError(R.string.error_network);
                    submitButton.setEnabled(projectAdp.getCount() > 0);
                    break;
                case PivotalService.BROADCAST_PROJECT_LIST:
                    // Update list of available projects
                    List<Project> projects = intent.getParcelableArrayListExtra(PivotalService.EXTRA_PROJECTS);
                    projectAdp.replaceAll(projects);
                    projectSpinner.setEnabled(projects.size() > 1);
                    submitButton.setEnabled(projectAdp.getCount() > 0);
                    break;
                case PivotalService.BROADCAST_POST_SUCCESS:
                    // Refresh the UI so the user can enter a new story
                    Toast.makeText(context, R.string.post_success, Toast.LENGTH_SHORT).show();
                    titleText.setText(null);
                    submitButton.setEnabled(projectAdp.getCount() > 0);
                    break;
            }
        }
    };

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable.length() == 0) {
            if (!descriptionHint.isEnabled()) {
                descriptionHint.setEnabled(true);
                descriptionHint.animate()
                        .alpha(visibleHintAlpha)
                        .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
                        .start();
            }
        }
        else if (descriptionHint.isEnabled()) {
            descriptionHint.setEnabled(false);
            descriptionHint.animate()
                    .alpha(0f)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
                    .start();
        }
    }
}
