package de.mecrytv.proxyTools.utils;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.proxyTools.ProxyTools;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class GeneralUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static boolean isPlayer(CommandSource source) {
        return source instanceof Player;
    }

    public static void sendTranslated(SimpleCommand.Invocation invocation, String configKey, String... replacements) {
        var sm = ProxyTools.getInstance().getServiceManager();
        String message = sm.getMessages().getString(configKey);

        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        invocation.source().sendMessage(
                sm.getPrefix().append(miniMessage.deserialize(message))
        );
    }
}
