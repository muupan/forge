package forge.screens.match;

import com.google.common.eventbus.Subscribe;

import forge.FThreads;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.event.*;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.zone.PlayerZone;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;
import forge.net.FServer;
import forge.screens.match.views.VPhaseIndicator.PhaseLabel;
import forge.screens.match.winlose.ViewWinLose;
import forge.toolbox.GuiChoose;
import forge.util.Lang;
import forge.util.maps.MapOfLists;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class FControlGameEventHandler extends IGameEventVisitor.Base<Void> {
    public FControlGameEventHandler() {
    }

    @Subscribe
    public void receiveGameEvent(final GameEvent ev) {
        ev.visit(this);
    }

    private final AtomicBoolean phaseUpdPlanned = new AtomicBoolean(false);
    @Override
    public Void visit(final GameEventTurnPhase ev) {
        if (phaseUpdPlanned.getAndSet(true)) return null;

        FThreads.invokeInEdtNowOrLater(new Runnable() { @Override public void run() {
            PhaseHandler pH = FControl.getGame().getPhaseHandler();
            Player p = pH.getPlayerTurn();
            PhaseType ph = pH.getPhase();

            phaseUpdPlanned.set(false);

            PhaseLabel lbl = FControl.getPlayerPanel(p).getPhaseIndicator().getLabel(ph);

            FControl.resetAllPhaseButtons();
            if (lbl != null) { lbl.setActive(true); }
        } });
        return null;
    }

    /* (non-Javadoc)
     * @see forge.game.event.IGameEventVisitor.Base#visit(forge.game.event.GameEventPlayerPriority)
     */
    private final AtomicBoolean combatUpdPlanned = new AtomicBoolean(false);
    @Override
    public Void visit(GameEventPlayerPriority event) {
        if (combatUpdPlanned.getAndSet(true)) { return null; }
        FThreads.invokeInEdtNowOrLater(new Runnable() {
            @Override
            public void run() {
                combatUpdPlanned.set(false);
                FControl.showCombat(FControl.getGame().getCombat());
            }
        });
        return null;
    }

    private final AtomicBoolean turnUpdPlanned = new AtomicBoolean(false);
    @Override
    public Void visit(final GameEventTurnBegan event) {
        if (turnUpdPlanned.getAndSet(true)) { return null; }

        final Game game = FControl.getGame(); // to make sure control gets a correct game instance
        FThreads.invokeInEdtNowOrLater(new Runnable() {
            @Override
            public void run() {
                /*VField nextField = FControl.getFieldViewFor(event.turnOwner);
                SDisplayUtil.showTab(nextField);*/

                turnUpdPlanned.set(false);
                FControl.getView().getPrompt().updateText(game);
            }
        });
        return null;
    }

    @Override
    public Void visit(GameEventAnteCardsSelected ev) {
        // Require EDT here?
        List<Object> options = new ArrayList<Object>();
        for (final Entry<Player, Card> kv : ((GameEventAnteCardsSelected) ev).cards.entries()) {
            options.add("  -- From " + Lang.getPossesive(kv.getKey().getName()) + " deck --");
            options.add(kv.getValue());
        }
        GuiChoose.one("These cards were chosen to ante", options);
        return null;
    }

    @Override
    public Void visit(GameEventPlayerControl ev) {
        if (FControl.getGame().isGameOver()) {
            return null;
        }

        FThreads.invokeInEdtNowOrLater(new Runnable() { @Override public void run() {
            /*FControl.initHandViews(FServer.getLobby().getGuiPlayer());
            SLayoutIO.loadLayout(null);
            VMatchUI.SINGLETON_INSTANCE.populate();
            for (VHand h : VMatchUI.SINGLETON_INSTANCE.getHands()) {
                h.getLayoutControl().updateHand();
            }*/
        } });
        return null;
    }

    private final Runnable unlockGameThreadOnGameOver = new Runnable() {
        @Override
        public void run() {
            FControl.getInputQueue().onGameOver(true); // this will unlock any game threads waiting for inputs to complete
        }
    };

    @Override
    public Void visit(GameEventGameOutcome ev) {
        FThreads.invokeInEdtNowOrLater(unlockGameThreadOnGameOver);
        return null;
    }

    @Override
    public Void visit(GameEventGameFinished ev) {
        FThreads.invokeInEdtNowOrLater(new Runnable() {
            @Override
            public void run() {
                new ViewWinLose(FControl.getGame()).setVisible(true);
            }
        });
        return null;
    }

    private final AtomicBoolean stackUpdPlanned = new AtomicBoolean(false);
    private final Runnable updStack = new Runnable() {
        @Override
        public void run() {
            stackUpdPlanned.set(false);
            FControl.getView().getStack().update();
        }
    };

    @Override
    public Void visit(GameEventSpellAbilityCast event) {
        if (!stackUpdPlanned.getAndSet(true)) {
            FThreads.invokeInEdtNowOrLater(updStack);
        }
        return null;
    }
    @Override
    public Void visit(GameEventSpellResolved event) {
        if (!stackUpdPlanned.getAndSet(true)) {
            FThreads.invokeInEdtNowOrLater(updStack);
        }
        return null;
    }
    @Override
    public Void visit(GameEventSpellRemovedFromStack event) {
        if (!stackUpdPlanned.getAndSet(true)) {
            FThreads.invokeInEdtNowOrLater(updStack);
        }
        return null;
    }

    private final List<Pair<Player, ZoneType>> zonesToUpdate = new Vector<Pair<Player, ZoneType>>();
    private final Runnable updZones = new Runnable() {
        @Override public void run() {
            synchronized (zonesToUpdate) {
                FControl.updateZones(zonesToUpdate);
                zonesToUpdate.clear();
            }
        }
    };

    @Override
    public Void visit(GameEventZone event) {
        if (event.player != null) {
            // anything except stack will get here
            updateZone(Pair.of(event.player, event.zoneType));
        }
        return null;
    }

    @Override
    public Void visit(GameEventCardAttachment event) {
        // TODO Auto-generated method stub
        Game game = event.equipment.getGame();
        PlayerZone zEq = (PlayerZone)game.getZoneOf(event.equipment);
        if (event.oldEntiy instanceof Card) {
            updateZone(game.getZoneOf((Card)event.oldEntiy));
        }
        if (event.newTarget instanceof Card) {
            updateZone(game.getZoneOf((Card)event.newTarget));
        }
        return updateZone(zEq);
    }

    private Void updateZone(Zone z) {
        return updateZone(Pair.of(z.getPlayer(), z.getZoneType()));
    }

    private Void updateZone(Pair<Player, ZoneType> kv) {
        boolean needUpdate = false;
        synchronized (zonesToUpdate) {
            needUpdate = zonesToUpdate.isEmpty();
            if (!zonesToUpdate.contains(kv)) {
                zonesToUpdate.add(kv);
            }
        }
        if (needUpdate) {
            FThreads.invokeInEdtNowOrLater(updZones);
        }
        return null;
    }

    private final Set<Card> cardsToUpdate = new HashSet<Card>();
    private final Runnable updCards = new Runnable() {
        @Override
        public void run() {
            synchronized (cardsToUpdate) {
                FControl.updateCards(cardsToUpdate);
                cardsToUpdate.clear();
            }
        }
    };

    @Override
    public Void visit(GameEventCardTapped event) {
        return updateSingleCard(event.card);
    }
    
    @Override
    public Void visit(GameEventCardPhased event) {
        return updateSingleCard(event.card);
    }

    @Override
    public Void visit(GameEventCardDamaged event) {
        return updateSingleCard(event.card);
    }

    @Override
    public Void visit(GameEventCardCounters event) {
        return updateSingleCard(event.card);
    }

    @Override
    public Void visit(GameEventBlockersDeclared event) { // This is to draw icons on blockers declared by AI
        for (MapOfLists<Card, Card> kv : event.blockers.values()) {
            for (Collection<Card> blockers : kv.values()) {
                updateManyCards(blockers);
            }
        }
        return super.visit(event);
    }

    @Override
    public Void visit(GameEventAttackersDeclared event) {
        // Skip redraw for GUI player?
        if (event.player.getLobbyPlayer() == FServer.getLobby().getGuiPlayer()) {
            return null;
        }

        // Update all attackers.
        // Although they might have been updated when they were apped, there could be someone with vigilance, not redrawn yet.
        updateManyCards(event.attackersMap.values());

        return super.visit(event);
    }

    @Override
    public Void visit(GameEventCombatEnded event) {
        // This should remove sword/shield icons from combatants by the time game moves to M2
        updateManyCards(event.attackers);
        updateManyCards(event.blockers);
        return null;
    }

    private Void updateSingleCard(Card c) {
        boolean needUpdate = false;
        synchronized (cardsToUpdate) {
            needUpdate = cardsToUpdate.isEmpty();
            if (!cardsToUpdate.contains(c)) {
                cardsToUpdate.add(c);
            }
        }
        if (needUpdate) {
            FThreads.invokeInEdtNowOrLater(updCards);
        }
        return null;
    }

    private Void updateManyCards(Collection<Card> cc) {
        boolean needUpdate = false;
        synchronized (cardsToUpdate) {
            needUpdate = cardsToUpdate.isEmpty();
            cardsToUpdate.addAll(cc);
        }
        if (needUpdate) {
            FThreads.invokeInEdtNowOrLater(updCards);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see forge.game.event.IGameEventVisitor.Base#visit(forge.game.event.GameEventCardStatsChanged)
     */
    @Override
    public Void visit(GameEventCardStatsChanged event) {
        // TODO Smart partial updates
        return updateManyCards(event.cards);
    }

    // Update manapool
    private final List<Player> manaPoolUpdate = new Vector<Player>();
    private final Runnable updManaPool = new Runnable() {
        @Override public void run() {
            synchronized (manaPoolUpdate) {
                FControl.updateManaPool(manaPoolUpdate);
                manaPoolUpdate.clear();
            }
        }
    };

    @Override
    public Void visit(GameEventManaPool event) {
        boolean invokeUpdate = false;
        synchronized (manaPoolUpdate) {
            if (!manaPoolUpdate.contains(event.player)) {
                invokeUpdate = manaPoolUpdate.isEmpty();
                manaPoolUpdate.add(event.player);
            }
        }
        if (invokeUpdate)
            FThreads.invokeInEdtNowOrLater(updManaPool);
        return null;
    }

    // Update lives counters
    private final List<Player> livesUpdate = new Vector<Player>();
    private final Runnable updLives = new Runnable() {
        @Override public void run() {
            synchronized (livesUpdate) {
                FControl.updateLives(livesUpdate);
                livesUpdate.clear();
            }
        }
    };
    @Override
    public Void visit(GameEventPlayerLivesChanged event) {
        boolean invokeUpdate = false;
        synchronized (livesUpdate) {
            if (!livesUpdate.contains(event.player)) {
                invokeUpdate = livesUpdate.isEmpty();
                livesUpdate.add(event.player);
            }
        }
        if (invokeUpdate)
            FThreads.invokeInEdtNowOrLater(updLives);
        return null;
    }

    @Override
    public Void visit(GameEventPlayerPoisoned event) {
        boolean invokeUpdate = false;
        synchronized (livesUpdate) {
            if (!livesUpdate.contains(event.receiver)) {
                invokeUpdate = livesUpdate.isEmpty();
                livesUpdate.add(event.receiver);
            }
        }
        if (invokeUpdate)
            FThreads.invokeInEdtNowOrLater(updLives);
        return null;
    }
}