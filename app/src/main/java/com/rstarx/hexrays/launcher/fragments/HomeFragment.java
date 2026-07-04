package com.rstarx.hexrays.launcher.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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

    private static final String HOSTED_URL = "https://kethrnensm.github.io/web/hosted.json";

    private Handler pingHandler;
    private Runnable pingRunnable;
    private int xValue = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideKeyboard(getActivity());
        }

        // Mạng xã hội
        ImageView yt_image = view.findViewById(R.id.youtube_logo);
        yt_image.setOnTouchListener(new ButtonAnimator(getContext(), yt_image));
        yt_image.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/x1y2z_yt"))));

        ImageView discord_image = view.findViewById(R.id.discord_logo);
        discord_image.setOnTouchListener(new ButtonAnimator(getContext(), discord_image));
        discord_image.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/jvKM7HR3Dc"))));

        ImageView internet_logo = view.findViewById(R.id.internet_button);
        internet_logo.setOnTouchListener(new ButtonAnimator(getContext(), internet_logo));
        internet_logo.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://samp-mobile.shop"))));

        // Tab chuyển vùng nhanh
        ImageView btnTabHome = view.findViewById(R.id.btn_tab_home);
        if (btnTabHome != null) {
            btnTabHome.setOnTouchListener(new ButtonAnimator(getContext(), btnTabHome));
            btnTabHome.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).changeTab(0);
                }
            });
        }

        ImageView btnTabSettings = view.findViewById(R.id.btn_tab_settings);
        if (btnTabSettings != null) {
            btnTabSettings.setOnTouchListener(new ButtonAnimator(getContext(), btnTabSettings));
            btnTabSettings.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).changeTab(1);
                }
            });
        }

        // Nút THAM GIA GAME
        Button btnJoinGame = view.findViewById(R.id.btn_join_game);
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

    private void showServerSelectionDialog(final ArrayList<SAMPServerInfo> servers) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_server_selection);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        RecyclerView rvServers = dialog.findViewById(R.id.rv_servers);
        rvServers.setLayoutManager(new LinearLayoutManager(getContext()));

        rvServers.setAdapter(new ServerAdapter(servers, selectedServer -> {
            dialog.dismiss();
            showServerDetailDialog(selectedServer);
        }));

        Button btnClose = dialog.findViewById(R.id.btn_close_dialog);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

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
        LineChart lineChart = detailDialog.findViewById(R.id.ping_line_chart);

        tvName.setText(server.getServerName());
        // ĐỒNG BỘ: address dài dùng singleLine + ellipsize trong XML, không cần wrap tay ở đây
        tvAddress.setText(server.getAddress() + ":" + server.getPort());
        tvPlayers.setText(server.getCurrentPlayerCount() + " / " + server.getMaxPlayerCount());

        // Setup định dạng LineChart mẫu
        ArrayList<Entry> entries = new ArrayList<>();
        LineDataSet dataSet = new LineDataSet(entries, "Ping (ms)");
        dataSet.setColor(Color.parseColor("#FFC933"));
        dataSet.setCircleColor(Color.parseColor("#FFC933"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.setBackgroundColor(Color.TRANSPARENT);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setDrawLabels(false);
        lineChart.getAxisLeft().setTextColor(Color.parseColor("#8A8F98"));
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisLeft().setGridColor(Color.parseColor("#22FFFFFF"));
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDrawBorders(false);
        lineChart.invalidate();

        xValue = 0;
        pingHandler = new Handler(Looper.getMainLooper());
        pingRunnable = new Runnable() {
            @Override
            public void run() {
                new Thread(() -> {
                    long pingResult = -1;
                    try {
                        String ipAddress = server.getAddress();
                        long startTime = System.currentTimeMillis();
                        Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -w 1 " + ipAddress);
                        int status = process.waitFor();
                        if (status == 0) {
                            pingResult = System.currentTimeMillis() - startTime;
                        }
                    } catch (Exception e) {
                        pingResult = -1;
                    }

                    final long finalPing = pingResult;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (finalPing >= 0) {
                                tvPing.setText(finalPing + " ms");
                                tvPing.setTextColor(Color.parseColor("#3DDC84"));

                                LineData data = lineChart.getData();
                                if (data != null) {
                                    data.addEntry(new Entry(xValue++, finalPing), 0);
                                    if (data.getEntryCount() > 15) {
                                        data.removeEntry(0, 0);
                                    }
                                    data.notifyDataChanged();
                                    lineChart.notifyDataSetChanged();
                                    lineChart.moveViewToX(data.getEntryCount());
                                    lineChart.invalidate();
                                }
                            } else {
                                tvPing.setText("Timeout");
                                tvPing.setTextColor(Color.parseColor("#FF5252"));
                            }
                        });
                    }
                }).start();
                pingHandler.postDelayed(this, 1000);
            }
        };
        pingHandler.post(pingRunnable);

        btnClose.setOnClickListener(v -> {
            stopPingTask();
            detailDialog.dismiss();
        });

        detailDialog.setOnDismissListener(dialogInterface -> stopPingTask());

        fabPlay.setOnClickListener(v -> {
            stopPingTask();
            detailDialog.dismiss();
            Intent intent = new Intent(getActivity(), SAMP.class);
            intent.putExtra("ip", server.getAddress());
            intent.putExtra("port", server.getPort());
            startActivity(intent);
        });

        detailDialog.show();
    }

    private void stopPingTask() {
        if (pingHandler != null && pingRunnable != null) {
            pingHandler.removeCallbacks(pingRunnable);
        }
    }

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
            // ĐỒNG BỘ: item_server.xml dùng id "tv_server_status", định dạng "hiện/tối đa"
            holder.tvStatus.setText(server.getCurrentPlayerCount() + " / " + server.getMaxPlayerCount());
            holder.itemView.setOnClickListener(v -> listener.onServerClick(server));
        }

        @Override
        public int getItemCount() {
            return serverList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvStatus;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_server_name);
                tvStatus = itemView.findViewById(R.id.tv_server_status);
            }
        }
    }
}
