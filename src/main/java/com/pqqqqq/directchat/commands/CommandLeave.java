package com.pqqqqq.directchat.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.member.Member;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

/**
 * Created by Kevin on 2015-05-06.
 */
public class CommandLeave extends CommandBase {
    private static final Optional<Text> desc = Optional.<Text> of(Texts.of("Leaves a channel."));
    private static final Optional<Text> help = Optional.<Text> of(Texts.of("Leaves a channel."));

    public CommandLeave(DirectChat plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource commandSource, String s) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(getErrorMessage("Player-only command."));
            return Optional.of(CommandResult.success());
        }

        Player player = (Player) commandSource;
        Member member = getPlugin().getMembers().getValue(player.getUniqueId().toString());

        if (s.trim().isEmpty()) {
            commandSource.sendMessage(getNormalMessage("Current channels: &f" + member.getChannels().getDetectableChannels().toString()));
            return Optional.of(CommandResult.success());
        }

        Channel toLeave = null;
        for (Channel channel : member.getChannels()) {
            if (channel.getName().equalsIgnoreCase(s.trim())) {
                toLeave = channel;
                break;
            }
        }

        if (toLeave == null) {
            commandSource.sendMessage(getErrorMessage("Could not find specified channel."));
            return Optional.of(CommandResult.success());
        }

        if (member.leaveChannel(toLeave)) {
            commandSource.sendMessage(getSuccessMessage("Successfully left channel: &f" + toLeave.getName()));
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
        return Texts.of("/leave [channel]");
    }
}
