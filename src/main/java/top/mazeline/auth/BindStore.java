package top.mazeline.auth;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class BindStore {
    private final File file;
    private final FileConfiguration config;

    public BindStore(MazeLineAuth plugin) {
        this.file = new File(plugin.getDataFolder(), "binds.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try { file.createNewFile(); } catch (IOException e) {
                plugin.getLogger().warning("创建 binds.yml 失败: " + e.getMessage());
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public String getEmail(UUID uuid) {
        return config.getString("binds." + uuid);
    }

    public void setEmail(UUID uuid, String email) {
        config.set("binds." + uuid, email);
        save();
    }

    public void remove(UUID uuid) {
        config.set("binds." + uuid, null);
        save();
    }

    private void save() {
        try { config.save(file); } catch (IOException e) {
            e.printStackTrace();
        }
    }
}