package de.mecrytv.proxyTools.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.proxyTools.ProxyTools;
import de.mecrytv.proxyTools.models.mariadb.PlayerWhitelistCache;
import de.mecrytv.proxyTools.models.mariadb.ServerMaintenanceCache;
import de.mecrytv.proxyTools.models.redis.ServerMaintenanceModel;
import de.mecrytv.proxyTools.utils.GeneralUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MaintenanceCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!GeneralUtils.isPlayer(source)) {
            GeneralUtils.sendTranslated(invocation, "errors.only_player");
            return;
        }

        Player player = (Player) source;

        if (!player.hasPermission("proxytools.command.maintenance")){
            GeneralUtils.sendTranslated(invocation, "errors.no_permission");
            return;
        }

        if (args.length == 0) {
            List.of("header", "on", "off", "reload").forEach(key ->
                    GeneralUtils.sendTranslated(invocation, "commands.maintenance.help." + key)
            );
            return;
        }

        String action = args[0].toLowerCase();
        ServerMaintenanceCache cache = ProxyTools.getInstance().getServiceManager().getCacheService().getNode("server_maintenance_cache");

        ServerMaintenanceModel currentStatus = cache.get("server_maintenance");
        boolean isCurrentlyActive = (currentStatus != null && currentStatus.isActive());

        switch (action) {
            case "on":
                if (isCurrentlyActive) {
                    GeneralUtils.sendTranslated(invocation, "commands.maintenance.on.already_on");
                    return;
                }

                String activatorUUID = player.getUniqueId().toString();
                ServerMaintenanceModel maintenanceOn = new ServerMaintenanceModel(true, activatorUUID);
                cache.set(maintenanceOn);
                GeneralUtils.sendTranslated(invocation, "commands.maintenance.on.success");

                PlayerWhitelistCache wlCache = ProxyTools.getInstance().getServiceManager().getCacheService().getNode("player_whitelist_cache");
                String kickMessage = ProxyTools.getInstance().getServiceManager().getMessages().getString("commands.maintenance.kick_message");

                Component kickComponent = MiniMessage.miniMessage().deserialize(kickMessage.replace("\\n", "\n"));

                for (Player onlinePlayer : ProxyTools.getInstance().getServer().getAllPlayers()) {
                    boolean hasBypass = onlinePlayer.hasPermission("proxytools.maintenance.bypass");
                    boolean isOnWhitelist = (wlCache.get(onlinePlayer.getUniqueId().toString()) != null);

                    if (!hasBypass && !isOnWhitelist) {
                        onlinePlayer.disconnect(kickComponent);
                    }
                }
                break;

            case "off":
                if (!isCurrentlyActive) {
                    GeneralUtils.sendTranslated(invocation, "commands.maintenance.off.already_off");
                    return;
                }

                String deactivatorUUID = player.getUniqueId().toString();
                ServerMaintenanceModel maintenanceOff = new ServerMaintenanceModel(false, deactivatorUUID);
                cache.set(maintenanceOff);
                GeneralUtils.sendTranslated(invocation, "commands.maintenance.off.success");
                break;

            case "reload":
                ProxyTools.getInstance().getServiceManager().getMessages().reload();
                ProxyTools.getInstance().getServiceManager().getModt().reload();
                GeneralUtils.sendTranslated(invocation, "commands.maintenance.reload.success");
                break;

            default:
                List.of("header", "on", "off", "reload").forEach(key ->
                        GeneralUtils.sendTranslated(invocation, "commands.maintenance.help." + key)
                );
                break;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            String search = (args.length == 1) ? args[0].toLowerCase() : "";
            return List.of("on", "off", "reload").stream()
                    .filter(s -> s.startsWith(search)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}