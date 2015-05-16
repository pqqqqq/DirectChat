package com.pqqqqq.directchat.commands;

import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.PrivateChannel;
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
 * Created by Kevin on 2015-05-05.
 */
public class CommandJoin implements CommandExecutor {
    private DirectChat plugin;

    private CommandJoin(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandJoin(plugin)).setDescription(Texts.of(TextColors.AQUA, "Joins a chat channel."))
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
            String print = "";

            for (Channel channel : plugin.getChannels().getMap().values()) {
                if (channel.isUndetectable() || channel.getMembers().contains(member)) {
                    continue;
                }

                if (channel instanceof PrivateChannel) {
                    PrivateChannel pc = (PrivateChannel) channel;

                    if (!pc.getOwner().equals(member) && pc.getInvitations().contains(member)) {
                        print += "*" + pc.getName() + ", ";
                    }
                } else {
                    if (channel.canEnter(member) == Channel.EnterResult.SUCCESS) {
                        print += channel.getName() + ", ";
                    }
                }
            }

            if (print.isEmpty()) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "There are no channels available for you to join."));
            } else {
                commandSource.sendMessage(Texts.of(TextColors.GREEN, "Available channels: ", TextColors.WHITE, print.substring(0, print.length() - 2)));
            }
            return CommandResult.success();
        }

        String channelName = arguments.<String> getOne("ChannelName").get();
        Channel toJoin = plugin.getChannels().getValue(channelName.trim());
        if (member.enterChannel(toJoin)) {
            commandSource.sendMessage(Texts.of(TextColors.GREEN, "Successfully joined channel: ", TextColors.WHITE, toJoin.getName()));
        }
        return CommandResult.success();
    }
}
