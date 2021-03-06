package forge.ai.ability;

import com.google.common.base.Predicate;
import forge.ai.ComputerUtilCard;
import forge.ai.ComputerUtilCombat;
import forge.ai.SpellAbilityAi;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates.Presets;
import forge.game.combat.Combat;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;
import forge.game.zone.ZoneType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ChooseCardAi extends SpellAbilityAi {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(final Player ai, SpellAbility sa) {
        final Card host = sa.getHostCard();
        final Game game = ai.getGame();

        final TargetRestrictions tgt = sa.getTargetRestrictions();
        if (tgt != null) {
            sa.resetTargets();
            if (sa.canTarget(ai.getOpponent())) {
                sa.getTargets().add(ai.getOpponent());
            } else {
                return false;
            }
        }
        if (sa.hasParam("AILogic")) {
            ZoneType choiceZone = ZoneType.Battlefield;
            String logic = sa.getParam("AILogic");
            if (sa.hasParam("ChoiceZone")) {
                choiceZone = ZoneType.smartValueOf(sa.getParam("ChoiceZone"));
            }
            List<Card> choices = ai.getGame().getCardsIn(choiceZone);
            if (sa.hasParam("Choices")) {
                choices = CardLists.getValidCards(choices, sa.getParam("Choices"), host.getController(), host);
            }
            if (sa.hasParam("TargetControls")) {
                choices = CardLists.filterControlledBy(choices, ai.getOpponent());
            }
            if (logic.equals("AtLeast1") || logic.equals("OppPreferred")) {
                if (choices.isEmpty()) {
                    return false;
                }
            } else if (logic.equals("AtLeast2") || logic.equals("BestBlocker")) {
                if (choices.size() < 2) {
                    return false;
                }
            } else if (logic.equals("Clone")) {
                choices = CardLists.getValidCards(choices, "Permanent.YouDontCtrl,Permanent.nonLegendary", host.getController(), host);
                if (choices.isEmpty()) {
                    return false;
                }
            } else if (logic.equals("Never")) {
                return false;
            } else if (logic.equals("NeedsPrevention")) {
                if (!game.getPhaseHandler().is(PhaseType.COMBAT_DECLARE_BLOCKERS)) {
                    return false;
                }
                final Combat combat = game.getCombat();
                choices = CardLists.filter(choices, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        if (!combat.isAttacking(c, ai) || !combat.isUnblocked(c)) {
                            return false;
                        }
                        int ref = host.getName().equals("Forcefield") ? 1 : 0;
                        return ComputerUtilCombat.damageIfUnblocked(c, ai, combat) > ref;
                    }
                });
                if (choices.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean chkAIDrawback(SpellAbility sa, Player ai) {
        return canPlayAI(ai, sa);
    }
    
    /* (non-Javadoc)
     * @see forge.card.ability.SpellAbilityAi#chooseSingleCard(forge.card.spellability.SpellAbility, java.util.List, boolean)
     */
    @Override
    public Card chooseSingleCard(final Player ai, SpellAbility sa, Collection<Card> options, boolean isOptional, Player targetedPlayer) {
        final Card host = sa.getHostCard();
        final String logic = sa.getParam("AILogic");
        Card choice = null;
        if (logic == null) {
            // Base Logic is choose "best"
            choice = ComputerUtilCard.getBestAI(options);
        } else if ("WorstCard".equals(logic)) {
            choice = ComputerUtilCard.getWorstAI(options);
        } else if (logic.equals("BestBlocker")) {
            if (!CardLists.filter(options, Presets.UNTAPPED).isEmpty()) {
                options = CardLists.filter(options, Presets.UNTAPPED);
            }
            choice = ComputerUtilCard.getBestCreatureAI(options);
        } else if (logic.equals("Clone")) {
            if (!CardLists.getValidCards(options, "Permanent.YouDontCtrl,Permanent.nonLegendary", host.getController(), host).isEmpty()) {
                options = CardLists.getValidCards(options, "Permanent.YouDontCtrl,Permanent.nonLegendary", host.getController(), host);
            }
            choice = ComputerUtilCard.getBestAI(options);
        } else if (logic.equals("Untap")) {
            if (!CardLists.getValidCards(options, "Permanent.YouCtrl,Permanent.tapped", host.getController(), host).isEmpty()) {
                options = CardLists.getValidCards(options, "Permanent.YouCtrl,Permanent.tapped", host.getController(), host);
            }
            choice = ComputerUtilCard.getBestAI(options);
        } else if (logic.equals("NeedsPrevention")) {
            final Game game = ai.getGame();
            final Combat combat = game.getCombat();
            List<Card> better =  CardLists.filter(options, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    if (combat == null || !combat.isAttacking(c, ai) || !combat.isUnblocked(c)) {
                        return false;
                    }
                    int ref = host.getName().equals("Forcefield") ? 1 : 0; 
                    return ComputerUtilCombat.damageIfUnblocked(c, ai, combat) > ref;
                }
            });
            if (!better.isEmpty()) {
                choice = ComputerUtilCard.getBestAI(better);
            } else {
                choice = ComputerUtilCard.getBestAI(options);
            }
        } else if ("OppPreferred".equals(logic)) {
            List<Card> oppControlled = CardLists.filterControlledBy(options, ai.getOpponents());
            if (!oppControlled.isEmpty()) {
                choice = ComputerUtilCard.getBestAI(oppControlled);
            } else {
                List<Card> aiControlled = CardLists.filterControlledBy(options, ai);
                choice = ComputerUtilCard.getWorstAI(aiControlled);
            }
        } else if ("LowestCMCCreature".equals(logic)) {
            List<Card> creats = CardLists.filter(options, Presets.CREATURES);
            creats = CardLists.filterToughness(creats, 1);
            if (creats.isEmpty()) {
                choice = ComputerUtilCard.getWorstAI(options);
            } else {
                CardLists.sortByCmcDesc(creats);
                Collections.reverse(creats);
                choice = creats.get(0);
            }
        } else if ("TangleWire".equals(logic)) {
            List<Card> betterList = CardLists.filter(options, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    if (c.isCreature()) {
                        return false;
                    }
                    for (SpellAbility sa : c.getAllSpellAbilities()) {
                        if (sa.getPayCosts() != null && sa.getPayCosts().hasTapCost()) {
                            return false;
                        }
                    }
                    return true;
                }
            });
            System.out.println("Tangle Wire" + options + " - " + betterList);
            if (!betterList.isEmpty()) {
                choice = betterList.get(0);
            } else {
                choice = ComputerUtilCard.getWorstPermanentAI(options, false, false, false, false);
            }
        } else {
            choice = ComputerUtilCard.getBestAI(options);
        }
        return choice;
    }
}
