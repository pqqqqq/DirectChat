package com.pqqqqq.directchat.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.channel.member.SnooperData;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
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
            commandSource.sendMessage(getErrorMessage("Player-only command"));
            return Optional.of(CommandResult.success());
        }

        Player player = (Player) commandSource;
        String type = s.trim();

        if (type.isEmpty()) {
            commandSource.sendMessage(getErrorMessage(getUsage(commandSource)));
            return Optional.of(CommandResult.success());
        }

        Member member = getPlugin().getMembers().getValue(player.getProfile().getUniqueId().toString());
        SnooperData snooper = member.getSnooperData();

        if (type.equalsIgnoreCase("private")) {
            if (!player.hasPermission("directchat.snooper.private")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions"));
                return Optional.of(CommandResult.success());
            }

            snooper.setPrivate(!snooper.isPrivate());
            player.sendMessage(getSuccessMessage("Private snooper: &f" + (snooper.isPrivate() ? "ON" : "OFF")));
        } else if (type.equalsIgnoreCase("public")) {
            if (!player.hasPermission("directchat.snooper.public")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions"));
                return Optional.of(CommandResult.success());
            }

            snooper.setPublic(!snooper.isPublic());
            player.sendMessage(getSuccessMessage("Public snooper: &f" + (snooper.isPublic() ? "ON" : "OFF")));
        } else if (type.equalsIgnoreCase("whisper")) {
            if (!player.hasPermission("directchat.snooper.whisper")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions"));
                return Optional.of(CommandResult.success());
            }

            snooper.setWhisper(!snooper.isWhisper());
            player.sendMessage(getSuccessMessage("Whisper snooper: &f" + (snooper.isWhisper() ? "ON" : "OFF")));
        } else {
            commandSource.sendMessage(getErrorMessage("Invalid type: &f" + type));
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
