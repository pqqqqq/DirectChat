package com.pqqqqq.directchat.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.util.Utilities;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

/**
 * Created by Kevin on 2015-05-07.
 */
public class CommandAdminChat extends CommandBase {
    private static final Optional<Text> desc = Optional.<Text> of(Texts.of("Admin chat command."));
    private static final Optional<Text> help = Optional.<Text> of(Texts.of("Admin chat command."));

    public CommandAdminChat(DirectChat plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource commandSource, String s) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(getErrorMessage("Player-only command"));
            return Optional.of(CommandResult.success());
        }

        Player player = (Player) commandSource;
        Member member = getPlugin().getMembers().getValue(player.getUniqueId().toString());

        if (Channel.admin == null) {
            commandSource.sendMessage(getErrorMessage("There is no admin chat setup."));
            return Optional.of(CommandResult.success());
        }

        if (!Channel.admin.getMembers().contains(member)) {
            commandSource.sendMessage(getErrorMessage("You must be in the admin channel."));
            return Optional.of(CommandResult.success());
        }

        if (Channel.admin.hasPermissions(member) != Channel.EnterResult.SUCCESS) {
            member.leaveChannel(Channel.admin);
            commandSource.sendMessage(getErrorMessage("You have been booted from the admin channel, how did you get here?"));
            return Optional.of(CommandResult.success());
        }

        if (s.trim().isEmpty()) {
            // Toggle admin chat
            if (member.getActive().equals(Channel.admin)) {
                member.setActive(Channel.def);
                commandSource.sendMessage(getNormalMessage("Admin chat toggled: &fOFF."));
            } else {
                member.setActive(Channel.admin);
                commandSource.sendMessage(getNormalMessage("Admin chat toggled: &fON."));
            }
        } else {
            // Say into admin chat
            if (player.hasPermission("directchat.colour")) {
                s = Utilities.formatColours(s);
            }

            Channel.admin.broadcast(Texts.of(Channel.admin.formatMessage(player, member, s.trim())));
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
        return Texts.of("/a [message]");
    }
}
