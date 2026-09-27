package top.mazeline.auth;

import org.bukkit.plugin.java.JavaPlugin;

public class MazeLineAuth extends JavaPlugin {
    private static MazeLineAuth instance;
    private SessionManager sessionManager;
    private ApiClient apiClient;
    private BindStore bindStore;
    private VersionChecker versionChecker;

    @Override
    public void onEnable() {
        instance = this;
        sessionManager = new SessionManager(this);
        apiClient = new ApiClient(this);
        bindStore = new BindStore(this);
        versionChecker = new VersionChecker(this);
        getCommand("login").setExecutor(new AuthCommand(this));
        getCommand("bind").setExecutor(new AuthCommand(this));
        getCommand("unbind").setExecutor(new AuthCommand(this));
        getServer().getPluginManager().registerEvents(new LoginListener(this), this);
        getServer().getPluginManager().registerEvents(new LoginDialogListener(this), this);
        getServer().getScheduler().runTaskLater(this, () -> versionChecker.checkAsync(), 40L);
        getLogger().info("MazeLineAuth 已启用");
    }

    @Override
    public void onDisable() {
        if (sessionManager != null) sessionManager.save();
    }

    public static MazeLineAuth getInstance() { return instance; }
    public SessionManager getSessionManager() { return sessionManager; }
    public ApiClient getApiClient() { return apiClient; }
    public BindStore getBindStore() { return bindStore; }
    public VersionChecker getVersionChecker() { return versionChecker; }
}