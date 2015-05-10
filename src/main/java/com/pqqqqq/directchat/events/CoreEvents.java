package com.pqqqqq.directchat.events;

import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.PrivateChannel;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.util.Utilities;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;
import org.spongepowered.api.event.entity.player.PlayerQuitEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

/**
 * Created by Kevin on 2015-05-03.
 */
public class CoreEvents {
    private DirectChat plugin;

    public CoreEvents(DirectChat plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = Order.LAST)
    public void chat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getProfile().getUniqueId().toString();

        Text message = event.getMessage();
        String raw = Texts.toPlain(message);

        // TODO: HACKISH FIX TO GET RID OF ANGULAR BRACKETS. REMOVE WHEN FIXED IN SPONGE
        raw = raw.substring(raw.indexOf('>') + 2);

        // Cancel it cause we're sending it individually
        event.setCancelled(true);

        // Add colour if they have permissions
        if (player.hasPermission("directchat.colour")) {
            raw = Utilities.formatColours(raw);
        }

        // Return if profanity/advertising
        if (!Utilities.filter(player, raw)) {
            return;
        }

        // Get the member
        Member member = plugin.getMembers().getValue(uuid);
        member.setLastActive(); // He's active

        Channel channel = member.getActive();
        if (channel == null) {
            return;
        }

        // Make sure they can still be here
        if (channel.hasPermissions(member) != Channel.EnterResult.SUCCESS) {
            member.leaveChannel(channel); // Kick this buffoon
            return;
        }

        String format = channel.formatMessage(player, member, raw);
        Text formatted = Texts.of(format);

        channel.onMessage(member, formatted);
        plugin.getLogger().info(Utilities.unformatColours(format));

        for (Member channelMember : plugin.getMembers().getMap().values()) {
            // Sound player's name if mentioned
            //if (Config.soundOnMention && channelMember.isOnline() && format.toLowerCase().contains(channelMember.getLastCachedUsername().toLowerCase())) {
            //    channelMember.getPlayer().get().playSound(SoundTypes.SUCCESSFUL_HIT, channelMember.getPlayer().get().getLocation().getPosition(), 60D);
            //}

            // If this member is in the channel, then
            if (channel.getMembers().contains(channelMember)) {
                // First ensure that they can still be in the channel
                if (!member.equals(channelMember) && channel.hasPermissions(channelMember) != Channel.EnterResult.SUCCESS) { // We've already checked the sender
                    channelMember.leaveChannel(channel); // Kick this buffoon
                }

                if (channel.canSpeak(member, channelMember)) { // Check if the sender can speak to this member, and send if they can.
                    channelMember.sendMessage(formatted); // Send the formatted message to the member
                }
                continue;
            }

            // Snooper
            // Private
            if (channel instanceof PrivateChannel) {
                if (channelMember.getSnooperData().isPrivate()) {
                    channelMember.sendMessage(formatted);
                    continue;
                }
            // Public
            } else {
                if (channelMember.getSnooperData().isPublic()) {
                    channelMember.sendMessage(formatted);
                    continue;
                }
            }
        }
    }

    @Subscribe
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (!plugin.getMembers().contains(uuid)) {
            plugin.getMembers().add(uuid, new Member(player));
        }
    }

    @Subscribe
    public void leave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Member member = plugin.getMembers().getValue(player.getUniqueId().toString());

        if (member == null) {
            return;
        }

        for (Channel channel : member.getChannels().clone()) {
            if (channel.isLeaveOnExit()) {
                member.leaveChannel(channel, false);
            }
        }
    }
}
