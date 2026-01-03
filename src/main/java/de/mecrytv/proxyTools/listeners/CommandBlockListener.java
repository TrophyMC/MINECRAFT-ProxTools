package de.mecrytv.proxyTools.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.proxyTools.ProxyTools;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

public class CommandBlockListener {

    @Subscribe
    public void onCommandBlock(CommandExecuteEvent event){
        if (!(event.getCommandSource() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("proxytools.commandblock.bypass")) {
            return;
        }

        List<String> blockedCommands = ProxyTools.getInstance().getServiceManager().getConfig().getStringList("blocked_commands");

        String command = event.getCommand().split(" ")[0].toLowerCase();

        for (String blocked : blockedCommands) {
            if (command.equalsIgnoreCase(blocked)) {
                event.setResult(CommandExecuteEvent.CommandResult.denied());

                String message = ProxyTools.getInstance().getServiceManager().getMessages().getString("errors.command_blocked");
                player.sendMessage(ProxyTools.getInstance().getServiceManager().getPrefix()
                        .append(MiniMessage.miniMessage().deserialize(message)));
                return;
            }
        }
    }
}
