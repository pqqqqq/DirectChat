package com.pqqqqq.directchat.commands;

import com.pqqqqq.directchat.DirectChat;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;

import java.util.Collections;
import java.util.List;

/**
 * Created by Kevin on 2015-05-04.
 */
public abstract class CommandBase implements CommandCallable {
    DirectChat plugin;

    public CommandBase(DirectChat plugin) {
        this.plugin = plugin;
    }

    public List<String> getSuggestions(CommandSource commandSource, String s) throws CommandException {
        return Collections.emptyList();
    }

    public boolean testPermission(CommandSource commandSource) {
        return true;
    }
}
