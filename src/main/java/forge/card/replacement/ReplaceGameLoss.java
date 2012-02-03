package forge.card.replacement;

import java.util.HashMap;

import forge.Card;
import forge.card.spellability.SpellAbility;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class ReplaceGameLoss extends ReplacementEffect {

    /**
     * Instantiates a new replace gain life.
     *
     * @param map the map
     * @param host the host
     */
    public ReplaceGameLoss(HashMap<String, String> map, Card host) {
        super(map, host);
    }

    /* (non-Javadoc)
     * @see forge.card.replacement.ReplacementEffect#canReplace(java.util.HashMap)
     */
    @Override
    public boolean canReplace(HashMap<String, Object> runParams) {
        if (!runParams.get("Event").equals("GameLoss")) {
            return false;
        }
        if (this.getMapParams().containsKey("ValidPlayer")) {
            if (!matchesValid(runParams.get("Affected"), this.getMapParams().get("ValidPlayer").split(","), this.getHostCard())) {
                return false;
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see forge.card.replacement.ReplacementEffect#getCopy()
     */
    @Override
    public ReplacementEffect getCopy() {
        return new ReplaceGameLoss(this.getMapParams(), getHostCard());
    }

    /* (non-Javadoc)
     * @see forge.card.replacement.ReplacementEffect#setReplacingObjects(java.util.HashMap, forge.card.spellability.SpellAbility)
     */
    @Override
    public void setReplacingObjects(HashMap<String, Object> runParams, SpellAbility sa) {
    }

}
