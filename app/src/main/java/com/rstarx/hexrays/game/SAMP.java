package com.rstarx.hexrays.launcher.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.joom.paranoid.Obfuscate;
import com.rstarx.hexrays.R;
import com.rstarx.hexrays.game.SAMP;
import com.rstarx.hexrays.launcher.MainActivity;
import com.rstarx.hexrays.launcher.util.ButtonAnimator;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Obfuscate
public class HomeFragment extends Fragment {

    // Đường dẫn API URL của bạn (Thay bằng link thật của bạn, trả về dạng {"ip":"127.0.0.1", "port":7777})
    private static final String API_URL = "https://your-domain.com/api.json"; 

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ((MainActivity)getActivity()).hideKeyboard(getActivity());

        // Cấu hình nút Youtube
        ImageView yt_image = (ImageView) view.findViewById(R.id.youtube_logo);
        yt_image.setOnTouchListener(new ButtonAnimator(getContext(), yt_image));
        yt_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent link = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/x1y2z_yt"));
                startActivity(link);
            }
        });

        // Cấu hình nút Discord
        ImageView discord_image = (ImageView) view.findViewById(R.id.discord_logo);
        discord_image.setOnTouchListener(new ButtonAnimator(getContext(), discord_image));
        discord_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent link = new Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/jvKM7HR3Dc"));
                startActivity(link);
            }
        });

        // Cấu hình nút Website
        ImageView internet_logo = (ImageView) view.findViewById(R.id.internet_button);
        internet_logo.setOnTouchListener(new ButtonAnimator(getContext(), internet_logo));
        internet_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent link = new Intent(Intent.ACTION_VIEW, Uri.parse("https://samp-mobile.shop"));
                startActivity(link);
            }
        });

        // Cấu hình nút THAM GIA GAME kết nối Online API
        Button btnJoinGame = (Button) view.findViewById(R.id.btn_join_game);
        btnJoinGame.setOnTouchListener(new ButtonAnimator(getContext(), btnJoinGame));
        btnJoinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khởi chạy một luồng phụ (Thread) để tải dữ liệu từ internet, tránh đơ màn hình chính
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String fetchedIp = "127.0.0.1"; // Giá trị dự phòng nếu lỗi mạng
                        int fetchedPort = 7777;
                        boolean success = false;

                        try {
                            URL url = new URL(API_URL);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(5000); // Giới hạn thời gian kết nối 5s
                            connection.setReadTimeout(5000);

                            int responseCode = connection.getResponseCode();
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                                StringBuilder response = new StringBuilder();
                                String inputLine;

                                while ((inputLine = in.readLine()) != null) {
                                    response.append(inputLine);
                                }
                                in.close();

                                // Phân tích dữ liệu JSON nhận được từ Server
                                JSONObject jsonObject = new JSONObject(response.toString());
                                if (jsonObject.has("ip")) {
                                    fetchedIp = jsonObject.getString("ip");
                                }
                                if (jsonObject.has("port")) {
                                    fetchedPort = jsonObject.getInt("port");
                                }
                                success = true;
                            }
                            connection.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Quay trở lại Main UI Thread để chuyển màn hình sang Game hoặc thông báo lỗi
                        final String finalIp = fetchedIp;
                        final int finalPort = fetchedPort;
                        final boolean isSuccess = success;

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (isSuccess) {
                                        Intent intent = new Intent(getActivity(), SAMP.class);
                                        intent.putExtra("ip", finalIp);
                                        intent.putExtra("port", finalPort);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(getContext(), "Lỗi kết nối máy chủ API! Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        return view;
    }
}
