package com.pqqqqq.directchat.commands;

import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.member.Member;
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

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kevin on 2015-05-06.
 */
public class CommandSelect implements CommandExecutor {
    private DirectChat plugin;

    private CommandSelect(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandSelect(plugin)).setDescription(Texts.of(TextColors.AQUA, "Selects an active channel"))
                .setArguments(GenericArguments.optional(GenericArguments.string(Texts.of("ChannelName")))).build();
    }

    public CommandResult execute(CommandSource commandSource, CommandContext arguments) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return CommandResult.success();
        }

        Player player = (Player) commandSource;
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        if (!arguments.hasAny("ChannelName")) {
            if (member.getActive() != null && !member.getActive().isUndetectable()) {
                commandSource.sendMessage(Texts.of(TextColors.AQUA, "Current active channel: ", TextColors.WHITE, member.getActive().toString()));
            }

            Set<Channel> woC = new HashSet<Channel>(member.getChannels().getDetectableChannels());
            woC.remove(member.getActive());

            commandSource.sendMessage(Texts.of(TextColors.AQUA, "Selectable channels: ", TextColors.WHITE, woC.toString()));
            return CommandResult.success();
        }

        String channelName = arguments.<String> getOne("ChannelName").get();
        Channel select = null;
        for (Channel ch : member.getChannels()) {
            if (ch.getName().equals(channelName)) {
                select = ch;
                break;
            }
        }

        if (select == null) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "You cannot select a channel you are not a part of. Use ", TextColors.WHITE, "/join", TextColors.RED, " first."));
            return CommandResult.success();
        }

        member.setActive(select);
        commandSource.sendMessage(Texts.of(TextColors.GREEN, "Successfully select channel: ", TextColors.WHITE, select.getName()));
        return CommandResult.success();
    }
}
