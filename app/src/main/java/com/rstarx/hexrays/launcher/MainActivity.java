package com.rstarx.hexrays.launcher;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.joom.paranoid.Obfuscate;
import com.rstarx.hexrays.R;
import com.rstarx.hexrays.launcher.config.Config;
import com.rstarx.hexrays.launcher.fragments.HomeFragment;
import com.rstarx.hexrays.launcher.fragments.SettingsFragment;
import com.rstarx.hexrays.launcher.util.ConfigValidator;
import com.rstarx.hexrays.launcher.util.ViewPagerWithoutSwipe;

import java.io.File;

@Obfuscate
public class MainActivity extends AppCompatActivity {

    // Tạo biến toàn cục để các fragment bên trong có thể gọi lệnh chuyển tab
    public ViewPagerWithoutSwipe pa;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Config.currentContext = this;
        ConfigValidator.validateConfigFiles(this);

        File file = new File(getExternalFilesDir(null) + "/download/update.apk");
        if (file.exists()) {
            file.delete();
        }

        clearModelCache();

        FragmentManager fm = getSupportFragmentManager();
        ViewPagerAdapter sa = new ViewPagerAdapter(fm);
        
        // Ánh xạ ViewPager full màn hình
        pa = findViewById(R.id.fragment_place);
        pa.setAdapter(sa);

        // ĐÃ XÓA TOÀN BỘ: Code TabLayout và OnTabSelectedListener cũ tại đây để xóa bỏ thanh xám đen đáy
    }

    // Hàm hỗ trợ chuyển tab nhanh dành cho các nút bấm mới ở fragment giao diện chính
    public void changeTab(int position) {
        if (pa != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            pa.setCurrentItem(position, true);
        }
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View currentFocusedView = activity.getCurrentFocus();
            if (inputManager != null && currentFocusedView != null) {
                inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 1) return new SettingsFragment();
            return new HomeFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private void clearModelCache() {
        try {
            File file = new File(getExternalFilesDir(null).toString() + "/CINFO.BIN");
            if (file.exists()) file.delete();
            file = new File(getExternalFilesDir(null).toString() + "/models/MINFO.BIN");
            if (file.exists()) file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
