package com.pqqqqq.directchat;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.commands.*;
import com.pqqqqq.directchat.commands.directchat.CommandInfo;
import com.pqqqqq.directchat.commands.directchat.CommandReload;
import com.pqqqqq.directchat.commands.mute.CommandMuteChannel;
import com.pqqqqq.directchat.commands.mute.CommandMuteInvite;
import com.pqqqqq.directchat.commands.privatechannel.CommandCreate;
import com.pqqqqq.directchat.commands.privatechannel.CommandInvite;
import com.pqqqqq.directchat.commands.privatechannel.CommandKick;
import com.pqqqqq.directchat.events.CoreEvents;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.util.command.dispatcher.SimpleDispatcher;

import java.io.File;

/**
 * Created by Kevin on 2015-05-03.
 * The main plugin container class.
 */
@Plugin(id = DirectChat.ID, name = DirectChat.NAME, version = DirectChat.VERSION)
public class DirectChat {
    public static final String ID = "directchat";
    public static final String NAME = "DirectChat";
    public static final String VERSION = "0.1 BETA";

    public static Game game;
    public static DirectChat plugin;

    private Member.Manager members;
    private Channel.Manager channels;

    private Optional<PluginContainer> pex;

    private Config cfg;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private File file;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    private Logger logger;

    @Inject
    public DirectChat(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    @Subscribe
    public void init(InitializationEvent event) {
        plugin = this;
        game = event.getGame();

        // Register events
        game.getEventManager().register(this, new CoreEvents(this));

        // Register commands
        CommandService commandService = game.getCommandDispatcher();

        commandService.register(this, CommandSnooper.build(this), "snooper");
        commandService.register(this, CommandPM.build(this), "pm", "message", "m", "whisper", "tell", "w", "msg", "t");
        commandService.register(this, CommandRespond.build(this), "r", "respond");
        commandService.register(this, CommandJoin.build(this), "join", "j", "jc");
        commandService.register(this, CommandSelect.build(this), "select", "s", "sc");
        commandService.register(this, CommandLeave.build(this), "leave", "l", "lc");
        commandService.register(this, CommandAdminChat.build(this), "admin", "a");

        // Private channels
        SimpleDispatcher privateChannelDispatcher = new SimpleDispatcher();
        privateChannelDispatcher.register(CommandCreate.build(this), "create", "c");
        privateChannelDispatcher.register(CommandInvite.build(this), "invite", "inv");
        privateChannelDispatcher.register(CommandKick.build(this), "kick");

        // Mute
        SimpleDispatcher muteDispatcher = new SimpleDispatcher();
        muteDispatcher.register(CommandMuteChannel.build(this), "channel", "ch");
        muteDispatcher.register(CommandMuteInvite.build(this), "invite", "inv");

        // Direct chat commands
        SimpleDispatcher dcDispatcher = new SimpleDispatcher();
        dcDispatcher.register(CommandInfo.build(this), "info", "inf");
        dcDispatcher.register(CommandReload.build(this), "reload");

        commandService.register(this, privateChannelDispatcher, "pc", "privatechannel", "pchannel", "privatechannels", "privatec", "p");
        commandService.register(this, muteDispatcher, "mutechannel", "mutec");
        commandService.register(this, dcDispatcher, "dc", "directchat");

        // Instantiate managers
        members = new Member.Manager();
        channels = new Channel.Manager();

        // Config
        cfg = new Config(this, file, configManager);
        cfg.init();
        cfg.load();

        // Hook to pex
        pex = game.getPluginManager().getPlugin("permissionsex");
    }

    public Game getGame() {
        return game;
    }

    public Member.Manager getMembers() {
        return members;
    }

    public Channel.Manager getChannels() {
        return channels;
    }

    public Optional<PluginContainer> getPEX() {
        return pex;
    }

    public Config getCfg() {
        return cfg;
    }
}
