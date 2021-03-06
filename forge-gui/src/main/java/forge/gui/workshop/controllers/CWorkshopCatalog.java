package forge.gui.workshop.controllers;

import forge.UiCommand;
import forge.gui.framework.ICDoc;
import forge.gui.toolbox.itemmanager.ItemManagerConfig;
import forge.gui.workshop.views.VWorkshopCatalog;

/** 
 * Controls the "card catalog" panel in the workshop UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CWorkshopCatalog implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    private CWorkshopCatalog() {
    }

    //========== Overridden methods

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public UiCommand getCommandOnSelect() {
        return null;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#initialize()
     */
    @Override
    public void initialize() {
    }
    
    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
        VWorkshopCatalog.SINGLETON_INSTANCE.getCardManager().setup(ItemManagerConfig.WORKSHOP_CATALOG);
        //TODO: Restore previously selected card
    }
}
