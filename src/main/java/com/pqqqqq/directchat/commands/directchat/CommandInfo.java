package com.pqqqqq.directchat.commands.directchat;

import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.member.Member;
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
 * Created by Kevin on 2015-05-15.
 */
public class CommandInfo implements CommandExecutor {
    private DirectChat plugin;

    private CommandInfo(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandInfo(plugin)).setDescription(Texts.of(TextColors.AQUA, "Gets info about a player"))
                .setArguments(GenericArguments.string(Texts.of("PlayerName"))).build();
    }

    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if (!commandSource.hasPermission("directchat.info")) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
            return CommandResult.success();
        }

        String name = commandContext.<String> getOne("PlayerName").get();
        Member member = null;
        for (Member m : plugin.getMembers().getMap().values()) {
            if (m.getLastCachedUsername().equalsIgnoreCase(name)) {
                member = m;
                break;
            }
        }

        if (member == null) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Invalid player: ", TextColors.WHITE, name));
            return CommandResult.success();
        }

        commandSource.sendMessage(Texts.of(TextColors.AQUA, "Player: ", TextColors.WHITE, member.getLastCachedUsername()));
        commandSource.sendMessage(Texts.of(TextColors.AQUA, "UUID: ", TextColors.WHITE, member.getUuid()));
        commandSource.sendMessage(Texts.of(TextColors.AQUA, "Snooper: ", TextColors.WHITE, member.getSnooperData().toString()));

        if (member.getActive() != null) {
            commandSource.sendMessage(Texts.of(TextColors.AQUA, "Active channel: ", TextColors.WHITE, (member.getActive().isUndetectable() ? "*" : "") + member.getActive().toString()));
        }

        commandSource.sendMessage(Texts.of(TextColors.AQUA, "Channels: ", TextColors.WHITE, member.getChannels().getChannels().toString()));
        commandSource.sendMessage(Texts.of(TextColors.AQUA, "Extra Data: ", TextColors.WHITE, member.getExtraData().toString()));
        return CommandResult.success();
    }
}
