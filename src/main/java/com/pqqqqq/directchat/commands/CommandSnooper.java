package com.pqqqqq.directchat.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.channel.member.SnooperData;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

/**
 * Created by Kevin on 2015-05-04.
 */
public class CommandSnooper extends CommandBase {
    private static final Optional<Text> desc = Optional.<Text> of(Texts.of("Toggles the snooper function for an admin. Ability to see different channels."));
    private static final Optional<Text> help = Optional.<Text> of(Texts.of("Toggles the snooper function for an admin. Ability to see different channels."));

    public CommandSnooper(DirectChat plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource commandSource, String s) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return Optional.of(CommandResult.success());
        }

        Player player = (Player) commandSource;
        String type = s.trim();

        if (type.isEmpty()) {
            commandSource.sendMessage(Texts.of(TextColors.RED, getUsage(commandSource)));
            return Optional.of(CommandResult.success());
        }

        Member member = plugin.getMembers().getValue(player.getProfile().getUniqueId().toString());
        SnooperData snooper = member.getSnooperData();

        if (type.equalsIgnoreCase("private")) {
            if (!player.hasPermission("directchat.snooper.private")) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            snooper.setPrivate(!snooper.isPrivate());
            commandSource.sendMessage(Texts.of(TextColors.GREEN, "Private snooper: ", TextColors.WHITE, (snooper.isPrivate() ? "ON" : "OFF")));
        } else if (type.equalsIgnoreCase("public")) {
            if (!player.hasPermission("directchat.snooper.public")) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            snooper.setPublic(!snooper.isPublic());
            commandSource.sendMessage(Texts.of(TextColors.GREEN, "Public snooper: ", TextColors.WHITE, (snooper.isPublic() ? "ON" : "OFF")));
        } else if (type.equalsIgnoreCase("whisper")) {
            if (!player.hasPermission("directchat.snooper.whisper")) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            snooper.setWhisper(!snooper.isWhisper());
            commandSource.sendMessage(Texts.of(TextColors.GREEN, "Whisper snooper: ", TextColors.WHITE, (snooper.isWhisper() ? "ON" : "OFF")));
        } else {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Invalid type: ", TextColors.WHITE, type));
        }
        return Optional.of(CommandResult.success());
    }

    public Optional<Text> getShortDescription(CommandSource commandSource) {
        return desc;
    }

    public Optional<Text> getHelp(CommandSource commandSource) {
        return help;
    }

    public Text getUsage(CommandSource commandSource) {
        return Texts.of("/snooper <private|public|whisper>");
    }
}
