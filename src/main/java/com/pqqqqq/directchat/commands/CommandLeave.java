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

/**
 * Created by Kevin on 2015-05-06.
 */
public class CommandLeave implements CommandExecutor {
    private DirectChat plugin;

    private CommandLeave(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandLeave(plugin)).setDescription(Texts.of(TextColors.AQUA, "Leaves a chat channel."))
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
            commandSource.sendMessage(Texts.of(TextColors.AQUA, "Current channels: ", TextColors.WHITE, member.getChannels().getDetectableChannels().toString()));
            return CommandResult.success();
        }

        String channelName = arguments.<String> getOne("ChannelName").get();
        Channel toLeave = null;
        for (Channel channel : member.getChannels()) {
            if (channel.getName().equalsIgnoreCase(channelName.trim())) {
                toLeave = channel;
                break;
            }
        }

        if (toLeave == null) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Could not find specified channel."));
            return CommandResult.success();
        }

        if (member.leaveChannel(toLeave)) {
            commandSource.sendMessage(Texts.of(TextColors.GREEN, "Successfully left channel: ", TextColors.WHITE, toLeave.getName()));
        }
        return CommandResult.success();
    }
}
