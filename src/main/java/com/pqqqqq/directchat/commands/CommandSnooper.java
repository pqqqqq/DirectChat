package com.pqqqqq.directchat.commands;

import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.channel.member.SnooperData;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.args.GenericArguments;
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;

/**
 * Created by Kevin on 2015-05-04.
 */
public class CommandSnooper implements CommandExecutor {
    private DirectChat plugin;

    private CommandSnooper(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandSnooper(plugin)).setDescription(Texts.of(TextColors.AQUA, "Toggles snooper actions."))
                .setArguments(GenericArguments.string(Texts.of("SnooperType"))).build();
    }

    public CommandResult execute(CommandSource commandSource, CommandContext arguments) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return CommandResult.success();
        }

        Player player = (Player) commandSource;
        String type = arguments.<String> getOne("SnooperType").get();

        Member member = plugin.getMembers().getValue(player.getProfile().getUniqueId().toString());
        SnooperData snooper = member.getSnooperData();

        if (type.equalsIgnoreCase("private")) {
            if (!player.hasPermission("directchat.snooper.private")) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
                return CommandResult.success();
            }

            snooper.setPrivate(!snooper.isPrivate());
            commandSource.sendMessage(Texts.of(TextColors.GREEN, "Private snooper: ", TextColors.WHITE, (snooper.isPrivate() ? "ON" : "OFF")));
        } else if (type.equalsIgnoreCase("public")) {
            if (!player.hasPermission("directchat.snooper.public")) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
                return CommandResult.success();
            }

            snooper.setPublic(!snooper.isPublic());
            commandSource.sendMessage(Texts.of(TextColors.GREEN, "Public snooper: ", TextColors.WHITE, (snooper.isPublic() ? "ON" : "OFF")));
        } else if (type.equalsIgnoreCase("whisper")) {
            if (!player.hasPermission("directchat.snooper.whisper")) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
                return CommandResult.success();
            }

            snooper.setWhisper(!snooper.isWhisper());
            commandSource.sendMessage(Texts.of(TextColors.GREEN, "Whisper snooper: ", TextColors.WHITE, (snooper.isWhisper() ? "ON" : "OFF")));
        } else {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Invalid type: ", TextColors.WHITE, type));
        }
        return CommandResult.success();
    }
}
