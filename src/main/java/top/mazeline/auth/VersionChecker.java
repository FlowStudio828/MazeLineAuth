package top.mazeline.auth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class VersionChecker {
    private final MazeLineAuth plugin;
    private volatile boolean outdated = false;

    public VersionChecker(MazeLineAuth plugin) {
        this.plugin = plugin;
    }

    public void checkAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(Constants.VERSION_URL).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                int code = conn.getResponseCode();
                if (code != 200) {
                    plugin.getLogger().warning("版本检查失败，HTTP " + code);
                    return;
                }
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line.trim());
                }
                String remote = sb.toString().trim();
                String local = plugin.getDescription().getVersion();
                outdated = !remote.equals(local);
                if (outdated) {
                    plugin.getLogger().info("发现新版本：" + remote + "（当前 " + local + "）");
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                        plugin.getServer().getOnlinePlayers().forEach(p ->
                            p.sendMessage(org.bukkit.ChatColor.RED +
                                "MazeLineAuth 插件已有新版本 " + remote +
                                "，请前往 " + Constants.DOWNLOAD_URL + " 下载")));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("版本检查异常: " + e.getMessage());
            }
        });
    }

    public boolean isOutdated() { return outdated; }
}