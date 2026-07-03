package com.rstarx.hexrays.launcher.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    private static final String HOSTED_URL = "https://samp-mobile.shop/hosted.json";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideKeyboard(getActivity());
        }

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

        // Nút chuyển Tab nhanh
        ImageView btnTabHome = (ImageView) view.findViewById(R.id.btn_tab_home);
        if (btnTabHome != null) {
            btnTabHome.setOnTouchListener(new ButtonAnimator(getContext(), btnTabHome));
            btnTabHome.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).changeTab(0);
                }
            });
        }

        ImageView btnTabSettings = (ImageView) view.findViewById(R.id.btn_tab_settings);
        if (btnTabSettings != null) {
            btnTabSettings.setOnTouchListener(new ButtonAnimator(getContext(), btnTabSettings));
            btnTabSettings.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).changeTab(1);
                }
            });
        }

        // Nút THAM GIA GAME
        Button btnJoinGame = (Button) view.findViewById(R.id.btn_join_game);
        btnJoinGame.setOnTouchListener(new ButtonAnimator(getContext(), btnJoinGame));
        btnJoinGame.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đang tải danh sách máy chủ...", Toast.LENGTH_SHORT).show();
            
            new Thread(() -> {
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
                    getActivity().runOnUiThread(() -> {
                        if (isSuccess && !serverList.isEmpty()) {
                            showServerSelectionDialog(serverList);
                        } else {
                            Toast.makeText(getContext(), "Không thể tải danh sách máy chủ!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        });

        return view;
    }

    // Mở hộp thoại chọn danh sách server
    private void showServerSelectionDialog(final ArrayList<SAMPServerInfo> servers) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_server_selection);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        RecyclerView rvServers = dialog.findViewById(R.id.rv_servers);
        rvServers.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // ĐÃ SỬA: Khi click dòng Server, gọi hàm hiện Tab thông tin chi tiết thay vì vào thẳng game
        rvServers.setAdapter(new ServerAdapter(servers, selectedServer -> {
            dialog.dismiss();
            showServerDetailDialog(selectedServer);
        }));

        Button btnClose = dialog.findViewById(R.id.btn_close_dialog);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ĐÃ THÊM MỚI: Hàm hiển thị Tab cấu trúc chi tiết thông tin Server và kích hoạt nút chạy Game
    private void showServerDetailDialog(final SAMPServerInfo server) {
        Dialog detailDialog = new Dialog(getContext());
        detailDialog.setContentView(R.layout.dialog_server_detail);
        if (detailDialog.getWindow() != null) {
            detailDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvName = detailDialog.findViewById(R.id.det_server_name);
        TextView tvAddress = detailDialog.findViewById(R.id.det_address);
        TextView tvPlayers = detailDialog.findViewById(R.id.det_players);
        TextView tvPing = detailDialog.findViewById(R.id.det_ping);
        ImageView btnClose = detailDialog.findViewById(R.id.btn_close_detail);
        FloatingActionButton fabPlay = detailDialog.findViewById(R.id.fab_play_game);

        // Gán dữ liệu lên View
        tvName.setText(server.getServerName());
        tvAddress.setText(server.getAddress() + ":" + server.getPort());
        tvPlayers.setText(server.getCurrentPlayerCount() + " / " + server.getMaxPlayerCount());
        tvPing.setText("65ms"); // Vì API gốc hosted.json không trả về Ping, nên ta gán tượng trưng mặc định hoặc tính toán sau

        // Sự kiện đóng Tab thông tin
        btnClose.setOnClickListener(v -> detailDialog.dismiss());

        // Sự kiện bấm nút Play màu vàng hình tròn để khởi động game chuyển tiếp IP và Port
        fabPlay.setOnClickListener(v -> {
            detailDialog.dismiss();
            Intent intent = new Intent(getActivity(), SAMP.class);
            intent.putExtra("ip", server.getAddress());
            intent.putExtra("port", server.getPort());
            startActivity(intent);
        });

        detailDialog.show();
    }

    // Bộ tiếp hợp Adapter danh sách Server
    private static class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ViewHolder> {
        private final ArrayList<SAMPServerInfo> serverList;
        private final OnServerClickListener listener;

        public interface OnServerClickListener {
            void onServerClick(SAMPServerInfo server);
        }

        public ServerAdapter(ArrayList<SAMPServerInfo> serverList, OnServerClickListener listener) {
            this.serverList = serverList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_server, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SAMPServerInfo server = serverList.get(position);
            holder.tvName.setText(server.getServerName());
            holder.tvOnline.setText(server.getCurrentPlayerCount() + "/" + server.getMaxPlayerCount());
            holder.itemView.setOnClickListener(v -> listener.onServerClick(server));
        }

        @Override
        public int getItemCount() {
            return serverList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvOnline;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_server_name);
                tvOnline = itemView.findViewById(R.id.tv_online_count);
            }
        }
    }
}
