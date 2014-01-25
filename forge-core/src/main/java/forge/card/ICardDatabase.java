package forge.card;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.common.base.Predicate;

import forge.card.CardDb.SetPreference;
import forge.item.PaperCard;

public interface ICardDatabase extends Iterable<PaperCard> {
    PaperCard getCard(String cardName);
    PaperCard getCard(String cardName, String edition);
    PaperCard getCard(String cardName, String edition, int artIndex);
    PaperCard getCardFromEdition(final String cardName, final SetPreference fromSet);
    PaperCard getCardFromEdition(final String cardName, final Date printedBefore, final SetPreference fromSet);
    
    PaperCard getFoiled(PaperCard cpi);

    int getPrintCount(String cardName, String edition);
    int getMaxPrintCount(String cardName);

    int getArtCount(String cardName, String edition);

    Collection<PaperCard> getUniqueCards();
    List<PaperCard> getAllCards();
    List<PaperCard> getAllCards(Predicate<PaperCard> predicate);

    Predicate<? super PaperCard> wasPrintedInSets(List<String> allowedSetCodes);
}