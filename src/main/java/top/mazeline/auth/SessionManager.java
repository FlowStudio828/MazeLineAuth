package top.mazeline.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.Reader;
import java.io.Writer;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final JavaPlugin plugin;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File file;

    public static class Session {
        public int userId;
        public String username;
        public String email;
        public long loginAt;
    }

    public SessionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), Constants.SESSION_FILE);
        load();
    }

    private void load() {
        if (!file.exists()) return;
        try (Reader r = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, Session>>() {}.getType();
            Map<String, Session> loaded = gson.fromJson(r, type);
            if (loaded != null) sessions.putAll(loaded);
        } catch (Exception e) {
            plugin.getLogger().warning("加载 sessions.json 失败: " + e.getMessage());
        }
    }

    public void save() {
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            try (Writer w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                gson.toJson(sessions, w);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("保存 sessions.json 失败: " + e.getMessage());
        }
    }

    public boolean isLoggedIn(String uuid) {
        Session s = sessions.get(uuid);
        if (s == null) return false;
        if (System.currentTimeMillis() - s.loginAt > Constants.SESSION_EXPIRE_MS) {
            sessions.remove(uuid);
            save();
            return false;
        }
        return true;
    }

    public Session get(String uuid) { return sessions.get(uuid); }

    public void login(String uuid, int userId, String username, String email) {
        Session s = new Session();
        s.userId = userId;
        s.username = username;
        s.email = email;
        s.loginAt = System.currentTimeMillis();
        sessions.put(uuid, s);
        save();
    }

    public void logout(String uuid) {
        sessions.remove(uuid);
        save();
    }
}