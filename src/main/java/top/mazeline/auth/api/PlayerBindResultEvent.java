package top.mazeline.auth.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBindResultEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final boolean success;
    private final String rawMessage;

    public PlayerBindResultEvent(Player player, boolean success, String rawMessage) {
        this.player = player;
        this.success = success;
        this.rawMessage = rawMessage;
    }

    public Player getPlayer() { return player; }
    public boolean isSuccess() { return success; }
    public String getRawMessage() { return rawMessage; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}