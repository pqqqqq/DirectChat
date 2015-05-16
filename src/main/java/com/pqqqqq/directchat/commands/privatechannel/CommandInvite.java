package com.pqqqqq.directchat.commands.privatechannel;

import com.google.common.base.Optional;
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

/**
 * Created by Kevin on 2015-05-15.
 */
public class CommandInvite implements CommandExecutor {
    private DirectChat plugin;

    private CommandInvite(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandInvite(plugin)).setDescription(Texts.of(TextColors.AQUA, "Invite a player to a private channel."))
                .setArguments(GenericArguments.player(Texts.of("Player"), plugin.getGame())).build();
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

        Optional<Player> inv = commandContext.getOne("Player");
        if (!inv.isPresent()) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Invalid player."));
            return CommandResult.success();
        }

        Member invm = plugin.getMembers().getValue(inv.get().getUniqueId().toString());
        if (invm.isMuteInvitations()) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "This player has turned invitations off."));
            return CommandResult.success();
        }

        if (pc.getInvitations().contains(inv)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "You have already sent an invitation to this player."));
            return CommandResult.success();
        }

        pc.getInvitations().add(invm);

        commandSource.sendMessage(Texts.of(TextColors.GREEN, "Invitation sent."));
        inv.get().sendMessage(Texts.of(TextColors.WHITE, player.getName(), TextColors.GREEN, " has sent you an invitation to join their channel ", TextColors.WHITE, pc.getName()));

        TextBuilder builder = Texts.builder();
        builder.append(Texts.of(TextColors.AQUA, "Type "));
        builder.append(Texts.builder("/j " + pc.getName()).color(TextColors.WHITE).onClick(TextActions.runCommand("/j " + pc.getName())).onHover(TextActions.showText(Texts.of(TextColors.WHITE, "Run this command."))).build());
        builder.append(Texts.of(TextColors.AQUA, " to join."));

        inv.get().sendMessage(Texts.of(TextColors.AQUA, builder.build()));
        return CommandResult.success();
    }
}
