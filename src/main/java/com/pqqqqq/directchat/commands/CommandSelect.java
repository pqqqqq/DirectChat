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

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kevin on 2015-05-06.
 */
public class CommandSelect extends CommandBase {
    private static final Optional<Text> desc = Optional.<Text> of(Texts.of("Selects a channel to talk in."));
    private static final Optional<Text> help = Optional.<Text> of(Texts.of("Selects a channel to talk in."));

    public CommandSelect(DirectChat plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource commandSource, String s) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(getErrorMessage("Player-only command"));
            return Optional.of(CommandResult.success());
        }

        Player player = (Player) commandSource;
        Member member = getPlugin().getMembers().getValue(player.getUniqueId().toString());

        if (s.trim().isEmpty()) {
            if (member.getActive() != null && !member.getActive().isUndetectable()) {
                commandSource.sendMessage(getNormalMessage("Current active channel: &f" + member.getActive().toString()));
            }

            Set<Channel> woC = new HashSet<Channel>(member.getChannels().getDetectableChannels());
            woC.remove(member.getActive());

            commandSource.sendMessage(getNormalMessage("Selectable channels: &f" + woC.toString()));
            return Optional.of(CommandResult.success());
        }

        Channel select = null;
        for (Channel ch : member.getChannels()) {
            if (ch.getName().equals(s)) {
                select = ch;
                break;
            }
        }

        if (select == null) {
            commandSource.sendMessage(getErrorMessage("You cannot select a channel you are not a part of. Use &f/join."));
            return Optional.of(CommandResult.success());
        }

        member.setActive(select);
        commandSource.sendMessage(getSuccessMessage("Successfully select channel: &f" + select.getName()));
        return Optional.of(CommandResult.success());
    }

    public Optional<Text> getShortDescription(CommandSource commandSource) {
        return desc;
    }

    public Optional<Text> getHelp(CommandSource commandSource) {
        return help;
    }

    public Text getUsage(CommandSource commandSource) {
        return Texts.of("/select [channel]");
    }
}
