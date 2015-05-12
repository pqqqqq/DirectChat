package com.pqqqqq.directchat.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.Config;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.util.Utilities;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

/**
 * Created by Kevin on 2015-05-04.
 */
public class CommandRespond extends CommandBase {
    private static final Optional<Text> desc = Optional.<Text> of(Texts.of("Send a response private message to a player."));
    private static final Optional<Text> help = Optional.<Text> of(Texts.of("Send a response private message to a player."));

    public CommandRespond(DirectChat plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource commandSource, String s) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return Optional.of(CommandResult.success());
        }

        Player player = (Player) commandSource;
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        s = s.trim();
        String[] args = s.split(" ");
        if (args.length == 0) {
            commandSource.sendMessage(Texts.of(TextColors.RED, getUsage(commandSource)));
            return Optional.of(CommandResult.success());
        }

        Optional<Member> rec = member.getRespond();
        if (!rec.isPresent()) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "You have no one to respond to ;("));
            return Optional.of(CommandResult.success());
        }

        Optional<Player> recP = rec.get().getPlayer();
        if (!recP.isPresent()) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Your response partner is no longer online."));
            return Optional.of(CommandResult.success());
        }

        String message = s.trim();

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
        return Optional.of(CommandResult.success());
    }

    public Optional<Text> getShortDescription(CommandSource commandSource) {
        return desc;
    }

    public Optional<Text> getHelp(CommandSource commandSource) {
        return help;
    }

    public Text getUsage(CommandSource commandSource) {
        return Texts.of("/r <message ...>");
    }

    private String format(String format, Player sender, Player receiver, String message) {
        return format.replace("%SENDER%", sender.getName()).replace("%RECEIVER%", receiver.getName()).replace("%MESSAGE%", message)
                .replace("%PREFIX%", Utilities.formatColours(Utilities.getPEXOption(sender, "prefix").get())).replace("%SUFFIX%", Utilities.formatColours(Utilities.getPEXOption(sender, "suffix").get()));
    }
}
