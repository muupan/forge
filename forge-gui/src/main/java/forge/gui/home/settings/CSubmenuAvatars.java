package forge.gui.home.settings;

import forge.UiCommand;
import forge.gui.framework.ICDoc;

import javax.swing.*;

/** 
 * Controls the avatars submenu in the home UI.
 */
public enum CSubmenuAvatars implements ICDoc {
    SINGLETON_INSTANCE;

    private final VSubmenuAvatars view = VSubmenuAvatars.SINGLETON_INSTANCE;

    @Override
    public void initialize() {
    }

    @Override
    public void update() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
            	view.refreshAvatarFromPrefs(0);
            	view.refreshAvatarFromPrefs(1);
            	view.focusHuman(); }
        });
    }

    @Override
    public UiCommand getCommandOnSelect() {
        return null;
    }
}
