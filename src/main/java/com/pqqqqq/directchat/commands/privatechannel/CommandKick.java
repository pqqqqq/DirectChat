package com.pqqqqq.directchat.commands.privatechannel;

import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.PrivateChannel;
import com.pqqqqq.directchat.channel.member.Member;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.args.GenericArguments;
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kevin on 2015-05-15.
 */
public class CommandKick implements CommandExecutor {
    private DirectChat plugin;

    private CommandKick(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandKick(plugin)).setDescription(Texts.of(TextColors.AQUA, "Kick a player from a private channel."))
                .setArguments(GenericArguments.string(Texts.of("PlayerName"))).build();
    }

    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return CommandResult.success();
        }

        Player player = (Player) commandSource;
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        PrivateChannel pc = member.getOwnerChannel();
        if (pc == null) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "You must be the owner of a private channel to do this."));
            return CommandResult.success();
        }

        if (!pc.getOwner().equals(member)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "You are not the owner of this channel"));
            return CommandResult.success();
        }

        String playerName = commandContext.<String> getOne("PlayerName").get();
        Set<Member> members = new HashSet<Member>(pc.getMembers());
        for (Member mem : members) {
            if (mem.getLastCachedUsername() != null && mem.getLastCachedUsername().equalsIgnoreCase(playerName)) {
                if (mem.equals(member)) {
                    TextBuilder builder = Texts.builder();
                    builder.append(Texts.of(TextColors.RED, "You cannot kick yourself. Use "));
                    builder.append(Texts.builder("/leave.").color(TextColors.WHITE).onClick(TextActions.runCommand("/leave")).onHover(TextActions.showText(Texts.of(TextColors.WHITE, "Run this command."))).build());

                    commandSource.sendMessage(Texts.of(TextColors.RED, builder.build()));
                    return CommandResult.success();
                }

                mem.leaveChannel(pc);

                mem.sendMessage(Texts.of(TextColors.AQUA, "You have been kicked from: ", TextColors.WHITE, pc.getName()));
                player.sendMessage(Texts.of(TextColors.GREEN, "Player successfully kicked."));
                return CommandResult.success();
            }
        }

        commandSource.sendMessage(Texts.of(TextColors.RED, "No such player ", TextColors.WHITE, playerName, TextColors.RED, " exists in your channel."));
        return CommandResult.success();
    }
}
