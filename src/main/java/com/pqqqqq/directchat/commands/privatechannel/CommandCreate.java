package com.pqqqqq.directchat.commands.privatechannel;

import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.PrivateChannel;
import com.pqqqqq.directchat.channel.member.Member;
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
 * Created by Kevin on 2015-05-15.
 */
public class CommandCreate implements CommandExecutor {
    private DirectChat plugin;

    private CommandCreate(DirectChat plugin) {
        this.plugin = plugin;
    }

    public static CommandSpec build(DirectChat plugin) {
        return CommandSpec.builder().setExecutor(new CommandCreate(plugin)).setDescription(Texts.of(TextColors.AQUA, "Creates a private channel."))
                .setArguments(GenericArguments.string(Texts.of("Name"))).build();
    }

    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return CommandResult.success();
        }

        Player player = (Player) commandSource;
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        String name = commandContext.<String> getOne("Name").get();

        if (name.length() > 10) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Channel names must be a maximum of 10 characters."));
            return CommandResult.success();
        }

        if (member.getOwnerChannel() == null) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "You may only be the owner of one private channel."));
            return CommandResult.success();
        }

        if (plugin.getChannels().contains(name)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "This channel name has already been taken."));
            return CommandResult.success();
        }

        PrivateChannel pc = new PrivateChannel(name, member);
        plugin.getChannels().add(name, pc);

        member.enterChannel(pc);
        commandSource.sendMessage(Texts.of(TextColors.GREEN, "Successfully created private channel: ", TextColors.WHITE, pc.getName()));
        return CommandResult.success();
    }
}
