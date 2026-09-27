package top.mazeline.auth;

public final class Constants {
    private Constants() {}

    public static final String API_URL        = "https://mazeline.top/mc/api.php";
    public static final String VERSION_URL    = "https://mazeline.top/mc/AuthV.txt";
    public static final String DOWNLOAD_URL   = "https://mazeline.top/mc/AuthDownload.html";
    public static final String REGISTER_URL   = "https://mazeline.top/login.php";

    public static final long   SESSION_EXPIRE_MS = 168L * 3600_000L;
    public static final String SESSION_FILE      = "sessions.json";

    public static final String MSG_NOT_LOGGED_IN = "&c请先登录";
    public static final String MSG_LOGIN_SUCCESS = "&a登录成功，欢迎回来！";
    public static final String MSG_LOGIN_FAIL    = "&c登录失败：%s";
    public static final String MSG_BIND_FAIL     = "&c绑定失败：%s";
    public static final String MSG_UNBIND_SUCCESS= "&a已解绑";
    public static final String MSG_UNBIND_FAIL   = "&c解绑失败：%s";
}