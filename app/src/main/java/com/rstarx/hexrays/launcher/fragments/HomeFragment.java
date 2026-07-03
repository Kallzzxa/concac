package com.rstarx.hexrays.launcher.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.rstarx.hexrays.launcher.util.SAMPServerInfo;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Obfuscate
public class HomeFragment extends Fragment {

    // Đường dẫn API hosted của bạn
    private static final String HOSTED_URL = "https://samp-mobile.shop/hosted.json";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ((MainActivity)getActivity()).hideKeyboard(getActivity());

        // Nút mạng xã hội
        ImageView yt_image = (ImageView) view.findViewById(R.id.youtube_logo);
        yt_image.setOnTouchListener(new ButtonAnimator(getContext(), yt_image));
        yt_image.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/x1y2z_yt"))));

        ImageView discord_image = (ImageView) view.findViewById(R.id.discord_logo);
        discord_image.setOnTouchListener(new ButtonAnimator(getContext(), discord_image));
        discord_image.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/jvKM7HR3Dc"))));

        ImageView internet_logo = (ImageView) view.findViewById(R.id.internet_button);
        internet_logo.setOnTouchListener(new ButtonAnimator(getContext(), internet_logo));
        internet_logo.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://samp-mobile.shop"))));

        // Nút THAM GIA GAME
        Button btnJoinGame = (Button) view.findViewById(R.id.btn_join_game);
        btnJoinGame.setOnTouchListener(new ButtonAnimator(getContext(), btnJoinGame));
        btnJoinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Đang tải danh sách máy chủ...", Toast.LENGTH_SHORT).show();
                
                // Chạy luồng phụ kết nối lấy danh sách server từ hosted.json
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final ArrayList<SAMPServerInfo> serverList = new ArrayList<>();
                        boolean success = false;

                        try {
                            URL url = new URL(HOSTED_URL);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(6000);
                            connection.setReadTimeout(6000);

                            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = in.readLine()) != null) {
                                    response.append(line);
                                }
                                in.close();

                                // Phân tích cấu trúc file hosted.json của bạn
                                JSONObject jsonObject = new JSONObject(response.toString());
                                JSONArray jsonArray = jsonObject.getJSONArray("query");
                                
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    SAMPServerInfo server = new SAMPServerInfo();
                                    server.setServerName(obj.getString("name"));
                                    server.setAddress(obj.getString("ip"));
                                    server.setPort(obj.getInt("port"));
                                    server.setCurrentPlayerCount(obj.getInt("online"));
                                    server.setMaxPlayerCount(obj.getInt("maxplayers"));
                                    serverList.add(server);
                                }
                                success = true;
                            }
                            connection.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        final boolean isSuccess = success;
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (isSuccess && !serverList.isEmpty()) {
                                        // Hiển thị danh sách Server cho người chơi chọn
                                        showServerSelectionDialog(serverList);
                                    } else {
                                        Toast.makeText(getContext(), "Không thể tải danh sách máy chủ. Vui lòng kiểm tra mạng!", Toast.LENGTH_SHORT).show();
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

    // Hàm hiển thị hộp thoại (Dialog) danh sách Server
    private void showServerSelectionDialog(final ArrayList<SAMPServerInfo> servers) {
        String[] serverNames = new String[servers.size()];
        for (int i = 0; i < servers.size(); i++) {
            SAMPServerInfo s = servers.get(i);
            // Định dạng hiển thị: Tên Server [Online/Max]
            serverNames[i] = s.getServerName() + " [" + s.getCurrentPlayerCount() + "/" + s.getMaxPlayerCount() + "]";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), AlertDialog.THEME_HOLO_DARK);
        builder.setTitle("CHỌN MÁY CHỦ THAM GIA");
        builder.setItems(serverNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Lấy thông tin server được chọn
                SAMPServerInfo selectedServer = servers.get(which);
                
                // Mở game chuyển tiếp IP và Port qua SAMP.class
                Intent intent = new Intent(getActivity(), SAMP.class);
                intent.putExtra("ip", selectedServer.getAddress());
                intent.putExtra("port", selectedServer.getPort());
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
