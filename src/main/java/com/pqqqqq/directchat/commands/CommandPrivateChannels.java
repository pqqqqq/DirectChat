package com.pqqqqq.directchat.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.PrivateChannel;
import com.pqqqqq.directchat.channel.member.Member;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kevin on 2015-05-05.
 */
public class CommandPrivateChannels extends CommandBase {
    private static final Optional<Text> desc = Optional.<Text> of(Texts.of("Private channel command."));
    private static final Optional<Text> help = Optional.<Text> of(Texts.of("Private channel command."));

    public CommandPrivateChannels(DirectChat plugin) {
        super(plugin);
    }

    public Optional<CommandResult> process(CommandSource commandSource, String s) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Player-only command."));
            return Optional.of(CommandResult.success());
        }

        Player player = (Player) commandSource;
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        if (s.trim().isEmpty()) {
            commandSource.sendMessage(Texts.of(TextColors.RED, getUsage(player)));
            return Optional.of(CommandResult.success());
        }

        String[] args = s.trim().split(" ");

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "/pc create <name>"));
                return Optional.of(CommandResult.success());
            }

            if (args[1].length() > 10) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "Channel names must be a maximum of 10 characters."));
                return Optional.of(CommandResult.success());
            }

            if (member.getOwnerChannel() == null) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "You may only be the owner of one private channel."));
                return Optional.of(CommandResult.success());
            }

            if (plugin.getChannels().contains(args[1])) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "This channel name has already been taken."));
                return Optional.of(CommandResult.success());
            }

            PrivateChannel pc = new PrivateChannel(args[1], member);
            plugin.getChannels().add(args[1], pc);

            member.enterChannel(pc);
            commandSource.sendMessage(Texts.of(TextColors.GREEN, "Successfully created private channel: ", TextColors.WHITE, pc.getName()));
        } else if (args[0].equalsIgnoreCase("invite")) {
            if (args.length < 2) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "/pc invite <player>"));
                return Optional.of(CommandResult.success());
            }

            PrivateChannel pc = member.getOwnerChannel();
            if (pc == null) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "You must be the owner of a private channel to do this."));
                return Optional.of(CommandResult.success());
            }

            if (!pc.getOwner().equals(member)) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "You are not the owner of this channel"));
                return Optional.of(CommandResult.success());
            }

            Optional<Player> inv = plugin.getGame().getServer().getPlayer(args[1]);
            if (!inv.isPresent()) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "Invalid player."));
                return Optional.of(CommandResult.success());
            }

            Member invm = plugin.getMembers().getValue(inv.get().getUniqueId().toString());
            if (invm.isMuteInvitations()) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "This player has turned invitations off."));
                return Optional.of(CommandResult.success());
            }

            if (pc.getInvitations().contains(inv)) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "You have already sent an invitation to this player."));
                return Optional.of(CommandResult.success());
            }

            pc.getInvitations().add(invm);

            commandSource.sendMessage(Texts.of(TextColors.GREEN, "Invitation sent."));
            inv.get().sendMessage(Texts.of(TextColors.WHITE, player.getName(), TextColors.GREEN, " has sent you an invitation to join their channel ", TextColors.WHITE, pc.getName()));

            TextBuilder builder = Texts.builder();
            builder.append(Texts.of(TextColors.AQUA, "Type "));
            builder.append(Texts.builder("/j " + pc.getName()).color(TextColors.WHITE).onClick(TextActions.runCommand("/j " + pc.getName())).onHover(TextActions.showText(Texts.of(TextColors.WHITE, "Run this command."))).build());
            builder.append(Texts.of(TextColors.AQUA, " to join."));

            inv.get().sendMessage(Texts.of(TextColors.AQUA, builder.build()));
        } else if (args[0].equalsIgnoreCase("kick")) {
            if (args.length < 2) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "/pc kick <player>"));
                return Optional.of(CommandResult.success());
            }

            PrivateChannel pc = member.getOwnerChannel();
            if (pc == null) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "You must be the owner of a private channel to do this."));
                return Optional.of(CommandResult.success());
            }

            if (!pc.getOwner().equals(member)) {
                commandSource.sendMessage(Texts.of(TextColors.RED, "You are not the owner of this channel"));
                return Optional.of(CommandResult.success());
            }

            Set<Member> members = new HashSet<Member>(pc.getMembers());
            for (Member mem : members) {
                if (mem.getLastCachedUsername() != null && mem.getLastCachedUsername().equalsIgnoreCase(args[1])) {
                    if (mem.equals(member)) {
                        TextBuilder builder = Texts.builder();
                        builder.append(Texts.of(TextColors.RED, "You cannot kick yourself. Use "));
                        builder.append(Texts.builder("/leave.").color(TextColors.WHITE).onClick(TextActions.runCommand("/leave")).onHover(TextActions.showText(Texts.of(TextColors.WHITE, "Run this command."))).build());

                        commandSource.sendMessage(Texts.of(TextColors.RED, builder.build()));
                        return Optional.of(CommandResult.success());
                    }

                    mem.leaveChannel(pc);

                    mem.sendMessage(Texts.of(TextColors.AQUA, "You have been kicked from: ", TextColors.WHITE, pc.getName()));
                    player.sendMessage(Texts.of(TextColors.GREEN, "Player successfully kicked."));
                    return Optional.of(CommandResult.success());
                }
            }

            commandSource.sendMessage(Texts.of(TextColors.RED, "No such player ", TextColors.WHITE, args[1], TextColors.RED, " exists in your channel."));
        } else {
            commandSource.sendMessage(Texts.of(TextColors.RED, "Invalid action: ", TextColors.WHITE, args[0]));
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
        return Texts.of("/p <create|invite|kick>");
    }
}
