package com.pqqqqq.directchat.channel.member;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.Config;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.ChannelSet;
import com.pqqqqq.directchat.channel.PrivateChannel;
import com.pqqqqq.directchat.util.MappedManager;
import com.pqqqqq.directchat.util.Utilities;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Kevin on 2015-05-03.
 * A wrapper for a Player's UUID
 */
public final class Member {
    private final String uuid;
    private Channel active = null;

    private Optional<Member> respond = Optional.<Member> absent();
    private final ChannelSet channels = new ChannelSet();

    // Cache
    private String lastCachedUsername;
    private Optional<Player> cachedPlayer = Optional.<Player> absent();
    private long lastActive = -1;

    // Extra data
    private Map<String, Object> extraData = new HashMap<String, Object>();

    // Admin
    private final SnooperData snooper;

    /**
     * Creates a {@link Member} instance from a UUID from {@link Player}'s UUID.
     * @param uuid the player's UUID
     */
    public Member(String uuid) {
        this.uuid = uuid;
        this.snooper = new SnooperData(this);

        // Perpetual channels
        for (Channel channel : DirectChat.plugin.getChannels().getMap().values()) {
            if (channel.isPerpetual() && channel.canEnter(this) == Channel.EnterResult.SUCCESS) {
                enterChannel(channel);
            }
        }

        enterChannel(Channel.def); // Default channel
        setLastActive();
    }

    /**
     * Creates a {@link Member} instance from a {@link Player}. Also caches the current username of the player.
     * @param player the player
     */
    public Member(Player player) {
        this(player.getUniqueId().toString());
        setLastCachedUsername(player.getName());
    }

    /**
     * Returns the UUID of the player
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Returns an {@link Optional} {@link Player} instance corresponding to the UUID. Returns a cached value if possible.
     * @return the player
     */
    public Optional<Player> getPlayer() {
        if (cachedPlayer.isPresent() && cachedPlayer.get().isLoaded() && !cachedPlayer.get().isRemoved()) {
            return cachedPlayer;
        }

        Optional<Player> player = DirectChat.game.getServer().getPlayer(UUID.fromString(uuid));
        if (player.isPresent()) {
            lastCachedUsername = player.get().getName();
        }

        return (cachedPlayer = player);
    }

    /**
     * Returns a {@link ChannelSet} of the channels the player is in
     * @return the channels
     */
    public ChannelSet getChannels() {
        return channels;
    }

    /**
     * Returns the active {@link Channel} of the player
     * @return the active channel
     */
    public Channel getActive() {
        return this.active;
    }

    /**
     * Sets the active {@link Channel} of the player
     * @param active the new active channel
     */
    public void setActive(Channel active) {
        this.active = active;
    }

    /**
     * Gets the {@link PrivateChannel} this player owns, or null if none
     * @return the private channel
     */
    @Nullable
    public PrivateChannel getOwnerChannel() {
        for (PrivateChannel channel : channels.getPrivateChannels()) {
            if (equals(channel.getOwner())) {
                return channel;
            }
        }

        return null;
    }

    /**
     * Enters the {@link Channel} and displays all messages associated with it.
     * @param channel the channel to enter
     * @return if the player successfully joined.
     */
    public final boolean enterChannel(Channel channel) {
        return enterChannel(channel, true);
    }

    /**
     * Enters the {@link Channel}.
     * @param channel the channel to enter
     * @param messages whether to display messages associated
     * @return if the player successfully joined
     */
    public final boolean enterChannel(Channel channel, boolean messages) {
        // Permission and invitation check
        Channel.EnterResult enterResult = channel.canEnter(this);
        switch (enterResult) {
            case SUCCESS:
                break;
            case INSUFFICIENT_PERMISSIONS:
                if (messages) {
                    sendMessage(Texts.of("You don't have sufficient permissions to enter this channel."));
                }
                return false;
            case NO_INVITATION:
                if (messages) {
                    sendMessage(Texts.of("You need an invitation to join a private channel."));
                }
                return false;
            default:
                return false;
        }

        // Broadcast on join
        if (messages && !channel.isSilent()) {
            if (channel.getFormat() != null && !channel.getFormat().isEmpty()) {
                sendMessage(Texts.of(channel.getFormattedJoinMessage())); // Print join message
            }

            if (Config.pcBroadcastOnJoin && channel instanceof PrivateChannel) {
                channel.broadcast(Texts.of(Utilities.formatColours("&f" + getLastCachedUsername() + " &bhas joined the channel: &f" + channel.getName())));
            }
        }

        channels.add(channel); // Add to set of channels in the member
        channel.getMembers().add(this); // Add to set of members in the channel
        this.active = channel; // Set as active channel
        channel.onJoin(this);

        setLastActive();
        return true;
    }

    /**
     * Leaves the {@link Channel} and displays any messages associated.
     * @param channel the channel to leave
     * @return if the leave was successful.
     */
    public final boolean leaveChannel(Channel channel) {
        return leaveChannel(channel, true);
    }

    /**
     * Leaves the {@link Channel}.
     * @param channel the channel to leave
     * @param messages whether to display the associated messages
     * @return if the leave was successful
     */
    public final boolean leaveChannel(Channel channel, boolean messages) {
        if (channel.isPerpetual()) {
            return false;
        }

        if (!channels.contains(channel)) {
            if (messages) {
                sendMessage(Texts.of("You are not in this channel."));
            }
            return false;
        }

        channels.remove(channel);
        channel.getMembers().remove(this);
        channel.onLeave(this);

        if (channels.isEmpty()) {
            enterChannel(Channel.def, messages);
        } else if (active == null || active != null && active.equals(channel)) {
            active = channels.toArray()[channels.size() - 1];
        }

        if (channel instanceof PrivateChannel) {
            PrivateChannel pc = (PrivateChannel) channel;
            if (equals(pc.getOwner())) {
                pc.disintegrate();
            }
        }

        setLastActive();
        return true;
    }

    /**
     * Sends a message to this player.
     * @param message the message to send
     * @return whether a message could be sent
     */
    public boolean sendMessage(Text message) {
        Optional<Player> player = getPlayer();
        if (player.isPresent()) {
            player.get().sendMessage(message);
            return true;
        }

        return false;
    }

    /**
     * Returns if the player is currently online.
     * @return true if the player in online
     */
    public boolean isOnline() {
        return getPlayer().isPresent();
    }

    /**
     * Calculates the squared distance to another {@link Member}.
     * @param member the member to calculate the distance to
     * @return the distance squared, or -1 if uncalcuable
     */
    public double distanceSquaredTo(Member member) {
        Optional<Player> first = getPlayer();
        Optional<Player> second = member.getPlayer();

        if (!first.isPresent() || !second.isPresent() || !first.get().getWorld().equals(second.get().getWorld())) {
            return -1;
        }

        return first.get().getLocation().getPosition().distanceSquared(second.get().getLocation().getPosition());
    }

    /**
     * Gets the response member for PMing
     * @return the response member
     */
    public Optional<Member> getRespond() {
        return respond;
    }

    /**
     * Sets the reponse member for PMing
     * @param respond the new response member
     */
    public void setRespond(Optional<Member> respond) {
        this.respond = respond;
    }

    /**
     * Gets the {@link SnooperData} for the player.
     * @return the snooper data
     */
    public SnooperData getSnooperData() {
        return snooper;
    }

    /**
     * Gets the last cached username of this member.
     * @return the last cached username
     */
    public String getLastCachedUsername() {
        return lastCachedUsername;
    }

    /**
     * Sets the last cached username of this player.
     * @param lastCachedUsername the new last cached username
     */
    public void setLastCachedUsername(String lastCachedUsername) {
        this.lastCachedUsername = lastCachedUsername;
    }

    /**
     * Gets millis since the epoch when this member was last active.
     * @return the time since last active
     * @see System#currentTimeMillis()
     */
    public long getLastActive() {
        return lastActive;
    }

    /**
     * Sets millis since the epoch when this member was last active to the current time.
     * @see System#currentTimeMillis()
     */
    public void setLastActive() {
        this.lastActive = System.currentTimeMillis();
    }

    /**
     * Sets millis since the epoch when this member was last active.
     * @param lastActive the new time since last active
     * @see System#currentTimeMillis()
     */
    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    /**
     * Returns whether this member is considered active, based off of the config.
     * @return if the member is active
     * @see Config#millisLastActive
     */
    public boolean isActive() {
        return (System.currentTimeMillis() - this.lastActive) < Config.millisLastActive;
    }

    /**
     * Gets a {@link Map} of any additional data for the {@link Member} that any plugin hooking into DirectChat may want to utilize.
     * @return the extra data map
     */
    public Map<String, Object> getExtraData() {
        return extraData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof Member) {
            return ((Member) obj).getUuid().equals(uuid);
        }

        if (obj instanceof Player) {
            return ((Player) obj).getUniqueId().equals(uuid);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public static class Manager extends MappedManager<String, Member> {

        public void leaveAllChannels() {
            for (Member member : getMap().values()) {
                for (Channel channel : member.getChannels()) {
                    member.leaveChannel(channel, false);
                }
            }
        }
    }
}
