package com.pqqqqq.directchat.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.PrivateChannel;
import com.pqqqqq.directchat.channel.member.Member;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

/**
 * Created by Kevin on 2015-05-05.
 */
public class CommandJoin extends CommandBase {
    private static final Optional<Text> desc = Optional.<Text> of(Texts.of("Joins a channel."));
    private static final Optional<Text> help = Optional.<Text> of(Texts.of("Joins a channel."));

    public CommandJoin(DirectChat plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource commandSource, String s) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return Optional.of(CommandResult.success());
        }

        Player player = (Player) commandSource;
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        if (s.trim().isEmpty()) {
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
            return Optional.of(CommandResult.success());
        }

        Channel toJoin = plugin.getChannels().getValue(s.trim());
        if (member.enterChannel(toJoin)) {
            commandSource.sendMessage(Texts.of(TextColors.GREEN, "Successfully joined channel: ", TextColors.WHITE, toJoin.getName()));
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
        return Texts.of("/join [channel]");
    }
}
