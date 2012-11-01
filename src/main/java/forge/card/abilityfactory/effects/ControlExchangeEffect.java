package forge.card.abilityfactory.effects;

import java.util.ArrayList;
import java.util.Map;

import forge.Card;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.abilityfactory.SpellEffect;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.player.Player;

/**
     * <p>
     * exchangeControlStackDescription.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    

public class ControlExchangeEffect extends SpellEffect {
    
    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#getStackDescription(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected String getStackDescription(Map<String, String> params, SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();
        Card object1 = null;
        Card object2 = null;
        final Target tgt = sa.getTarget();
        ArrayList<Card> tgts = tgt.getTargetCards();
        if (tgts.size() > 0) {
            object1 = tgts.get(0);
        }
        if (params.containsKey("Defined")) {
            object2 = AbilityFactory.getDefinedCards(sa.getSourceCard(), params.get("Defined"), sa).get(0);
        } else if (tgts.size() > 1) {
            object2 = tgts.get(1);
        }
    
        if (sa instanceof AbilitySub) {
            sb.append(" ");
        } else {
            sb.append(sa.getSourceCard()).append(" - ");
        }
    
        sb.append(object1 + " exchanges controller with " + object2);
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#resolve(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(Map<String, String> params, SpellAbility sa) {
        Card object1 = null;
        Card object2 = null;
        final Target tgt = sa.getTarget();
        ArrayList<Card> tgts = tgt.getTargetCards();
        if (tgts.size() > 0) {
            object1 = tgts.get(0);
        }
        if (params.containsKey("Defined")) {
            object2 = AbilityFactory.getDefinedCards(sa.getSourceCard(), params.get("Defined"), sa).get(0);
        } else if (tgts.size() > 1) {
            object2 = tgts.get(1);
        }

        if (object1 == null || object2 == null || !object1.isInPlay()
                || !object2.isInPlay()) {
            return;
        }

        Player player2 = object2.getController();
        object2.addController(object1.getController());
        object1.addController(player2);
    }

} // end class AbilityFactory_GainControl