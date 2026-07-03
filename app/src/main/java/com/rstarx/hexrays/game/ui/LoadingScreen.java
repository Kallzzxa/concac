package com.rstarx.hexrays.game.ui;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView; // Thêm import thư viện TextView
import androidx.constraintlayout.widget.ConstraintLayout;
import com.rstarx.hexrays.R;

public class LoadingScreen {

    private Activity activity;
    private ConstraintLayout mainLayout;
    private ProgressBar progressBar;
    private TextView txtProgressNum; // Khai báo thêm biến TextView hiển thị số

    public LoadingScreen(Activity activity)
    {
        this.activity = activity;

        mainLayout = (ConstraintLayout) activity.getLayoutInflater()
                .inflate(R.layout.loadingscreen, null);

        progressBar = mainLayout.findViewById(R.id.progressBar2);
        txtProgressNum = mainLayout.findViewById(R.id.txt_progress_num); // Ánh xạ ID từ XML mới thêm

        activity.addContentView(mainLayout,
                new ConstraintLayout.LayoutParams(-1, -1));
    }

    public void setProgress(int value)
    {
        activity.runOnUiThread(() -> {
            if (progressBar != null) {
                progressBar.setProgress(value);
            }
            // Thêm logic cập nhật chữ số tăng dần theo phần trăm (0% -> 100%)
            if (txtProgressNum != null) {
                txtProgressNum.setText(value + "%");
            }
        });
    }

    public void show()
    {
        activity.runOnUiThread(() -> {
            if (mainLayout != null) {
                mainLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hide()
    {
        activity.runOnUiThread(() -> {
            if (mainLayout != null) {
                mainLayout.setVisibility(View.INVISIBLE);
            }
        });
    }
}
