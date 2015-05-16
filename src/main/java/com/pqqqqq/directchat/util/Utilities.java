package com.pqqqqq.directchat.util;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.Config;
import com.pqqqqq.directchat.DirectChat;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Texts;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Kevin on 2015-05-03.
 */
public class Utilities {
    private static final Pattern URL_PATTERN = Pattern.compile("([^\\.\\,\\s]+?)([(\\.|\\,)]+?)([^\\.\\,\\s]+?)($|\\s)");

    @SuppressWarnings("deprecation")
    public static String formatColour(String str) {
        if (str == null) {
            return null;
        }

        return str.replaceAll("&([0-9a-fA-FkKlLmMnNoOrR])", Texts.getLegacyChar() + "$1");
    }

    @SuppressWarnings("deprecation")
    public static String unformatColour(String str) {
        if (str == null) {
            return null;
        }

        return str.replaceAll(Texts.getLegacyChar() + "([0-9a-fA-FkKlLmMnNoOrR])", "&$1");
    }

    public static boolean filter(Player player, String message) {
        // Swearing
        for (String swear : Config.blacklistedWords) {
            if (message.contains(swear)) { // TODO: Better recognition, this sucks ass
                player.sendMessage(Texts.of("Please don't swear."));
                return false;
            }
        }

        if (!player.hasPermission("directchat.bypass.url")) {
            main: for (String word : message.split(" ")) {
                if (isURL(word)) {
                    for (String whitelist : Config.whitelistedURLs) {
                        if (word.trim().toLowerCase().contains(whitelist.trim().toLowerCase())) {
                            continue main;
                        }
                    }

                    player.sendMessage(Texts.of("Please don't advertise."));
                    for (Player admin : DirectChat.game.getServer().getOnlinePlayers()) {
                        if (admin.hasPermission("directchat.see-ad")) {
                            admin.sendMessage(Texts.of(player.getName() + " advertised: " + word));
                        }
                    }

                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isURL(String message) {
        return URL_PATTERN.matcher(message).find();
    }

    // PEX
    public static Optional<String> getPEXOption(Player player, String option) {
        DirectChat plugin = DirectChat.plugin;
        if (!plugin.getPEX().isPresent()) {
            return Optional.of("");
        }

        String uuid = player.getUniqueId().toString();

        ninja.leaping.permissionsex.sponge.PermissionsExPlugin pex = (ninja.leaping.permissionsex.sponge.PermissionsExPlugin) plugin.getPEX().get().getInstance();
        ninja.leaping.permissionsex.sponge.PEXSubject sub = pex.getUserSubjects().get(uuid);

        Optional<String> opop = sub.getOption(option);

        List<Subject> subjects = sub.getParents();
        ninja.leaping.permissionsex.sponge.PEXSubject parent = (subjects.isEmpty() ? null : (ninja.leaping.permissionsex.sponge.PEXSubject) subjects.get(0));

        if (!opop.isPresent() && parent != null) {
            opop = parent.getOption(option);
        }

        return opop;
    }
}
