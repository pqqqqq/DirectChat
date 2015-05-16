package com.pqqqqq.directchat.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.Config;
import com.pqqqqq.directchat.DirectChat;
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
 * Created by Kevin on 2015-05-04.
 */
public class CommandPM implements CommandExecutor {
    private DirectChat plugin;

    private CommandPM(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandPM(plugin)).setDescription(Texts.of(TextColors.AQUA, "Private messages a player"))
                .setArguments(GenericArguments.seq(GenericArguments.player(Texts.of("Player"), plugin.getGame()), GenericArguments.remainingJoinedStrings(Texts.of("Message")))).build();
    }

    public CommandResult execute(CommandSource commandSource, CommandContext arguments) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return CommandResult.success();
        }

        Player player = (Player) commandSource;
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        Optional<Player> receiver = arguments.getOne("Player");
        String message = arguments.<String> getOne("Message").get().trim();

        if (!receiver.isPresent()) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Invalid player."));
            return CommandResult.success();
        }

        Member mrec = plugin.getMembers().getValue(receiver.get().getUniqueId().toString());

        // Add colour if perms
        if (player.hasPermission("directchat.colour")) {
            message = Utilities.formatColour(message);
        }

        String whisperSend = format(Config.whisperSendFormat, player, receiver.get(), message);
        String whisperReceive = format(Config.whisperReceiveFormat, player, receiver.get(), message);
        String whisperSnoop = format(Config.whisperSnooperFormat, player, receiver.get(), message);

        player.sendMessage(Texts.of(whisperSend));
        receiver.get().sendMessage(Texts.of(whisperReceive));

        for (Member admin : plugin.getMembers().getMap().values()) {
            if (admin.getSnooperData().isWhisper() && !admin.equals(mrec) && !admin.equals(member)) {
                admin.sendMessage(Texts.of(whisperSnoop));
            }
        }

        member.setRespond(Optional.of(mrec));
        mrec.setRespond(Optional.of(member));
        return CommandResult.success();
    }

    private String format(String format, Player sender, Player receiver, String message) {
        return format.replace("%SENDER%", sender.getName()).replace("%RECEIVER%", receiver.getName()).replace("%MESSAGE%", message)
                .replace("%PREFIX%", Utilities.formatColour(Utilities.getPEXOption(sender, "prefix").get())).replace("%SUFFIX%", Utilities.formatColour(Utilities.getPEXOption(sender, "suffix").get()));
    }
}
