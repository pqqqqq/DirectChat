package com.pqqqqq.directchat.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.Config;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.util.Utilities;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

/**
 * Created by Kevin on 2015-05-04.
 */
public class CommandPM extends CommandBase {
    private static final Optional<Text> desc = Optional.<Text> of(Texts.of("Send a private message to a player."));
    private static final Optional<Text> help = Optional.<Text> of(Texts.of("Send a private message to a player."));

    public CommandPM(DirectChat plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource commandSource, String s) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(getErrorMessage("Player-only command"));
            return Optional.of(CommandResult.success());
        }

        Player player = (Player) commandSource;
        Member member = getPlugin().getMembers().getValue(player.getUniqueId().toString());

        Optional<Player> receiver = Optional.<Player> absent();
        String message = null;

        if (s.trim().isEmpty()) {
            commandSource.sendMessage(getErrorMessage(getUsage(commandSource)));
            return Optional.of(CommandResult.success());
        }

        if (!s.trim().contains(" ")) {
            Optional<Member> rec = member.getRespond();

            if (!rec.isPresent()) {
                commandSource.sendMessage(getErrorMessage("You have no one to respond to ;("));
                return Optional.of(CommandResult.success());
            }

            receiver = rec.get().getPlayer();
            message = s.trim();
        } else {
            String a1 = s.substring(0, s.indexOf(' '));
            message = s.substring(s.indexOf(' ') + 1, s.length());

            receiver = getPlugin().getGame().getServer().getPlayer(a1);
        }

        if (!receiver.isPresent()) {
            commandSource.sendMessage(getErrorMessage("Invalid player."));
            return Optional.of(CommandResult.success());
        }

        Member mrec = getPlugin().getMembers().getValue(receiver.get().getUniqueId().toString());

        // Add colour if perms
        if (player.hasPermission("directchat.colour")) {
            message = Utilities.formatColours(message);
        }

        String whisperSend = format(Config.whisperSendFormat, player, receiver.get(), message);
        String whisperReceive = format(Config.whisperReceiveFormat, player, receiver.get(), message);
        String whisperSnoop = format(Config.whisperSnooperFormat, player, receiver.get(), message);

        player.sendMessage(Texts.of(whisperSend));
        receiver.get().sendMessage(Texts.of(whisperReceive));

        for (Member admin : getPlugin().getMembers().getMap().values()) {
            if (admin.getSnooperData().isWhisper()) {
                admin.sendMessage(Texts.of(whisperSnoop));
            }
        }

        member.setRespond(Optional.of(mrec));
        mrec.setRespond(Optional.of(member));
        return Optional.of(CommandResult.success());
    }

    public Optional<Text> getShortDescription(CommandSource commandSource) {
        return desc;
    }

    public Optional<Text> getHelp(CommandSource commandSource) {
        return help;
    }

    public Text getUsage(CommandSource commandSource) {
        return Texts.of("/pm [name] <message ...>");
    }

    private String format(String format, Player sender, Player receiver, String message) {
        return format.replace("%SENDER%", sender.getName()).replace("%RECEIVER%", receiver.getName()).replace("%MESSAGE%", message)
                .replace("%PREFIX%", Utilities.formatColours(Utilities.getPEXOption(sender, "prefix").get())).replace("%SUFFIX%", Utilities.formatColours(Utilities.getPEXOption(sender, "suffix").get()));
    }
}
