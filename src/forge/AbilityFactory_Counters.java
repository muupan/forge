package forge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class AbilityFactory_Counters {
	// An AbilityFactory subclass for Putting or Removing Counters on Cards.
	
	public static SpellAbility createAbilityPutCounters(final AbilityFactory AF){

		final SpellAbility abPutCounter = new Ability_Activated(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()){
			private static final long serialVersionUID = -1259638699008542484L;
			
			final AbilityFactory af = AF;
			final HashMap<String,String> params = af.getMapParams();

			final String type = params.get("CounterType");
		
			@Override
			public String getStackDescription(){
			// when getStackDesc is called, just build exactly what is happening
				 Counters cType = Counters.valueOf(type);
				 StringBuilder sb = new StringBuilder();
				 String name = af.getHostCard().getName();
				 int amount = calculateAmount(af.getHostCard(), params.get("CounterNum"), this);
				 sb.append(name).append(" - Put ").append(amount).append(" ").append(cType.getName()).append(" counter on ");
				 Card tgt = getTargetCard();
				 if (tgt != null)
					 sb.append(tgt.getName());
				 else
					 sb.append(name);
				 return sb.toString();
			}
			
			public boolean canPlay(){
				// super takes care of AdditionalCosts
				return super.canPlay();	
			}
			
			public boolean canPlayAI()
			{
				return putCanPlayAI(af, this, params.get("CounterNum"), type);
			}
			
			@Override
			public void resolve() {
				int amount = calculateAmount(af.getHostCard(), params.get("CounterNum"), this);
				putResolve(af, this, amount, type);
			}
			
		};
		return abPutCounter;
	}
	
	public static SpellAbility createAbilityRemoveCounters(final AbilityFactory AF){

		final SpellAbility abRemCounter = new Ability_Activated(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()){
			private static final long serialVersionUID = 8581011868395954121L;
			
			final AbilityFactory af = AF;
			final HashMap<String,String> params = af.getMapParams();

			final String type = params.get("CounterType");
		
			@Override
			public String getStackDescription(){
			// when getStackDesc is called, just build exactly what is happening
				 Counters cType = Counters.valueOf(type);
				 StringBuilder sb = new StringBuilder();
				 String name = af.getHostCard().getName();
				 int amount = calculateAmount(af.getHostCard(), params.get("CounterNum"), this);
				 sb.append(name).append(" - Remove ").append(amount).append(" ").append(cType.getName()).append(" counter from ");
				 Card tgt = getTargetCard();
				 if (tgt != null)
					 sb.append(tgt.getName());
				 else
					 sb.append(name);
				 return sb.toString();
			}
			
			public boolean canPlay(){
				// super takes care of AdditionalCosts
				return super.canPlay();	
			}
			
			public boolean canPlayAI()
			{
				return removeCanPlayAI(af, this, params.get("CounterNum"), type);
			}
			
			@Override
			public void resolve() {
				int amount = calculateAmount(af.getHostCard(), params.get("CounterNum"), this);
				removeResolve(af, this, amount, type);
			}
			
		};
		return abRemCounter;
	}
	
	public static SpellAbility createSpellPutCounters(final AbilityFactory AF){
		final SpellAbility spPutCounter = new Spell(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()){
			private static final long serialVersionUID = -323471693082498224L;
			
			final AbilityFactory af = AF;
			final HashMap<String,String> params = af.getMapParams();
			
			final String type = params.get("CounterType");
		
			@Override
			public String getStackDescription(){
			// when getStackDesc is called, just build exactly what is happening
				 Counters cType = Counters.valueOf(type);
				 StringBuilder sb = new StringBuilder();
				 String name = af.getHostCard().getName();
				 int amount = calculateAmount(af.getHostCard(), params.get("CounterNum"), this);
				 sb.append(name).append(" - Put ").append(amount).append(" ").append(cType.getName()).append(" counter on ");
				 Card tgt = getTargetCard();
				 if (tgt != null)
					 sb.append(tgt.getName());
				 else
					 sb.append(name);
				 return sb.toString();
			}
			
			public boolean canPlay(){
				// super takes care of AdditionalCosts
				return super.canPlay();	
			}
			
			public boolean canPlayAI()
			{
				// if X depends on abCost, the AI needs to choose which card he would sacrifice first
				// then call xCount with that card to properly calculate the amount
				// Or choosing how many to sacrifice 
				return putCanPlayAI(af, this, params.get("CounterNum"), type);
			}
			
			@Override
			public void resolve() {
				int amount = calculateAmount(af.getHostCard(), params.get("CounterNum"), this);
				putResolve(af, this, amount, type);
			}
			
		};
		return spPutCounter;
	}
	
	public static SpellAbility createSpellRemoveCounters(final AbilityFactory AF){
		final SpellAbility spPutCounter = new Spell(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()){
			private static final long serialVersionUID = -5065591869141835456L;
			
			final AbilityFactory af = AF;
			final HashMap<String,String> params = af.getMapParams();
			
			final String type = params.get("CounterType");
		
			@Override
			public String getStackDescription(){
				// when getStackDesc is called, just build exactly what is happening
				 Counters cType = Counters.valueOf(type);
				 StringBuilder sb = new StringBuilder();
				 String name = af.getHostCard().getName();
				 int amount = calculateAmount(af.getHostCard(), params.get("CounterNum"), this);
				 sb.append(name).append(" - Remove ").append(amount).append(" ").append(cType.getName()).append(" counter from ");
				 Card tgt = getTargetCard();
				 if (tgt != null)
					 sb.append(tgt.getName());
				 else
					 sb.append(name);
				 return sb.toString();
			}
			
			public boolean canPlay(){
				// super takes care of AdditionalCosts
				return super.canPlay();	
			}
			
			public boolean canPlayAI()
			{
				// if X depends on abCost, the AI needs to choose which card he would sacrifice first
				// then call xCount with that card to properly calculate the amount
				// Or choosing how many to sacrifice 
				return putCanPlayAI(af, this, params.get("CounterNum"), type);
			}
			
			@Override
			public void resolve() {
				int amount = calculateAmount(af.getHostCard(), params.get("CounterNum"), this);
				removeResolve(af, this, amount, type);
			}
			
		};
		return spPutCounter;
	}
	
	public static int calculateAmount(Card card, String counterNum, SpellAbility ability){
		if (counterNum.matches("X"))
		{
			String calcX[] = card.getSVar(counterNum).split("\\$");
			if (calcX.length == 1 || calcX[1].equals("none"))
				return 0;
			
			if (calcX[0].startsWith("Count"))
			{
				return CardFactoryUtil.xCount(card, calcX[1]);
			}
			else if (calcX[0].startsWith("Sacrificed"))
			{
				return CardFactoryUtil.handlePaid(ability.getSacrificedCost(), calcX[1]);
			}
			else
				return 0;
		}

		return Integer.parseInt(counterNum);
	}
	
	public static boolean putCanPlayAI(final AbilityFactory af, final SpellAbility sa, final String amountStr, final String type){
		// AI needs to be expanded, since this function can be pretty complex based on what the expected targets could be
		Random r = new Random();
		Ability_Cost abCost = sa.getPayCosts();
		Target abTgt = sa.getTarget();
		final Card source = sa.getSourceCard();
		CardList list;
		Card choice = null;
		
		Player player = af.isCurse() ? AllZone.HumanPlayer : AllZone.ComputerPlayer;
		
		list = new CardList(AllZone.getZone(Constant.Zone.Play, player).getCards());
		list = list.filter(new CardListFilter() {
			public boolean addCard(Card c) {
				return CardFactoryUtil.canTarget(source, c);
			}
		});
		
		if (abTgt != null){
			list = list.getValidCards(abTgt.getValidTgts());

			if (list.size() == 0)
				return false;
		}
		
		if (abCost != null){
			// AI currently disabled for these costs
			if (abCost.getSacCost()){ 
				return false;
			}
			if (abCost.getLifeCost())	 return false;
			if (abCost.getDiscardCost()) return false;
			
			if (abCost.getSubCounter()){
				// A card has a 25% chance per counter to be able to pass through here
				// 8+ counters will always pass. 0 counters will never
				int currentNum = source.getCounters(abCost.getCounterType());
				double percent = .25 * (currentNum / abCost.getCounterNum());
				if (percent <= r.nextFloat())
					return false;
			}
		}
		
		if (!ComputerUtil.canPayCost(sa))
			return false;
		
		// TODO handle proper calculation of X values based on Cost
		final int amount = calculateAmount(af.getHostCard(), amountStr, sa);
		
		 // prevent run-away activations - first time will always return true
		 boolean chance = r.nextFloat() <= Math.pow(.6667, source.getAbilityUsed());
		 
		 // Targeting
		 if (abTgt != null){
			 abTgt.resetTargets();
			 // target loop
			while(abTgt.getNumTargeted() < abTgt.getMaxTargets()){ 
				if (list.size() == 0){
					if (abTgt.getNumTargeted() < abTgt.getMinTargets() || abTgt.getNumTargeted() == 0){
						abTgt.resetTargets();
						return false;
					}
					else{
						// todo is this good enough? for up to amounts?
						break;
					}
				}
				
				 if (af.isCurse()){
					 if (type.equals("M1M1")){
						 // try to kill the best killable creature, or reduce the best one 
						 CardList killable = list.filter(new CardListFilter() {
							public boolean addCard(Card c) {
								return c.getNetDefense() <= amount;
							}
						 });
						 if (killable.size() > 0)
							 choice = CardFactoryUtil.AI_getBestCreature(killable);
						 else
							 choice = CardFactoryUtil.AI_getBestCreature(list);
					 }
					 else{
						 // improve random choice here
						 list.shuffle();
						 choice = list.get(0);
					 }
				 }
				 else{
					 if (type.equals("P1P1")){
						 choice = CardFactoryUtil.AI_getBestCreature(list);
					 }
					 else{
						 // The AI really should put counters on cards that can use it. 
						 // Charge counters on things with Charge abilities, etc. Expand these above
						 list.shuffle();
						 choice = list.get(0);
					 } 
				 }
				 
				if (choice == null){	// can't find anything left
					if (abTgt.getNumTargeted() < abTgt.getMinTargets() || abTgt.getNumTargeted() == 0){
						abTgt.resetTargets();
						return false;
					}
					else{
						// todo is this good enough? for up to amounts?
						break;
					}
				}
				list.remove(choice);
				abTgt.addTarget(choice);
			}
		 }
		 else{
			// Placeholder: No targeting necessary
			 int currCounters = sa.getSourceCard().getCounters(Counters.valueOf(type));
			// each counter on the card is a 10% chance of not activating this ability. 
			 if (r.nextFloat() < .1 * currCounters)	
				 return false;
		 }
		 
		 return ((r.nextFloat() < .6667) && chance);
	}
	
	public static boolean removeCanPlayAI(final AbilityFactory af, final SpellAbility sa, final String amountStr, final String type){
		// AI needs to be expanded, since this function can be pretty complex based on what the expected targets could be
		Random r = new Random();
		Ability_Cost abCost = sa.getPayCosts();
		//Target abTgt = sa.getTarget();
		final Card source = sa.getSourceCard();
		//CardList list;
		//Card choice = null;
		
		//TODO - currently, not targeted, only for Self
		
		//Player player = af.isCurse() ? AllZone.HumanPlayer : AllZone.ComputerPlayer;
		
		
		if (abCost != null){
			// AI currently disabled for these costs
			if (abCost.getSacCost()){ 
				return false;
			}
			if (abCost.getLifeCost())	 return false;
			if (abCost.getDiscardCost()) return false;
			
			if (abCost.getSubCounter()){
				// A card has a 25% chance per counter to be able to pass through here
				// 8+ counters will always pass. 0 counters will never
				int currentNum = source.getCounters(abCost.getCounterType());
				double percent = .25 * (currentNum / abCost.getCounterNum());
				if (percent <= r.nextFloat())
					return false;
			}
		}
		
		if (!ComputerUtil.canPayCost(sa))
			return false;
		
		// TODO handle proper calculation of X values based on Cost
		//final int amount = calculateAmount(af.getHostCard(), amountStr, sa);
		
		 // prevent run-away activations - first time will always return true
		 boolean chance = r.nextFloat() <= Math.pow(.6667, source.getAbilityUsed());
		 
		 //currently, not targeted

		 // Placeholder: No targeting necessary
		 int currCounters = sa.getSourceCard().getCounters(Counters.valueOf(type));
		 // each counter on the card is a 10% chance of not activating this ability. 
		 if (r.nextFloat() < .1 * currCounters)	
			 return false;


		 return ((r.nextFloat() < .6667) && chance);
	}
	
	public static void putResolve(final AbilityFactory af, final SpellAbility sa, int counterAmount, final String type){
		HashMap<String,String> params = af.getMapParams();
		String DrawBack = params.get("SubAbility");
		Card card = af.getHostCard();
		
		ArrayList<Card> tgtCards;

		Target tgt = af.getAbTgt();
		if (tgt != null)
			tgtCards = tgt.getTargetCards();
		else{
			tgtCards = new ArrayList<Card>();
			tgtCards.add(card);
		}
		
		for(Card tgtCard : tgtCards)
			if(AllZone.GameAction.isCardInPlay(tgtCard) && (tgt == null || CardFactoryUtil.canTarget(card, tgtCard)))
				tgtCard.addCounter(Counters.valueOf(type), counterAmount);
		
		if (af.hasSubAbility())
			 CardFactoryUtil.doDrawBack(DrawBack, counterAmount, card.getController(), card.getController().getOpponent(), card.getController(), card, null, sa);

	}
	
	public static void removeResolve(final AbilityFactory af, final SpellAbility sa, int counterAmount, final String type){
		HashMap<String,String> params = af.getMapParams();
		String DrawBack = params.get("SubAbility");
		Card card = af.getHostCard();
		
		ArrayList<Card> tgtCards;

		Target tgt = af.getAbTgt();
		if (tgt != null)
			tgtCards = tgt.getTargetCards();
		else{
			tgtCards = new ArrayList<Card>();
			tgtCards.add(card);
		}
		
		for(Card tgtCard : tgtCards)
			if(AllZone.GameAction.isCardInPlay(tgtCard) && (tgt == null || CardFactoryUtil.canTarget(card, tgtCard)))
				tgtCard.subtractCounter(Counters.valueOf(type), counterAmount);
		
		if (af.hasSubAbility())
			 CardFactoryUtil.doDrawBack(DrawBack, counterAmount, card.getController(), card.getController().getOpponent(), card.getController(), card, null, sa);

	}
	
	
	// move proliferate here? AB$Proliferate|NumProliferate$2
}
