package com.pqqqqq.directchat.channel.member;

import com.google.common.base.Optional;
import org.spongepowered.api.entity.player.Player;

/**
 * Created by Kevin on 2015-05-04.
 */
public class SnooperData {
    private boolean priv = false;
    private boolean pub = false;
    private boolean whisper = false;

    public SnooperData(Member member) {
        Optional<Player> player = member.getPlayer();
        if (player.isPresent()) {
            if (player.get().hasPermission("directchat.snooper.whisper")) {
                this.whisper = true;
            }

            if (player.get().hasPermission("directchat.snooper.public")) {
                this.pub = true;
            }

            if (player.get().hasPermission("directchat.snooper.private")) {
                this.priv = true;
            }
        }
    }

    public boolean isPrivate() {
        return priv;
    }

    public void setPrivate(boolean priv) {
        this.priv = priv;
    }

    public boolean isPublic() {
        return pub;
    }

    public void setPublic(boolean pub) {
        this.pub = pub;
    }

    public boolean isWhisper() {
        return whisper;
    }

    public void setWhisper(boolean whisper) {
        this.whisper = whisper;
    }

    @Override
    public String toString() {
        return "Private=" + priv + ";Public=" + pub + ";Whisper=" + whisper;
    }
}
