package com.pqqqqq.directchat.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.member.Member;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

/**
 * Created by Kevin on 2015-05-07.
 */
public class CommandDirectChat extends CommandBase {
    private static final Optional<Text> desc = Optional.<Text> of(Texts.of("Main plugin command."));
    private static final Optional<Text> help = Optional.<Text> of(Texts.of("Main plugin command."));

    public CommandDirectChat(DirectChat plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource commandSource, String s) throws CommandException {
        s = s.trim();
        String[] args = s.split(" ");

        if (args[0].equalsIgnoreCase("reload")) {
            if (!commandSource.hasPermission("directchat.reload")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            getPlugin().getCfg().load();
            commandSource.sendMessage(getSuccessMessage("DirectChat reloaded."));
        } else if (args[0].equalsIgnoreCase("info")) {
            if (!commandSource.hasPermission("directchat.info")) {
                commandSource.sendMessage(getErrorMessage("Insufficient permissions."));
                return Optional.of(CommandResult.success());
            }

            String name = args[1];
            if (name.isEmpty()) {
                commandSource.sendMessage(getErrorMessage("/ds info <player>"));
                return Optional.of(CommandResult.success());
            }

            Member member = null;
            for (Member m : getPlugin().getMembers().getMap().values()) {
                if (m.getLastCachedUsername().equalsIgnoreCase(name)) {
                    member = m;
                    break;
                }
            }

            if (member == null) {
                commandSource.sendMessage(getErrorMessage("Invalid player: &f" + name));
                return Optional.of(CommandResult.success());
            }

            commandSource.sendMessage(getNormalMessage("Player: &f" + member.getLastCachedUsername()));
            commandSource.sendMessage(getNormalMessage("UUID: &f" + member.getUuid()));
            commandSource.sendMessage(getNormalMessage("Snooper: &f" + member.getSnooperData().toString()));

            if (member.getActive() != null) {
                commandSource.sendMessage(getNormalMessage("Active channel: &f" + (member.getActive().isUndetectable() ? "*" : "") + member.getActive().toString()));
            }

            commandSource.sendMessage(getNormalMessage("Channels: &f" + member.getChannels().getChannels().toString()));
            commandSource.sendMessage(getNormalMessage("Extra Data: &f" + member.getExtraData().toString()));
        } else {
            commandSource.sendMessage(getErrorMessage(getUsage(commandSource)));
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
        return Texts.of("/dc <reload|info>");
    }
}
