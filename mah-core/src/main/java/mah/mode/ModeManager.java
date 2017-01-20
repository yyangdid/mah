package mah.mode;

import mah.action.Action;
import mah.action.ActionException;
import mah.action.ActionHandler;
import mah.action.GlobalAction;
import mah.app.ApplicationEvent;
import mah.app.ApplicationListener;
import mah.app.config.Config;
import mah.app.config.XMLConfig;
import mah.keybind.Keybind;
import mah.keybind.KeybindManager;
import mah.keybind.config.KeybindConfig;
import mah.keybind.util.SimpleParser;
import mah.mode.config.ModeConfig;
import mah.mode.config.XMLModeParser;
import org.w3c.dom.Document;

import javax.swing.*;
import java.util.*;

/**
 * Created by zgq on 2017-01-08 20:34
 */
public class ModeManager implements ApplicationListener {

    private static final ModeManager INSTANCE = new ModeManager();
    private final Map<String, Mode> MODES = new HashMap<>();

    private ModeManager() {}

    public static ModeManager getInstance() {
        return INSTANCE;
    }

    public void triggerMode(Mode mode) {
        triggerMode(mode.getName());
    }

    public void triggerMode(String mod) {
        Mode mode = getMode(mod);
        if (mode == null) {
            throw new ModeException("Not found mode " + mod);
        }
        KeybindManager.getInstance().setCurrentMode(mode);
    }

    public Mode getMode(String mod) {
        return MODES.get(mod);
    }

    public void registerMode(Mode mode) {
        mode.init();
        MODES.put(mode.getName(), mode);
    }

    public void registerMode(Mode mode, ActionHandler actionHandler) {
        mode.init();
        mode.updateActionHandler(actionHandler);
        MODES.put(mode.getName(), mode);
    }

    public Mode registerOrUpdateMode(Mode mode, ActionHandler actionHandler) {
        Mode existedMode = MODES.get(mode.getName());
        if (existedMode == null) {
            registerMode(mode, actionHandler);
            return null;
        } else {
            existedMode.updateActionHandler(actionHandler);
        }
        return existedMode;
    }

    public Action findGlobalAction(String actionName) {
        Set<Map.Entry<String, Mode>> entries = MODES.entrySet();
        Action action = null;
        for (Map.Entry<String, Mode> entry : entries) {
            Mode mode = entry.getValue();
            action = mode.findAction(actionName);
            if (action != null && action instanceof GlobalAction) {
                break;
            }
        }
        if (action == null) {
            throw new ActionException("Not found global action " + actionName);
        }
        return action;
    }

    public Mode getOrRegisterMode(Mode mode) {
        Mode existedMode = getMode(mode.getName());
        if (existedMode == null) {
            registerMode(mode);
            return mode;
        }
        return existedMode;
    }

    @Override
    public void afterStart(ApplicationEvent event) {
        Config config = event.getConfig();
        if (config instanceof XMLConfig) {
            XMLConfig xmlConfig = (XMLConfig) config;
            registerModeKeybinds(xmlConfig.getDocument());
        }
    }

    private void registerModeKeybinds(Document document) {
        XMLModeParser xmlModeParser = new XMLModeParser(document);
        List<ModeConfig> modeConfigs = xmlModeParser.parseModeConfigs();
        modeConfigs.forEach(modeConfig -> {
            HashSet<KeybindConfig> keybinds = modeConfig.getKeybinds();
            for (KeybindConfig keybindConfig : keybinds) {
                Keybind keybind = new Keybind();
                Mode mode = MODES.get(modeConfig.getName());
                if (mode == null) {
                    throw new ModeException("Not found mode " + modeConfig.getName());
                }
                Action action = mode.findAction(keybindConfig.getAction());
                keybind.setAction(action);
                List<KeyStroke> keyStrokes = SimpleParser.parse(keybindConfig.getBind());
                keybind.setKeyStrokes(keyStrokes);
                KeybindManager.getInstance().addKeybind(modeConfig.getName(), keybind);
            }
        });
    }
}
