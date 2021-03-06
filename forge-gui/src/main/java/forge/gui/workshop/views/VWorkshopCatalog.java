package forge.gui.workshop.views;

import com.google.common.collect.Iterables;
import forge.Singletons;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.framework.IVDoc;
import forge.gui.match.controllers.CDetail;
import forge.gui.match.controllers.CPicture;
import forge.gui.toolbox.itemmanager.CardManager;
import forge.gui.toolbox.itemmanager.ItemManagerContainer;
import forge.gui.workshop.controllers.CCardScript;
import forge.gui.workshop.controllers.CWorkshopCatalog;
import forge.item.PaperCard;
import forge.util.ItemPool;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/** 
 * Assembles Swing components of card catalog in workshop.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 * 
 */
public enum VWorkshopCatalog implements IVDoc<CWorkshopCatalog> {
    /** */
    SINGLETON_INSTANCE;

    public static final int SEARCH_MODE_INVERSE_INDEX = 1;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Card Catalog");
    private final ItemManagerContainer cardManagerContainer = new ItemManagerContainer();
    private final CardManager cardManager;

    //========== Constructor
    /** */
    private VWorkshopCatalog() {
        this.cardManager = new CardManager(true);
        this.cardManager.setCaption("Catalog");
        Iterable<PaperCard> allCards = Iterables.concat(Singletons.getMagicDb().getCommonCards(), Singletons.getMagicDb().getVariantCards());
        this.cardManager.setPool(ItemPool.createFrom(allCards, PaperCard.class), true);
        this.cardManagerContainer.setItemManager(this.cardManager);

        this.cardManager.addSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                PaperCard card = cardManager.getSelectedItem();
                CDetail.SINGLETON_INSTANCE.showCard(card);
                CPicture.SINGLETON_INSTANCE.showImage(card);
                CCardScript.SINGLETON_INSTANCE.showCard(card);
            }
        });
    }

    //========== Overridden from IVDoc

    @Override
    public EDocID getDocumentID() {
        return EDocID.WORKSHOP_CATALOG;
    }

    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    @Override
    public CWorkshopCatalog getLayoutControl() {
        return CWorkshopCatalog.SINGLETON_INSTANCE;
    }

    @Override
    public void setParentCell(DragCell cell0) {
        this.parentCell = cell0;
    }

    @Override
    public DragCell getParentCell() {
        return this.parentCell;
    }

    @Override
    public void populate() {
        JPanel parentBody = parentCell.getBody();
        parentBody.setLayout(new MigLayout("insets 5, gap 0, wrap, hidemode 3"));
        parentBody.add(cardManagerContainer, "push, grow");
    }

    public CardManager getCardManager() {
        return cardManager;
    }
}
