package com.pqqqqq.directchat;

import com.google.common.base.Function;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.util.Utilities;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Created by Kevin on 2015-05-03.
 */
public class Config {
    private File file;
    private ConfigurationLoader<CommentedConfigurationNode> configManager;
    private DirectChat plugin;

    public static List<String> blacklistedWords;
    public static List<String> whitelistedURLs;
    public static long millisLastActive;
    public static boolean soundOnMention;

    public static String pcJoinMessage;
    public static String pcFormat;
    public static boolean pcBroadcastOnJoin;

    public static String whisperSendFormat;
    public static String whisperReceiveFormat;
    public static String whisperSnooperFormat;

    public Config(DirectChat plugin, File file, ConfigurationLoader<CommentedConfigurationNode> configManager) {
        this.plugin = plugin;
        this.file = file;
        this.configManager = configManager;
    }

    public void init() {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            plugin.getMembers().leaveAllChannels();
            Channel.def = null;
            Channel.admin = null;
            plugin.getChannels().clear();

            CommentedConfigurationNode root = configManager.load(ConfigurationOptions.defaults().setHeader("The main configuration for the DirectChat Sponge plugin."));

            // Channels
            int i = 0;
            Collection<? extends CommentedConfigurationNode> col = getNodeAndComment(root, "The list of preset channels", "channels").getChildrenMap().values();

            for (CommentedConfigurationNode c : col) {
                boolean last = (i++ == col.size() - 1);

                Channel channel = new Channel(c.getKey().toString());
                channel.setDefChannel(getNodeAndComment(last, c, "Whether this channel is the default channel. There should only be one default channel.", "default").getBoolean(false));
                channel.setCrossWorlds(getNodeAndComment(last, c, "Whether this channel's members can send messages in the channel across worlds. If a radius is set, this is negligible.", "cross-worlds").getBoolean(true));
                channel.setSilent(getNodeAndComment(last, c, "Whether this channel is silent, or doesn't send messages to its players.", "silent").getBoolean(false));
                channel.setPerpetual(getNodeAndComment(last, c, "Whether this channel is perpetual, or is always active for users who can join it.", "perpetual").getBoolean(false));
                channel.setUndetectable(getNodeAndComment(last, c, "Whether this channel is undetectable, or cannot be found by players.", "undetectable").getBoolean(false));
                channel.setLeaveOnExit(getNodeAndComment(last, c, "Whether this channel is left by the user when they leave the server.", "leave-on-exit").getBoolean(false));
                channel.setFormat(Utilities.formatColours(getNodeAndComment(last, c,
                        "The format as to which chat messages for members chatting in this group are given." +
                                "\n%PLAYERNAME% - The name of the player sending the message." +
                                "\n%MESSAGE% - The message that the player sent." +
                                "\n%CHANNEL% - The channel the player is sending the chat into." +
                                "\n%PREFIX% - The prefix of the player, according to PermissionsEx." +
                                "\n%SUFFIX% - The suffix of the player, according to PermissionsEx." +
                                "\n*INACTIVE* %DISPLAYNAME% - The display name of the sender.", "format").getString("%PLAYERNAME%: %MESSAGE%")));
                channel.setRadius(getNodeAndComment(last, c, "The radius, or locality of the channel. Anything less than 0 specifies a global channel.", "radius").getInt(-1));

                channel.setJoinMessage(Utilities.formatColours(getNodeAndComment(last, c,
                        "The message sent to a player joining this channel." +
                                "\n%CHANNEL% The channel they are joining.", "enterance", "joinMessage").getString("You have joined %CHANNEL%")));
                channel.setPermissions(getNodeAndComment(last, c, "The permissions needed to join. None means no permissions are needed.", "enterance", "permissions").getList(new Function<Object, String>() {

                    @Nullable
                    public String apply(Object obj) {
                        return obj == null ? null : obj.toString();
                    }
                }));

                plugin.getChannels().add(c.getKey().toString(), channel);

                if (channel.isDefChannel()) {
                    if (Channel.def != null) {
                        plugin.getLogger().error("Two default channels have been specified in the config. Please only specify one.");
                    }

                    Channel.def = channel;
                } else if (getNodeAndComment(last, c, "Whether this channel is the admin channel. There should only be one admin channel.", "admin").getBoolean(false)) {
                    if (Channel.admin != null) {
                        plugin.getLogger().error("Two admin channels have been specified in the config. Please only specify one.");
                    }

                    Channel.admin = channel;
                }
            }

            // General
            CommentedConfigurationNode general = getNodeAndComment(root, "General configuration stuff.", "general");
            blacklistedWords = getNodeAndComment(general, "Words in the server to be blocked when said.", "blacklisted-words").getList(new Function<Object, String>() {

                @Nullable
                public String apply(Object obj) {
                    return obj == null ? null : obj.toString();
                }
            });

            whitelistedURLs = getNodeAndComment(general, "URLs which are allowed to be said by users.", "whitelisted-urls").getList(new Function<Object, String>() {

                @Nullable
                public String apply(Object obj) {
                    return obj == null ? null : obj.toString();
                }
            });
            millisLastActive = getNodeAndComment(general, "The amount of milliseconds for a player to be considered inactive", "last-active-millis").getLong(600000);
            soundOnMention = getNodeAndComment(general, "Make a sound when a player's name is mentioned in chat", "sound-on-mention").getBoolean(true);

            // Private channels
            CommentedConfigurationNode pc = getNodeAndComment(root, "Private channel settings.", "private-channel");
            pcJoinMessage = Utilities.formatColours(getNodeAndComment(pc,
                    "The generic join message for private channels. This inherits all placeholders from channel join message, plus:" +
                            "\n%OWNER% - The owner of the private channel.", "join-message").getString("You have joined %CHANNEL% owned by %OWNER%"));
            pcFormat = Utilities.formatColours(getNodeAndComment(pc, "The generic format for private channels. This inherits all placeholders from channel format.", "format").getString("[PM] %PLAYERNAME%: %MESSAGE%"));
            pcBroadcastOnJoin = getNodeAndComment(pc, "Whether to broadcast to all members in the private channel when a new member joins.", "broadcast-on-join").getBoolean(true);

            // Whisper
            CommentedConfigurationNode whisper = getNodeAndComment(root, "Whispering/PM settings", "whisper");
            CommentedConfigurationNode whisperFormat = getNodeAndComment(whisper,
                    "Formatting options for whispering. For all formats:" +
                            "\n%SENDER% - The sender of the whisper." +
                            "\n%RECEIVER% - The receiver of the whisper." +
                            "\n%MESSAGE% - The message sent.", "format");
            whisperSendFormat = Utilities.formatColours(getNodeAndComment(whisperFormat, "The format the whisper sender sees.", "send").getString("[To %RECEIVER%]: %MESSAGE%"));
            whisperReceiveFormat = Utilities.formatColours(getNodeAndComment(whisperFormat, "The format the whisper receiver sees.", "receive").getString("[From %SENDER%]: %MESSAGE%"));
            whisperSnooperFormat = Utilities.formatColours(getNodeAndComment(whisperFormat, "The format a snooping admin sees.", "snooper").getString("[%SENDER% -> %RECEIVER%]: %MESSAGE%"));

            configManager.save(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CommentedConfigurationNode getNodeAndComment(boolean last, CommentedConfigurationNode root, String comment, Object... path) {
        CommentedConfigurationNode node = root.getNode(path);

        if (last) {
            node.setComment(comment);
        } else {
            node.setComment(null);
        }

        return node;
    }

    public CommentedConfigurationNode getNodeAndComment(CommentedConfigurationNode root, String comment, Object... path) {
        return getNodeAndComment(true, root, comment, path);
    }
}
