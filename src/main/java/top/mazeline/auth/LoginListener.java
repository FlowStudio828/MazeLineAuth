package top.mazeline.auth;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

public class LoginListener implements Listener {
    private final MazeLineAuth plugin;

    public LoginListener(MazeLineAuth plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (plugin.getSessionManager().isLoggedIn(p.getUniqueId().toString())) {
            unfreeze(p);
            return;
        }
        freeze(p);
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', Constants.MSG_NOT_LOGGED_IN));
        p.sendMessage(ChatColor.YELLOW + "还没绑定？使用 /bind <邮箱> <密码>");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getSessionManager().logout(e.getPlayer().getUniqueId().toString());
        LoginDialogListener.clearState(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent e) {
        if (!plugin.getSessionManager().isLoggedIn(e.getPlayer().getUniqueId().toString())) {
            Location from = e.getFrom();
            Location to = e.getTo();
            if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())) {
                e.setTo(from);
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getPlayer() instanceof Player p) {
            if (!plugin.getSessionManager().isLoggedIn(p.getUniqueId().toString())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (!plugin.getSessionManager().isLoggedIn(p.getUniqueId().toString())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!plugin.getSessionManager().isLoggedIn(e.getPlayer().getUniqueId().toString())) e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!plugin.getSessionManager().isLoggedIn(e.getPlayer().getUniqueId().toString())) e.setCancelled(true);
    }

    public static void freeze(Player p) {
        p.setWalkSpeed(0);
        p.setFlySpeed(0);
        p.setInvulnerable(true);
        p.setCollidable(false);
    }

    public static void unfreeze(Player p) {
        p.setWalkSpeed(0.2f);
        p.setFlySpeed(0.1f);
        p.setInvulnerable(false);
        p.setCollidable(true);
    }
}