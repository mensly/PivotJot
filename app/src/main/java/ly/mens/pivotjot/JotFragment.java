package ly.mens.pivotjot;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * Interface to input text to create a Pivotal story
 * Created by mensly on 3/05/2015.
 */
public class JotFragment extends Fragment implements TextView.OnEditorActionListener {

    @InjectView(R.id.text)
    TextView titleText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: Add drop-down box of projects
        return inflater.inflate(R.layout.fragment_jot, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        titleText.setOnEditorActionListener(this);
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
        titleText.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager input = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                input.showSoftInput(titleText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 300);
        // Listen for relevant events from back end service
        IntentFilter filter = new IntentFilter();
        filter.addAction(PivotalService.BROADCAST_PROJECT_LIST);
        filter.addAction(PivotalService.BROADCAST_POST_SUCCESS);
        filter.addAction(PivotalService.BROADCAST_AUTH_ERROR);
        filter.addAction(PivotalService.BROADCAST_NETWORK_ERROR);
//        LocalBroadcastManager.getInstance(getActivity())
        getActivity()
                .registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
//        LocalBroadcastManager.getInstance(getActivity())
        getActivity()
                .unregisterReceiver(receiver);
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
        // TODO: Show loading UI
        // Submit to Pivotal via service
        PivotalService.Methods.postStory(getActivity(), 0, title);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                // Handle invalid data
                return;
            }
            switch (action) {
                case PivotalService.BROADCAST_AUTH_ERROR:
                    if (intent.getBooleanExtra(PivotalService.EXTRA_HAS_TOKEN, false)) {
                        // Error related to error in attempting a request
                        Toast.makeText(context, R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                    // TODO: Show login fragment
                    break;
                case PivotalService.BROADCAST_NETWORK_ERROR:
                    Toast.makeText(context, R.string.error_network, Toast.LENGTH_SHORT).show();
                    break;
                case PivotalService.BROADCAST_PROJECT_LIST:
                    // TODO: Display in drop-down box
                    break;
                case PivotalService.BROADCAST_POST_SUCCESS:
                    Toast.makeText(context, R.string.post_success, Toast.LENGTH_SHORT).show();
                    titleText.setText(null);
                    break;
            }
        }
    };
}
