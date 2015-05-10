package com.pqqqqq.directchat.channel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Kevin on 2015-05-09.
 * An {@link Iterable} {@link Set} of {@link Channel}s
 */
public class ChannelSet implements Iterable<Channel>, Cloneable {
    private Set<Channel> channels = new HashSet<Channel>();

    public Set<Channel> getChannels() {
        return this.channels;
    }

    public void add(Channel channel) {
        this.channels.add(channel);
    }

    public boolean remove(Channel channel) {
        return this.channels.remove(channel);
    }

    public Set<Channel> getDetectableChannels() {
        Set<Channel> channels = new HashSet<Channel>();
        for (Channel channel : this.channels) {
            if (!channel.isUndetectable()) {
                channels.add(channel);
            }
        }

        return channels;
    }

    public Set<PrivateChannel> getPrivateChannels() {
        Set<PrivateChannel> pchannels = new HashSet<PrivateChannel>();
        for (Channel channel : this.channels) {
            if (channel instanceof PrivateChannel) {
                pchannels.add((PrivateChannel) channel);
            }
        }

        return pchannels;
    }

    public boolean contains(Channel channel) {
        return this.channels.contains(channel);
    }

    public boolean isEmpty() {
        return this.channels.isEmpty();
    }

    public Channel[] toArray() {
        return this.channels.toArray(new Channel[this.channels.size()]);
    }

    public int size() {
        return this.channels.size();
    }

    public Iterator<Channel> iterator() {
        return channels.iterator();
    }

    @Override
    public ChannelSet clone() {
        ChannelSet newset = new ChannelSet();
        newset.getChannels().addAll(channels);

        return newset;
    }
}
