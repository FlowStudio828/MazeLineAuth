package top.mazeline.auth;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.mazeline.auth.api.PlayerBindResultEvent;
import top.mazeline.auth.api.PlayerLoginResultEvent;
import top.mazeline.auth.dialog.AccountDialog;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LoginDialogListener implements Listener {
    private static final Set<UUID> showing = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> closed  = ConcurrentHashMap.newKeySet();

    private final MazeLineAuth plugin;

    public LoginDialogListener(MazeLineAuth plugin) { this.plugin = plugin; }

    public static void markShown(UUID uuid) { showing.add(uuid); }
    public static void markClosed(UUID uuid) {
        showing.remove(uuid);
        closed.add(uuid);
    }
    public static void clearState(UUID uuid) {
        showing.remove(uuid);
        closed.remove(uuid);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        UUID id = p.getUniqueId();
        if (plugin.getSessionManager().isLoggedIn(id.toString())) return;
        if (closed.contains(id)) return;
        if (isBedrockPlayer(p)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (p.isOnline() && !showing.contains(id) && !closed.contains(id)) {
                    showing.add(id);
                    AccountDialog.showMain(p);
                }
            }, 20L);
        }
    }

    private boolean isBedrockPlayer(Player player) {
        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object api = apiClass.getMethod("getInstance").invoke(null);
            return (boolean) apiClass.getMethod("isFloodgatePlayer", UUID.class)
                    .invoke(api, player.getUniqueId());
        } catch (ClassNotFoundException e) {
            return player.getUniqueId().toString().startsWith("00000000");
        } catch (Exception e) {
            plugin.getLogger().warning("Floodgate 检测异常: " + e.getMessage());
            return player.getUniqueId().toString().startsWith("00000000");
        }
    }

    @EventHandler
    public void onLoginResult(PlayerLoginResultEvent event) {
        Player p = event.getPlayer();
        if (!p.isOnline()) return;
        if (event.isSuccess()) {
            clearState(p.getUniqueId());
            try { p.closeDialog(); } catch (Throwable ignored) {}
        }
        p.sendMessage(LegacyComponentSerializer.legacySection().deserialize(event.getRawMessage()));
    }

    @EventHandler
    public void onBindResult(PlayerBindResultEvent event) {
        Player p = event.getPlayer();
        if (!p.isOnline()) return;
        p.sendMessage(LegacyComponentSerializer.legacySection().deserialize(event.getRawMessage()));
    }
}