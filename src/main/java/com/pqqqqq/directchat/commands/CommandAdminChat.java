package com.pqqqqq.directchat.commands;

import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.util.Utilities;
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
 * Created by Kevin on 2015-05-07.
 */
public class CommandAdminChat implements CommandExecutor {
    private DirectChat plugin;

    private CommandAdminChat(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandAdminChat(plugin)).setDescription(Texts.of(TextColors.AQUA, "Toggles or talks in admin chat."))
                .setArguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Texts.of("Message")))).build();
    }

    public CommandResult execute(CommandSource commandSource, CommandContext arguments) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return CommandResult.success();
        }

        Player player = (Player) commandSource;
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        if (Channel.admin == null) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "There is no admin chat setup."));
            return CommandResult.success();
        }

        if (!Channel.admin.getMembers().contains(member)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "You must be in the admin channel."));
            return CommandResult.success();
        }

        if (Channel.admin.hasPermissions(member) != Channel.EnterResult.SUCCESS) {
            member.leaveChannel(Channel.admin);
            commandSource.sendMessage(Texts.of(TextColors.RED, "You have been booted from the admin channel, how did you get here?"));
            return CommandResult.success();
        }

        if (!arguments.hasAny("Message")) {
            // Toggle admin chat
            if (member.getActive().equals(Channel.admin)) {
                member.setActive(Channel.def);
                commandSource.sendMessage(Texts.of(TextColors.AQUA, "Admin chat toggled: ", TextColors.WHITE, "OFF"));
            } else {
                member.setActive(Channel.admin);
                commandSource.sendMessage(Texts.of(TextColors.AQUA, "Admin chat toggled: ", TextColors.WHITE, "ON"));
            }
        } else {
            // Say into admin chat
            String message = arguments.<String> getOne("Message").get();
            if (player.hasPermission("directchat.colour")) {
                message = Utilities.formatColour(message);
            }

            Channel.admin.broadcast(Texts.of(Channel.admin.formatMessage(player, member, message.trim())));
        }

        return CommandResult.success();
    }
}
