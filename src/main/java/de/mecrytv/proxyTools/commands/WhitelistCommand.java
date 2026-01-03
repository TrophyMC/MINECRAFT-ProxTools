package de.mecrytv.proxyTools.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.proxyTools.ProxyTools;
import de.mecrytv.proxyTools.models.mariadb.PlayerWhitelistCache;
import de.mecrytv.proxyTools.models.redis.PlayerWhitelistModel;
import de.mecrytv.proxyTools.utils.GeneralUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WhitelistCommand implements SimpleCommand {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm");

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!GeneralUtils.isPlayer(source)) {
            GeneralUtils.sendTranslated(invocation, "errors.only_players");
            return;
        }

        Player player = (Player) source;

        if (!player.hasPermission("proxytools.command.whitelist")){
            GeneralUtils.sendTranslated(invocation, "errors.no_permission");
            return;
        }

        if (args.length == 0) {
            List.of("header", "add", "remove", "info", "sync", "reload").forEach(key ->
                    GeneralUtils.sendTranslated(invocation, "commands.whitelist.help." + key)
            );
            return;
        }

        String action = args[0].toLowerCase();
        PlayerWhitelistCache cache = ProxyTools.getInstance().getServiceManager().getCacheService().getNode("player_whitelist_cache");

        switch (action) {
            case "info":
                List<PlayerWhitelistModel> allEntries = cache.getAll();

                if (allEntries.isEmpty()) {
                    GeneralUtils.sendTranslated(invocation, "commands.whitelist.info.empty_whitelist");
                    return;
                }

                GeneralUtils.sendTranslated(invocation, "commands.whitelist.info.header");
                for (PlayerWhitelistModel model: allEntries) {
                    if (model == null) continue;

                    String displayName = (model.getName() != null) ? model.getName() : "Unbekannt (" + model.getIdentifier().substring(0, 8) + ")";
                    String formattedDate = (model.getWhitelistedAt() != null) ? dateFormat.format(model.getWhitelistedAt()) : "??.??.????";

                    GeneralUtils.sendTranslated(invocation, "commands.whitelist.info.entry",
                            "{playerName}", displayName,
                            "{timestamp}", formattedDate
                    );
                }
                break;

            case "sync":
                GeneralUtils.sendTranslated(invocation, "commands.whitelist.sync.start");
                try {
                    List<PlayerWhitelistModel> syncedEntries = cache.getAllFromDatabase();

                    for (PlayerWhitelistModel model : cache.getAll()) {
                        cache.delete(model.getIdentifier());
                    }

                    for (PlayerWhitelistModel model: syncedEntries) {
                        cache.set(model);
                    }
                    GeneralUtils.sendTranslated(invocation, "commands.whitelist.sync.success");
                } catch (Exception e) {
                    GeneralUtils.sendTranslated(invocation, "commands.whitelist.sync.sync_error",
                            "{errorMessage}", e.getMessage()
                    );
                    return;
                }
                break;

            case "add":
                if (args.length < 2) {
                    GeneralUtils.sendTranslated(invocation, "commands.whitelist.add.usage");
                    return;
                }

                String targetName = args[1];
                Optional<Player> targetPlayer = ProxyTools.getInstance().getServer().getPlayer(targetName);

                if (targetPlayer.isEmpty()) {
                    GeneralUtils.sendTranslated(invocation, "errors.add.not_found",
                            "{playerName}", targetName
                    );
                    return;
                }

                Player addPlayer = targetPlayer.get();
                String playerId = addPlayer.getUniqueId().toString();

                if (cache.get(playerId) != null) {
                    GeneralUtils.sendTranslated(invocation, "commands.whitelist.add.already_exists",
                            "{playerName}", targetName
                    );
                    return;
                }

                PlayerWhitelistModel model = new PlayerWhitelistModel(playerId, addPlayer.getUsername(), new Timestamp(System.currentTimeMillis()));
                cache.set(model);

                GeneralUtils.sendTranslated(invocation, "commands.whitelist.add.success",
                        "{playerName}", targetName
                );
                break;

            case "remove":
                if (args.length < 2) {
                    GeneralUtils.sendTranslated(invocation, "commands.whitelist.remove.usage");
                    return;
                }

                String remPlayer = args[1];
                Optional<Player> remTargetPlayer = ProxyTools.getInstance().getServer().getPlayer(remPlayer);

                String removePlayer = remTargetPlayer.map(delPlayer -> delPlayer.getUniqueId().toString()).orElse(remPlayer);
                if (cache.get(removePlayer) == null) {
                    GeneralUtils.sendTranslated(invocation, "commands.whitelist.remove.not_found",
                            "{playerName}", remPlayer
                    );
                    return;
                }
                cache.delete(removePlayer);
                GeneralUtils.sendTranslated(invocation, "commands.whitelist.remove.success",
                        "{playerName}", remPlayer
                );
                break;

            case "reload":
                ProxyTools.getInstance().getServiceManager().reload();
                GeneralUtils.sendTranslated(invocation, "commands.whitelist.reload.success");
                break;

            default:
                List.of("header", "add", "remove", "info", "sync", "reload").forEach(key ->
                        GeneralUtils.sendTranslated(invocation, "commands.whitelist.help." + key)
                );
                break;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            String search = (args.length == 1) ? args[0].toLowerCase() : "";
            return List.of("add", "remove", "info", "sync", "reload").stream()
                    .filter(s -> s.startsWith(search)).collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            String search = args[1].toLowerCase();
            return ProxyTools.getInstance().getServer().getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(search)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}