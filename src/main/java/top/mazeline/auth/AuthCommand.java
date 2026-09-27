package top.mazeline.auth;

import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mazeline.auth.api.PlayerBindResultEvent;
import top.mazeline.auth.api.PlayerLoginResultEvent;

import java.util.HashMap;
import java.util.Map;

public class AuthCommand implements CommandExecutor {
    private final MazeLineAuth plugin;

    public AuthCommand(MazeLineAuth plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        String uuid = p.getUniqueId().toString();
        String name = cmd.getName().toLowerCase();
        switch (name) {
            case "login": {
                if (args.length < 1) { p.sendMessage(ChatColor.RED + "用法：/login <密码>"); return true; }
                String email = plugin.getBindStore().getEmail(p.getUniqueId());
                if (email == null) {
                    p.sendMessage(ChatColor.RED + "你还没有绑定账号，请先使用 /bind <邮箱> <密码>");
                    return true;
                }
                handleLogin(p, uuid, email, args[0]);
                return true;
            }
            case "bind": {
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "用法：/bind <邮箱> <密码> [验证码]");
                    return true;
                }
                String code = args.length >= 3 ? args[2] : null;
                handleBind(p, uuid, args[0], args[1], code);
                return true;
            }
            case "unbind": {
                if (args.length < 2) { p.sendMessage(ChatColor.RED + "用法：/unbind <邮箱> <密码>"); return true; }
                handleUnbind(p, uuid, args[0], args[1]);
                return true;
            }
        }
        return true;
    }

    private void handleLogin(Player p, String uuid, String email, String password) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        data.put("mc_uuid", uuid);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            JsonObject resp;
            try {
                resp = plugin.getApiClient().post("login", data);
            } catch (Exception e) {
                plugin.getLogger().warning("登录请求失败: " + e.getMessage());
                plugin.getServer().getScheduler().runTask(plugin, () ->
                    finishLogin(p, false, ChatColor.RED + "网络错误，请稍后再试", uuid));
                return;
            }
            if (resp == null || !resp.has("ok")) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                    finishLogin(p, false, ChatColor.RED + "服务器返回异常", uuid));
                return;
            }
            boolean ok = resp.get("ok").getAsBoolean();
            if (ok) {
                JsonObject d = resp.getAsJsonObject("data");
                int userId = d.get("user_id").getAsInt();
                String username = d.get("username").getAsString();
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getSessionManager().login(uuid, userId, username, email);
                    finishLogin(p, true, ChatColor.translateAlternateColorCodes('&',
                        Constants.MSG_LOGIN_SUCCESS), uuid);
                });
            } else {
                String err = resp.has("error") ? resp.get("error").getAsString() : "未知错误";
                String msg = (err.contains("不存在") || err.contains("未找到") || err.contains("not found"))
                    ? ChatColor.RED + "请前往 " + Constants.REGISTER_URL + " 注册账号"
                    : ChatColor.translateAlternateColorCodes('&',
                        String.format(Constants.MSG_LOGIN_FAIL, err));
                plugin.getServer().getScheduler().runTask(plugin, () ->
                    finishLogin(p, false, msg, uuid));
            }
        });
    }

    private void finishLogin(Player p, boolean success, String message, String uuid) {
        if (!p.isOnline()) return;
        p.sendMessage(message);
        if (success) {
            LoginListener.unfreeze(p);
            LoginDialogListener.clearState(p.getUniqueId());
            try { p.closeDialog(); } catch (Throwable ignored) {}
        }
        plugin.getServer().getPluginManager().callEvent(
            new PlayerLoginResultEvent(p, success, message));
    }

    private void handleBind(Player p, String uuid, String email, String password, String code) {
        if (code == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("password", password);
            data.put("mc_uuid", uuid);
            data.put("mc_name", p.getName());

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                JsonObject resp;
                try {
                    resp = plugin.getApiClient().post("bind_request", data);
                } catch (Exception e) {
                    plugin.getLogger().warning("绑定请求失败: " + e.getMessage());
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                        finishBind(p, false, ChatColor.RED + "网络错误，请稍后再试"));
                    return;
                }
                if (resp == null || !resp.has("ok")) {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                        finishBind(p, false, ChatColor.RED + "服务器返回异常"));
                    return;
                }
                boolean ok = resp.get("ok").getAsBoolean();
                if (ok) {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                        finishBind(p, true, ChatColor.translateAlternateColorCodes('&',
                            "&a验证码已发送到邮箱，请使用 /bind <邮箱> <密码> <验证码>")));
                } else {
                    String err = resp.has("error") ? resp.get("error").getAsString() : "未知错误";
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                        finishBind(p, false, ChatColor.RED + "绑定失败：" + err));
                }
            });
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("password", password);
            data.put("mc_uuid", uuid);
            data.put("code", code);

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                JsonObject resp;
                try {
                    resp = plugin.getApiClient().post("bind_verify", data);
                } catch (Exception e) {
                    plugin.getLogger().warning("验证请求失败: " + e.getMessage());
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                        finishBind(p, false, ChatColor.RED + "回调检查失败，验证码错误或已过期"));
                    return;
                }
                if (resp == null || !resp.has("ok")) {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                        finishBind(p, false, ChatColor.RED + "回调检查失败，验证码错误或已过期"));
                    return;
                }
                boolean ok = resp.get("ok").getAsBoolean();
                if (ok) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getBindStore().setEmail(p.getUniqueId(), email);
                        finishBind(p, true, ChatColor.translateAlternateColorCodes('&', "&a绑定成功"));
                    });
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                        finishBind(p, false, ChatColor.RED + "回调检查失败，验证码错误或已过期"));
                }
            });
        }
    }

    private void finishBind(Player p, boolean success, String message) {
        if (!p.isOnline()) return;
        p.sendMessage(message);
        plugin.getServer().getPluginManager().callEvent(
            new PlayerBindResultEvent(p, success, message));
    }

    private void handleUnbind(Player p, String uuid, String email, String password) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        data.put("mc_uuid", uuid);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            JsonObject resp;
            try {
                resp = plugin.getApiClient().post("unbind", data);
            } catch (Exception e) {
                plugin.getLogger().warning("解绑请求失败: " + e.getMessage());
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (p.isOnline()) p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format(Constants.MSG_UNBIND_FAIL, "网络错误")));
                });
                return;
            }
            if (resp == null || !resp.has("ok")) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (p.isOnline()) p.sendMessage(ChatColor.RED + "服务器返回异常");
                });
                return;
            }
            boolean ok = resp.get("ok").getAsBoolean();
            if (ok) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getSessionManager().logout(uuid);
                    plugin.getBindStore().remove(p.getUniqueId());
                    if (p.isOnline()) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Constants.MSG_UNBIND_SUCCESS));
                        LoginListener.freeze(p);
                    }
                });
            } else {
                String err = resp.has("error") ? resp.get("error").getAsString() : "未知错误";
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (p.isOnline()) p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format(Constants.MSG_UNBIND_FAIL, err)));
                });
            }
        });
    }
}