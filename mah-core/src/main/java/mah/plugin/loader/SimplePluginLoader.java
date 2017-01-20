package mah.plugin.loader;

import mah.app.ApplicationManager;
import mah.app.config.Config;
import mah.command.Command;
import mah.command.CommandManager;
import mah.command.event.EventHandler;
import mah.command.event.InitializeEvent;
import mah.plugin.Plugin;
import mah.plugin.PluginException;
import mah.plugin.PluginMetainfo;
import mah.plugin.command.PluginCommand;
import mah.plugin.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by zgq on 2017-01-08 19:51
 */
public class SimplePluginLoader {

    private final XMLConfigReader reader;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutor;
    private final Logger logger = LoggerFactory.getLogger(SimplePluginLoader.class);

    public SimplePluginLoader(Config config) throws Exception {
        this.reader = new XMLConfigReader(ApplicationManager.getInstance().getPluginDir(), config);
        executorService = Executors.newCachedThreadPool();
        scheduledExecutor = Executors.newScheduledThreadPool(2);
    }

    private Plugin loadPlugin(PluginMetainfo pluginMetainfo) throws Exception {
        Class<?> clazz = pluginMetainfo.getPluginLoader().loadClass(pluginMetainfo.getPluginClass());
        Object pluginObj = clazz.newInstance();
        if (pluginObj instanceof Plugin) {
            return (Plugin) pluginObj;
        } else {
            throw new PluginException("Plugin is supposed to implement the interface of Plugin");
        }
    }

    private void setPluginDir(PluginMetainfo pluginMetainfo) {
        pluginMetainfo.setPluginDataDir(ApplicationManager.getInstance().getPluginDataDir() + File.separator + pluginMetainfo.getName());
    }

    public final List<Plugin> loadPlugins(List<String> pluginNames) {
        List<PluginMetainfo> activePluginMetainfos = reader.getPluginMetainfos(pluginNames);
        List<Plugin> plugins;
        plugins = new ArrayList<>();
        for (PluginMetainfo pluginMetainfo : activePluginMetainfos) {
            Class<?> clazz = null;
            try {
                setPluginDir(pluginMetainfo);
                Plugin plugin = loadPlugin(pluginMetainfo);
                plugin.setPluginMetainfo(pluginMetainfo);
                plugins.add(plugin);
            } catch (Exception e) {
                throw new PluginException("Unable to loadPlugins plugin " + clazz, e);
            }
        }
        return plugins;
    }

    private Map<String, List<CommandConfig>> loadCommands(List<String> pluginNames) {
        List<? extends PluginConfig> pluginConfigs = reader.parsePluginConfigs(pluginNames);
        Map<String, List<CommandConfig>> pluginToCommandConfigs = new HashMap<>();
        for (PluginConfig pluginConfig : pluginConfigs) {
            List<CommandConfig> commandConfigs = new ArrayList<>();
            List<? extends CommandConfig> xmlCommandConfigs = pluginConfig.getCommandConfigs();
            for (CommandConfig commandConfig : xmlCommandConfigs) {
                commandConfigs.add(commandConfig);
            }
            pluginToCommandConfigs.put(pluginConfig.getName(), commandConfigs);
        }
        return pluginToCommandConfigs;
    }

    private Command findCommand(CommandConfig commandConfig) {
        String commandName = commandConfig.getCommandName();
        Command command = CommandManager.getInstance().findCommand(commandConfig.getPluginName(), commandName);
        if (command == null) {
            throw new PluginException("Not found command " + commandName);
        }
        return command;
    }

    public final void start() {
        List<String> activePlugins = reader.getActivePluginNames();
        List<Plugin> plugins = loadPlugins(activePlugins);
        Map<String, List<CommandConfig>> pluginToCommands = loadCommands(activePlugins);
        for (Plugin plugin : plugins) {
            List<CommandConfig> commandConfigs = pluginToCommands.get(plugin.getName());
            Future<?> loadTask = executorService.submit(() -> {
                try {
                    long start = System.nanoTime();
                    startPlugin(plugin, commandConfigs);
                    long end = System.nanoTime();
                    logger.info("Loading plugin {} took {} nanoseconds", plugin, end - start);
                } catch (Exception e) {
                    logger.error("Plugin failed to start {}",e);
                }
            });
            scheduledExecutor.schedule(() -> {
                        if (!loadTask.isDone()) {
                            loadTask.cancel(true);
                            logger.error("Plugin {} took too much time to load,already canceled", plugin);
                        }
                    }
                    , 50000, TimeUnit.MILLISECONDS);
        }

        try {
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new PluginException("Plugins took too much time to load,failing to startup app");
        }
    }


    private void startPlugin(Plugin plugin, List<CommandConfig> commandConfigs) throws Exception {
        checkPluginDir(plugin);
        plugin.init();
        plugin.prepare();
        for (CommandConfig commandConfig : commandConfigs) {
            Command command = findCommand(commandConfig);
            startCommand(plugin, command, commandConfig);
            CommandManager.getInstance().mapCommand(commandConfig.getTriggerKey(), command);
        }
    }

    private void checkPluginDir(Plugin plugin) {
        String pluginDataDir = plugin.getPluginMetainfo().getPluginDataDir();
        File pluginData = new File(pluginDataDir);
        if (!pluginData.exists()) {
            pluginData.mkdirs();
        }
    }

    private void startCommand(Plugin plugin, Command command, CommandConfig commandConfig) {
        configureCommand(command, commandConfig);
        try {
            if (command instanceof PluginCommand) {
                PluginCommand pluginCommand = (PluginCommand) command;
                pluginCommand.setPlugin(plugin);
            }

            List<EventHandler<? extends InitializeEvent>> initializeHandlers = command.getInitializeHandlers();
            InitializeEvent initializeEvent = new InitializeEvent();
            for (EventHandler initializeHandler : initializeHandlers) {
                initializeHandler.handle(initializeEvent);
            }
        } catch (Exception e) {
            throw new PluginException(e);
        }
    }

    private void configureCommand(Command command, CommandConfig commandConfig) {
        if (command instanceof XMLConfigurable) {
            XMLConfigurable xmlConfigurable = (XMLConfigurable) command;
            XMLCommandConfig xmlCommandConfig = (XMLCommandConfig) commandConfig;
            try {
                xmlConfigurable.configure(xmlCommandConfig.getCustomConfig());
            } catch (Exception e) {
                throw new PluginException(e);
            }
        }
    }

}
