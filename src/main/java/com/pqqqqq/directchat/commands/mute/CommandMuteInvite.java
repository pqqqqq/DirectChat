package com.pqqqqq.directchat.commands.mute;

import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.member.Member;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;

/**
 * Created by Kevin on 2015-05-15.
 */
public class CommandMuteInvite implements CommandExecutor {
    private DirectChat plugin;

    private CommandMuteInvite(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandMuteInvite(plugin)).setDescription(Texts.of(TextColors.AQUA, "Mutes invites.")).build();
    }

    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return CommandResult.success();
        }

        Player player = (Player) commandSource;
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        member.setMuteInvitations(!member.isMuteInvitations());
        player.sendMessage(Texts.of(TextColors.AQUA, "Invitation mute: ", TextColors.WHITE, (member.isMuteInvitations() ? "ON" : "OFF")));
        return CommandResult.success();
    }
}
