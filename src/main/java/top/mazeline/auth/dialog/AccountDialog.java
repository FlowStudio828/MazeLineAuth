package top.mazeline.auth.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import top.mazeline.auth.LoginDialogListener;

import java.time.Duration;
import java.util.List;

public final class AccountDialog {

    private AccountDialog() {}

    private static final ClickCallback.Options CALLBACK_OPTIONS =
        ClickCallback.Options.builder().uses(1).lifetime(Duration.ofMinutes(10)).build();

    public static void showMain(Player player) {
        Dialog dialog = Dialog.create(factory -> factory.empty()
            .base(DialogBase.builder(Component.text("账号操作"))
                .body(List.of(DialogBody.plainMessage(Component.text("请选择操作类型"))))
                .canCloseWithEscape(false)
                .build())
            .type(DialogType.multiAction(List.of(
                ActionButton.builder(Component.text("绑定"))
                    .action(DialogAction.customClick((response, audience) -> {
                        if (audience instanceof Player p) showBind(p);
                    }, CALLBACK_OPTIONS)).build(),
                ActionButton.builder(Component.text("登录"))
                    .action(DialogAction.customClick((response, audience) -> {
                        if (audience instanceof Player p) showLogin(p);
                    }, CALLBACK_OPTIONS)).build(),
                ActionButton.builder(Component.text("关闭"))
                    .action(DialogAction.customClick((response, audience) -> {
                        if (audience instanceof Player p) {
                            LoginDialogListener.markClosed(p.getUniqueId());
                            try { p.closeDialog(); } catch (Throwable ignored) {}
                        }
                    }, CALLBACK_OPTIONS)).build()
            )).build())
        );
        player.showDialog(dialog);
    }

    public static void showBind(Player player) {
        Dialog dialog = Dialog.create(factory -> factory.empty()
            .base(DialogBase.builder(Component.text("绑定账号"))
                .body(List.of(DialogBody.plainMessage(Component.text("请输入邮箱和密码"))))
                .inputs(List.of(
                    DialogInput.text("email", Component.text("邮箱")).maxLength(100).build(),
                    DialogInput.text("password", Component.text("密码")).maxLength(50).build()
                ))
                .canCloseWithEscape(false)
                .build())
            .type(DialogType.confirmation(
                ActionButton.builder(Component.text("确认绑定"))
                    .action(DialogAction.customClick((response, audience) -> {
                        if (audience instanceof Player p) {
                            String email = response.getText("email");
                            String password = response.getText("password");
                            if (email != null && password != null) {
                                p.performCommand("bind " + email + " " + password);
                            }
                        }
                    }, CALLBACK_OPTIONS)).build(),
                ActionButton.builder(Component.text("返回")).build()
            ))
        );
        player.showDialog(dialog);
    }

    public static void showLogin(Player player) {
        Dialog dialog = Dialog.create(factory -> factory.empty()
            .base(DialogBase.builder(Component.text("登录账号"))
                .body(List.of(DialogBody.plainMessage(Component.text("请输入密码"))))
                .inputs(List.of(
                    DialogInput.text("password", Component.text("密码")).maxLength(50).build()
                ))
                .canCloseWithEscape(false)
                .build())
            .type(DialogType.confirmation(
                ActionButton.builder(Component.text("登录"))
                    .action(DialogAction.customClick((response, audience) -> {
                        if (audience instanceof Player p) {
                            String password = response.getText("password");
                            if (password != null) p.performCommand("login " + password);
                        }
                    }, CALLBACK_OPTIONS)).build(),
                ActionButton.builder(Component.text("返回")).build()
            ))
        );
        player.showDialog(dialog);
    }
}