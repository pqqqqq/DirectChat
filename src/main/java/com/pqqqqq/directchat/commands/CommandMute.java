package com.pqqqqq.directchat.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.member.Member;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

/**
 * Created by Kevin on 2015-05-11.
 */
public class CommandMute extends CommandBase {
    private static final Optional<Text> desc = Optional.<Text> of(Texts.of("Mutes certain channels or functions."));
    private static final Optional<Text> help = Optional.<Text> of(Texts.of("Mutes certain channels or functions."));

    public CommandMute(DirectChat plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource source, String arguments) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return Optional.of(CommandResult.success());
        }

        Player player = (Player) source;
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        String argument = arguments.trim();
        String[] args = argument.split(" ");
        if (args.length == 0) {
            source.sendMessage(Texts.of(TextColors.RED, getUsage(source)));
            return Optional.of(CommandResult.success());
        }

        if (args[0].equalsIgnoreCase("channel")) {
            if (args.length < 2) {
                source.sendMessage(Texts.of(TextColors.RED, "/mute channel <channel|-all>"));
                return Optional.of(CommandResult.success());
            }

            if (args[1].equalsIgnoreCase("-all")) { // TODO: Perhaps a better way to do this, channels can be named -all.
                member.setMuteAllChannels(!member.isMuteAllChannels());
                source.sendMessage(Texts.of(TextColors.AQUA, "Full channel mute: ", TextColors.WHITE, (member.isMuteAllChannels() ? "ON" : "OFF")));
                return Optional.of(CommandResult.success());
            }

            Channel chan = plugin.getChannels().getValue(args[1]);
            if (!member.getChannels().contains(chan)) {
                source.sendMessage(Texts.of(TextColors.RED, "You are not in this channel."));
                return Optional.of(CommandResult.success());
            }

            if (member.getMuted().remove(chan)) {
                source.sendMessage(Texts.of(TextColors.AQUA, "Successfully unmuted channel: ", TextColors.WHITE, chan.getName()));
            } else {
                member.getMuted().add(chan);
                source.sendMessage(Texts.of(TextColors.AQUA, "Successfully muted channel: ", TextColors.WHITE, chan.getName()));
            }
        } else if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("invites") || args[0].equalsIgnoreCase("invitation") || args[0].equalsIgnoreCase("invitations")) {
            member.setMuteInvitations(!member.isMuteInvitations());
            source.sendMessage(Texts.of(TextColors.AQUA, "Invitation mute: ", TextColors.WHITE, (member.isMuteInvitations() ? "ON" : "OFF")));
            return Optional.of(CommandResult.success());
        } else {
            source.sendMessage(Texts.of(TextColors.RED, getUsage(source)));
        }
        return Optional.of(CommandResult.success());
    }

    public Optional<Text> getShortDescription(CommandSource source) {
        return desc;
    }

    public Optional<Text> getHelp(CommandSource source) {
        return help;
    }

    public Text getUsage(CommandSource source) {
        return Texts.of("/mute <channel|invites>");
    }
}
