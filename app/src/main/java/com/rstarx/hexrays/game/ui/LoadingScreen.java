package com.rstarx.hexrays.game.ui;

import android.app.Activity;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.rstarx.hexrays.R;

public class LoadingScreen {

    private Activity activity;
    private ConstraintLayout mainLayout;
    private ProgressBar progressBar;

    public LoadingScreen(Activity activity)
    {
        this.activity = activity;

        mainLayout = (ConstraintLayout) activity.getLayoutInflater()
                .inflate(R.layout.loadingscreen, null);

        progressBar = mainLayout.findViewById(R.id.progressBar2);

        activity.addContentView(mainLayout,
                new ConstraintLayout.LayoutParams(-1, -1));
    }

    public void setProgress(int value)
    {
        activity.runOnUiThread(() -> progressBar.setProgress(value));
    }

    public void hide()
    {
        mainLayout.setVisibility(View.INVISIBLE);
    }
}
