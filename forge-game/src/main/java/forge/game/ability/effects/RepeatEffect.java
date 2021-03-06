package forge.game.ability.effects;

import forge.game.Game;
import forge.game.ability.AbilityFactory;
import forge.game.ability.AbilityUtils;
import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.card.CardFactoryUtil;
import forge.game.card.CardLists;
import forge.game.player.Player;
import forge.game.spellability.AbilitySub;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import forge.util.Expressions;

import java.util.ArrayList;
import java.util.List;

public class RepeatEffect extends SpellAbilityEffect {

    @Override
    protected String getStackDescription(SpellAbility sa) {
        return "Repeat something. Somebody should really write a better StackDescription!";
    } // end repeatStackDescription()

    @Override
    public void resolve(SpellAbility sa) {
        Card source = sa.getHostCard();

        // setup subability to repeat
        final SpellAbility repeat = AbilityFactory.getAbility(sa.getHostCard().getSVar(sa.getParam("RepeatSubAbility")), source);
        repeat.setActivatingPlayer(sa.getActivatingPlayer());
        ((AbilitySub) repeat).setParent(sa);

        Integer maxRepeat = null;
        if (sa.hasParam("MaxRepeat")) {
            maxRepeat = AbilityUtils.calculateAmount(sa.getHostCard(), sa.getParam("MaxRepeat"), sa);
            if (maxRepeat.intValue() == 0) return; // do nothing if maxRepeat is 0. the next loop will execute at least once
        }

        //execute repeat ability at least once
        int count = 0;
        do {
            AbilityUtils.resolve(repeat);
            count++;
            if (maxRepeat != null && maxRepeat <= count) {
                // TODO Replace Infinite Loop Break with a game draw. Here are the scenarios that can cause this:
                // Helm of Obedience vs Graveyard to Library replacement effect
                StringBuilder infLoop = new StringBuilder(sa.getHostCard().toString());
                infLoop.append(" - To avoid an infinite loop, this repeat has been broken ");
                infLoop.append(" and the game will now continue in the current state, ending the loop early. ");
                infLoop.append("Once Draws are available this probably should change to a Draw.");
                System.out.println(infLoop.toString());
                break;
            }
        } while (checkRepeatConditions(sa));
    }
// end class AbilityFactory_Repeat

    /**
     * <p>
     * checkRepeatConditions.
     * </p>
     * 
     * @param AF
     *            a {@link forge.game.ability.AbilityFactory} object.
     * @param SA
     *            a {@link forge.game.spellability.SpellAbility} object.
     */
    private boolean checkRepeatConditions(final SpellAbility sa) {
        //boolean doAgain = false;
        final Player activator = sa.getActivatingPlayer();
        final Game game = activator.getGame();


        if (sa.hasParam("RepeatPresent")) {
            final String repeatPresent = sa.getParam("RepeatPresent");
            List<Card> list = new ArrayList<Card>();

            String repeatCompare = "GE1";
            if (sa.hasParam("RepeatCompare")) {
                repeatCompare = sa.getParam("RepeatCompare");
            }

            if (sa.hasParam("RepeatDefined")) {
                list.addAll(AbilityUtils.getDefinedCards(sa.getHostCard(), sa.getParam("RepeatDefined"), sa));
            } else {
                list = game.getCardsIn(ZoneType.Battlefield);
            }

            list = CardLists.getValidCards(list, repeatPresent.split(","), sa.getActivatingPlayer(), sa.getHostCard());

            int right;
            final String rightString = repeatCompare.substring(2);
            try { // If this is an Integer, just parse it
                right = Integer.parseInt(rightString);
            } catch (final NumberFormatException e) { // Otherwise, grab it from
                                                      // the
                // SVar
                right = CardFactoryUtil.xCount(sa.getHostCard(), sa.getHostCard().getSVar(rightString));
            }

            final int left = list.size();

            if (!Expressions.compare(left, repeatCompare, right)) {
                return false;
            }
        }

        if (sa.hasParam("RepeatCheckSVar")) {
            String sVarOperator = "GE";
            String sVarOperand = "1";
            if (sa.hasParam("RepeatSVarCompare")) {
                sVarOperator = sa.getParam("RepeatSVarCompare").substring(0, 2);
                sVarOperand = sa.getParam("RepeatSVarCompare").substring(2);
            }
            final int svarValue = AbilityUtils.calculateAmount(sa.getHostCard(), sa.getParam("RepeatCheckSVar"), sa);
            final int operandValue = AbilityUtils.calculateAmount(sa.getHostCard(), sVarOperand, sa);

            if (!Expressions.compare(svarValue, sVarOperator, operandValue)) {
                return false;
            }
        }

        if (sa.hasParam("RepeatOptional")) {
            return sa.getActivatingPlayer().getController().confirmAction(sa, null, "Do you want to repeat this process again?");
        }

        return true;
    }
}
