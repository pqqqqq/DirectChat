package com.pqqqqq.directchat.commands;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.PrivateChannel;
import com.pqqqqq.directchat.channel.member.Member;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
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
            commandSource.sendMessage(getErrorMessage("Player-only command"));
            return Optional.of(CommandResult.success());
        }

        Player player = (Player) commandSource;
        Member member = getPlugin().getMembers().getValue(player.getUniqueId().toString());

        if (s.trim().isEmpty()) {
            commandSource.sendMessage(getErrorMessage(getUsage(player)));
            return Optional.of(CommandResult.success());
        }

        String[] args = s.trim().split(" ");

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                commandSource.sendMessage(getErrorMessage("/pc create <name>"));
                return Optional.of(CommandResult.success());
            }

            if (args[1].length() > 10) {
                commandSource.sendMessage(getErrorMessage("Channel names must be a maximum of 10 characters."));
                return Optional.of(CommandResult.success());
            }

            if (member.getOwnerChannel() == null) {
                commandSource.sendMessage(getErrorMessage("You may only be the owner of one private channel."));
                return Optional.of(CommandResult.success());
            }

            if (getPlugin().getChannels().contains(args[1])) {
                commandSource.sendMessage(getErrorMessage("This channel name has already been taken."));
                return Optional.of(CommandResult.success());
            }

            PrivateChannel pc = new PrivateChannel(args[1], member);
            getPlugin().getChannels().add(args[1], pc);

            member.enterChannel(pc);
            player.sendMessage(getSuccessMessage("Successfully created private channel: &f" + pc.getName()));
        } else if (args[0].equalsIgnoreCase("invite")) {
            if (args.length < 2) {
                commandSource.sendMessage(getErrorMessage("/pc invite <player>"));
                return Optional.of(CommandResult.success());
            }

            PrivateChannel pc = member.getOwnerChannel();
            if (pc == null) {
                commandSource.sendMessage(getErrorMessage("You must be the owner of a private channel to do this."));
                return Optional.of(CommandResult.success());
            }

            if (!pc.getOwner().equals(member)) {
                commandSource.sendMessage(getErrorMessage("You are not the owner of this channel"));
                return Optional.of(CommandResult.success());
            }

            Optional<Player> inv = getPlugin().getGame().getServer().getPlayer(args[1]);
            if (!inv.isPresent()) {
                commandSource.sendMessage(getErrorMessage("Invalid player"));
                return Optional.of(CommandResult.success());
            }

            Member invm = getPlugin().getMembers().getValue(inv.get().getUniqueId().toString());

            if (pc.getInvitations().contains(inv)) {
                commandSource.sendMessage(getErrorMessage("You have already sent an invitation to this player."));
                return Optional.of(CommandResult.success());
            }

            pc.getInvitations().add(invm);

            player.sendMessage(getSuccessMessage("Invitation sent"));
            inv.get().sendMessage(getSuccessMessage("&f" + player.getName() + " &ahas sent you an invitation to join his channel &f" + pc.getName()));
            inv.get().sendMessage(getNormalMessage("Type &f\"/j " + pc.getName() + "\" &bto join"));
        } else if (args[0].equalsIgnoreCase("kick")) {
            if (args.length < 2) {
                commandSource.sendMessage(getErrorMessage("/pc kick <player>"));
                return Optional.of(CommandResult.success());
            }

            PrivateChannel pc = member.getOwnerChannel();
            if (pc == null) {
                commandSource.sendMessage(getErrorMessage("You must be the owner of a private channel to do this."));
                return Optional.of(CommandResult.success());
            }

            if (!pc.getOwner().equals(member)) {
                commandSource.sendMessage(getErrorMessage("You are not the owner of this channel"));
                return Optional.of(CommandResult.success());
            }

            Set<Member> members = new HashSet<Member>(pc.getMembers());
            for (Member mem : members) {
                if (mem.getLastCachedUsername() != null && mem.getLastCachedUsername().equalsIgnoreCase(args[1])) {
                    if (mem.equals(member)) {
                        commandSource.sendMessage(getErrorMessage("You cannot kick yourself. Use &f/leave"));
                        return Optional.of(CommandResult.success());
                    }

                    mem.leaveChannel(pc);
                    mem.sendMessage(getNormalMessage("You have been kicked from: &f" + pc.getName()));
                    player.sendMessage(getSuccessMessage("Player successfully kicked"));
                    return Optional.of(CommandResult.success());
                }
            }

            player.sendMessage(getErrorMessage("No such player &f" + args[1] + " &c exists in your channel."));
        } else {
            player.sendMessage(getErrorMessage("Invalid action: &f" + args[0]));
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
