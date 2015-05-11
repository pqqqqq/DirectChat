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
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return Optional.of(CommandResult.success());
        }

        Player player = (Player) commandSource;
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        if (s.trim().isEmpty()) {
            if (member.getActive() != null && !member.getActive().isUndetectable()) {
                commandSource.sendMessage(Texts.of(TextColors.AQUA, "Current active channel: ", TextColors.WHITE, member.getActive().toString()));
            }

            Set<Channel> woC = new HashSet<Channel>(member.getChannels().getDetectableChannels());
            woC.remove(member.getActive());

            commandSource.sendMessage(Texts.of(TextColors.AQUA, "Selectable channels: ", TextColors.WHITE, woC.toString()));
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
            commandSource.sendMessage(Texts.of(TextColors.RED, "You cannot select a channel you are not a part of. Use ", TextColors.WHITE, "/join", TextColors.RED, " first."));
            return Optional.of(CommandResult.success());
        }

        member.setActive(select);
        commandSource.sendMessage(Texts.of(TextColors.GREEN, "Successfully select channel: ", TextColors.WHITE, select.getName()));
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
