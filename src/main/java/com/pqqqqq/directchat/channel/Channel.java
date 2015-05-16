package com.pqqqqq.directchat.channel;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.util.MappedManager;
import com.pqqqqq.directchat.util.Utilities;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Kevin on 2015-05-03.
 * Represents a chat channel that contains {@link Member}s
 */
public class Channel {
    public static Channel def = null;
    public static Channel admin = null;
    private final String name;
    // Config options
    private String format = null; // The format as to which chat messages for members chatting in this group are given.
    private String joinMessage = null; // The message sent to a player joining this channel.
    private boolean defChannel = false; // Whether this channel is the default channel. There should only be one default channel.
    private boolean crossWorlds = true; // Whether this channel's members can send messages in the channel across worlds.
    private boolean silent = false; // Whether this channel is silent, or doesn't send messages to its players.
    private boolean perpetual = false; // Whether this channel is perpetual, or is always active for users who can join it.
    private boolean undetectable = false; // Whether this channel is undetectable, or cannot be found by players.
    private boolean leaveOnExit = false; // Whether this channel is left by the user when they leave the server.
    private int radius = -1; // The radius, or locality of the channel. Anything less than 0 specifies a global channel.
    private List<String> permissions = Collections.emptyList(); // The permissions needed to join. None means no permissions are needed.
    private Set<Member> members = new HashSet<Member>();

    public Channel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // DO NOT add directly to this, use Member#enterChannel
    public Set<Member> getMembers() {
        return members;
    }

    public void broadcast(Text text) {
        for (Member member : members) {
            Optional<Player> player = member.getPlayer();
            if (player.isPresent()) {
                player.get().sendMessage(text);
            }
        }
    }

    public boolean isDefChannel() {
        return defChannel;
    }

    public void setDefChannel(boolean defChannel) {
        this.defChannel = defChannel;
    }

    public boolean isCrossWorlds() {
        return crossWorlds;
    }

    public void setCrossWorlds(boolean crossWorlds) {
        this.crossWorlds = crossWorlds;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean isPerpetual() {
        return perpetual;
    }

    public void setPerpetual(boolean perpetual) {
        this.perpetual = perpetual;
    }

    public boolean isUndetectable() {
        return undetectable;
    }

    public void setUndetectable(boolean undetectable) {
        this.undetectable = undetectable;
    }

    public boolean isLeaveOnExit() {
        return leaveOnExit;
    }

    public void setLeaveOnExit(boolean leaveOnExit) {
        this.leaveOnExit = leaveOnExit;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String formatMessage(Player sender, Member member, String message) {
        String format = getFormat();
        format = format.replace("%MESSAGE%", message);
        format = format.replace("%PLAYERNAME%", sender.getName());
        format = format.replace("%CHANNEL%", getName());
        format = format.replace("%PREFIX%", Utilities.formatColour(Utilities.getPEXOption(sender, "prefix").get()));
        format = format.replace("%SUFFIX%", Utilities.formatColour(Utilities.getPEXOption(sender, "suffix").get()));
        // format = format.replace("%DISPLAYNAME%", sender.getDisplayNameData().getDisplayName().toString()); TODO: Readd when implemented

        return format;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRadiusSquared() {
        return radius * radius;
    }

    public String getJoinMessage() {
        return joinMessage;
    }

    public void setJoinMessage(String joinMessage) {
        this.joinMessage = joinMessage;
    }

    public String getFormattedJoinMessage() {
        return joinMessage == null || joinMessage.trim().isEmpty() ? null : joinMessage.replace("%CHANNEL%", name);
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public final EnterResult hasPermissions(Member member) {
        Optional<Player> player = member.getPlayer();
        if (!player.isPresent()) {
            return EnterResult.OTHER_FAILURE;
        }

        if (permissions.isEmpty()) {
            return EnterResult.SUCCESS;
        }

        for (String permission : permissions) {
            if (player.get().hasPermission(permission)) {
                return EnterResult.SUCCESS;
            }
        }

        return EnterResult.INSUFFICIENT_PERMISSIONS;
    }

    // This method is not final so it can be overriden.
    public EnterResult canEnter(Member member) {
        return hasPermissions(member);
    }

    public boolean canSpeak(Member a, Member b) {
        Optional<Player> pa = a.getPlayer();
        Optional<Player> pb = b.getPlayer();

        // If it's the same player, return true
        if (a.equals(b)) {
            return true;
        }

        // Check if they're both present
        if (!pa.isPresent() || !pb.isPresent()) {
            return false;
        }

        // Check muted
        if (a.getMuted().contains(this) || b.getMuted().contains(this)) {
            return false;
        }

        // Check radius, locality
        if (getRadius() >= 0) {
            if (a.distanceSquaredTo(b) > getRadiusSquared()) { // It is much less expensive to take a power of 2 than of (1/2)
                return false;
            }
        }

        // Check cross worlds
        if (!isCrossWorlds()) {
            if (!pa.get().getWorld().equals(pb.get().getWorld())) {
                return false;
            }
        }

        return true;
    }

    public void onJoin(Member member) {
    }

    public void onLeave(Member member) {
    }

    public void onMessage(Member member, Text message) {
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Channel && name.equals(((Channel) obj).getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public enum EnterResult {
        SUCCESS, INSUFFICIENT_PERMISSIONS, NO_INVITATION, OTHER_FAILURE
    }

    public static class Manager extends MappedManager<String, Channel> {
    }
}
