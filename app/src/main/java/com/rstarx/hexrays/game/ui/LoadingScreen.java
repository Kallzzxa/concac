package com.rstarx.hexrays.game.ui;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.rstarx.hexrays.R;

public class LoadingScreen {

    private Activity activity;
    private ConstraintLayout mainLayout;
    private ProgressBar progressBar;
    private TextView txtProgressNum;

    public LoadingScreen(Activity activity) {
        this.activity = activity;

        mainLayout = (ConstraintLayout) activity.getLayoutInflater()
                .inflate(R.layout.loadingscreen, null);

        progressBar = mainLayout.findViewById(R.id.progressBar2);
        txtProgressNum = mainLayout.findViewById(R.id.txt_progress_num); // ID giờ đã trùng khớp với XML

        activity.addContentView(mainLayout,
                new ConstraintLayout.LayoutParams(-1, -1));
    }

    public void setProgress(int value) {
        activity.runOnUiThread(() -> {
            if (progressBar != null) {
                progressBar.setProgress(value);
            }
            if (txtProgressNum != null) {
                txtProgressNum.setText(value + "%");
            }
        });
    }

    public void show() {
        activity.runOnUiThread(() -> {
            if (mainLayout != null) {
                mainLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hide() {
        activity.runOnUiThread(() -> {
            if (mainLayout != null) {
                mainLayout.setVisibility(View.INVISIBLE);
            }
        });
    }
}
