package com.pqqqqq.directchat;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.pqqqqq.directchat.channel.Channel;
import com.pqqqqq.directchat.channel.member.Member;
import com.pqqqqq.directchat.commands.*;
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
        commandService.register(this, new CommandSnooper(this), "snooper");
        commandService.register(this, new CommandPM(this), "pm", "message", "m", "whisper", "tell", "w", "msg", "t", "r", "respond");
        commandService.register(this, new CommandPrivateChannels(this), "p", "private", "pc");
        commandService.register(this, new CommandJoin(this), "join", "j", "jc");
        commandService.register(this, new CommandSelect(this), "select", "s", "sc");
        commandService.register(this, new CommandLeave(this), "leave", "l", "lc");
        commandService.register(this, new CommandDirectChat(this), "dc", "directchat");
        commandService.register(this, new CommandAdminChat(this), "admin", "a");

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
