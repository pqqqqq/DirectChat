package com.pqqqqq.directchat.commands.mute;

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
 * Created by Kevin on 2015-05-15.
 */
public class CommandMuteChannel implements CommandExecutor {
    private DirectChat plugin;

    private CommandMuteChannel(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandMuteChannel(plugin)).setDescription(Texts.of(TextColors.AQUA, "Mutes channel conversations."))
                .setArguments(GenericArguments.string(Texts.of("ChannelName"))).build();
    }

    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return CommandResult.success();
        }

        Player player = (Player) commandSource;
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        String channelName = commandContext.<String> getOne("ChannelName").get();
        if (channelName.equalsIgnoreCase("-all")) { // TODO: Perhaps a better way to do this, channels can be named -all.
            member.setMuteAllChannels(!member.isMuteAllChannels());
            commandSource.sendMessage(Texts.of(TextColors.AQUA, "Full channel mute: ", TextColors.WHITE, (member.isMuteAllChannels() ? "ON" : "OFF")));
            return CommandResult.success();
        }

        Channel chan = plugin.getChannels().getValue(channelName);
        if (!member.getChannels().contains(chan)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "You are not in this channel."));
            return CommandResult.success();
        }

        if (member.getMuted().remove(chan)) {
            commandSource.sendMessage(Texts.of(TextColors.AQUA, "Successfully unmuted channel: ", TextColors.WHITE, chan.getName()));
        } else {
            member.getMuted().add(chan);
            commandSource.sendMessage(Texts.of(TextColors.AQUA, "Successfully muted channel: ", TextColors.WHITE, chan.getName()));
        }

        return CommandResult.success();
    }
}
