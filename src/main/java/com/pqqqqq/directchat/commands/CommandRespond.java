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
public class CommandRespond implements CommandExecutor {
    private DirectChat plugin;

    private CommandRespond(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandRespond(plugin)).setDescription(Texts.of(TextColors.AQUA, "Responds to a player's PM"))
                .setArguments(GenericArguments.remainingJoinedStrings(Texts.of("Message"))).build();
    }

    public CommandResult execute(CommandSource commandSource, CommandContext arguments) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return CommandResult.success();
        }

        Player player = (Player) commandSource;
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        Optional<Member> rec = member.getRespond();
        if (!rec.isPresent()) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "You have no one to respond to ;("));
            return CommandResult.success();
        }

        Optional<Player> recP = rec.get().getPlayer();
        if (!recP.isPresent()) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Your response partner is no longer online."));
            return CommandResult.success();
        }

        String message = arguments.<String> getOne("Message").get().trim();

        // Add colour if perms
        if (player.hasPermission("directchat.colour")) {
            message = Utilities.formatColours(message);
        }

        String whisperSend = format(Config.whisperSendFormat, player, recP.get(), message);
        String whisperReceive = format(Config.whisperReceiveFormat, player, recP.get(), message);
        String whisperSnoop = format(Config.whisperSnooperFormat, player, recP.get(), message);

        player.sendMessage(Texts.of(whisperSend));
        recP.get().sendMessage(Texts.of(whisperReceive));

        for (Member admin : plugin.getMembers().getMap().values()) {
            if (admin.getSnooperData().isWhisper() && !admin.equals(rec.get()) && !admin.equals(member)) {
                admin.sendMessage(Texts.of(whisperSnoop));
            }
        }

        member.setRespond(Optional.of(rec.get()));
        rec.get().setRespond(Optional.of(member));
        return CommandResult.success();
    }

    private String format(String format, Player sender, Player receiver, String message) {
        return format.replace("%SENDER%", sender.getName()).replace("%RECEIVER%", receiver.getName()).replace("%MESSAGE%", message)
                .replace("%PREFIX%", Utilities.formatColours(Utilities.getPEXOption(sender, "prefix").get())).replace("%SUFFIX%", Utilities.formatColours(Utilities.getPEXOption(sender, "suffix").get()));
    }
}
