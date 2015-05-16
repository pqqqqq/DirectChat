package com.pqqqqq.directchat.commands.directchat;

import com.pqqqqq.directchat.DirectChat;
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
public class CommandReload implements CommandExecutor {
    private DirectChat plugin;

    private CommandReload(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandReload(plugin)).setDescription(Texts.of(TextColors.AQUA, "Reloads plugin config.")).build();
    }

    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if (!commandSource.hasPermission("directchat.reload")) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Insufficient permissions."));
            return CommandResult.success();
        }

        plugin.getCfg().load();
        commandSource.sendMessage(Texts.of(TextColors.GREEN, "DirectChat reloaded."));
        return CommandResult.success();
    }
}
