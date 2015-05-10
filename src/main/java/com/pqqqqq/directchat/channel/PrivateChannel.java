package com.pqqqqq.directchat.channel;

import com.google.common.base.Optional;
import com.pqqqqq.directchat.Config;
import com.pqqqqq.directchat.DirectChat;
import com.pqqqqq.directchat.channel.member.Member;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Texts;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kevin on 2015-05-04.
 */
public class PrivateChannel extends Channel {
    private Member owner;
    private Set<Member> invitations = new HashSet<Member>();

    public PrivateChannel(String name, Member owner) {
        super(name);
        this.owner = owner;

        setFormat(Config.pcFormat);
        setJoinMessage(Config.pcJoinMessage);
    }

    public Member getOwner() {
        return owner;
    }

    public Set<Member> getInvitations() {
        return invitations;
    }

    public void disintegrate() {
        disintegrate(true);
    }

    public void disintegrate(boolean messages) {
        Set<Member> members = new HashSet<Member>();
        members.addAll(getMembers());

        for (Member member : members) {
            if (messages) {
                member.sendMessage(Texts.of("This private channel disintegrated since the owner left."));
            }
            member.leaveChannel(this, messages);
        }

        DirectChat.plugin.getChannels().remove(getName());
    }

    @Override
    public String formatMessage(Player sender, Member member, String message) {
        String format = super.formatMessage(sender, member, message);
        format = format.replace("%OWNER%", owner.getLastCachedUsername());

        return format;
    }

    @Override
    public String getFormattedJoinMessage() {
        String superM = super.getFormattedJoinMessage();

        if (superM == null || superM.isEmpty()) {
            return null;
        }

        Optional<Player> ownp = owner.getPlayer();
        return superM.replace("%OWNER%", (ownp.isPresent() ? ownp.get().getName() : ""));
    }

    @Override
    public EnterResult canEnter(Member member) {
        EnterResult superResult = super.canEnter(member);
        if (superResult != EnterResult.SUCCESS) {
            return superResult;
        }

        if (!owner.equals(member) && !invitations.contains(member)) {
            return EnterResult.NO_INVITATION;
        }

        return EnterResult.SUCCESS;
    }
}
