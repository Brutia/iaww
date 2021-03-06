package fr.keyser.wonderfull.world.game;

import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.keyser.wonderfull.world.CurrentProduction;
import fr.keyser.wonderfull.world.Empire;
import fr.keyser.wonderfull.world.Token;
import fr.keyser.wonderfull.world.Tokens;
import fr.keyser.wonderfull.world.action.ActionAffectToProduction;
import fr.keyser.wonderfull.world.action.ActionRecycleProduction;
import fr.keyser.wonderfull.world.action.ActionSupremacy;
import fr.keyser.wonderfull.world.event.AffectProductionEvent;
import fr.keyser.wonderfull.world.event.EmpireEvent;
import fr.keyser.wonderfull.world.event.RecycleEvent;
import fr.keyser.wonderfull.world.event.RecycleInProductionEvent;
import fr.keyser.wonderfull.world.event.SupremacyEvent;

public class EmpireProductionWrapper extends EmpireWrapper {

	private final CurrentProduction production;

	/**
	 * Create a wrapper for a step
	 * 
	 * @param empire
	 * @param step
	 */
	public EmpireProductionWrapper(Empire empire, Token step) {
		this(empire, new CurrentProduction(step, empire.producedAt(step)));
	}

	public EmpireProductionWrapper(Empire empire, CurrentProduction production) {
		super(empire);
		this.production = production.sync(empire.getOnEmpire());
	}

	@JsonCreator
	public EmpireProductionWrapper(@JsonProperty("empire") Empire empire, @JsonProperty("step") Token step,
			@JsonProperty("available") Tokens available) {
		super(empire);
		this.production = new CurrentProduction(step, available);
	}

	public EmpireProductionWrapper transfertKrystaliumStep() {
		Token step = production.getStep();
		Tokens producedAt = empire.producedAt(step);
		return new EmpireProductionWrapper(empire.addTokens(producedAt), production);

	}

	/**
	 * Get the produced value for the step
	 * 
	 * @return
	 */
	@JsonIgnore
	public int getRemaining() {
		return production.getRemaining();
	}

	/**
	 * The affectation is done
	 * 
	 * @param consumer
	 * 
	 * @return
	 */
	public Empire done(Consumer<EmpireEvent> consumer) {
		int remaining = getRemaining();
		if (remaining > 0 && Token.KRYSTALIUM != production.getStep()) {
			RecycleEvent event = empire.recycle(remaining);
			consumer.accept(event);
			return empire.apply(event);
		} else
			return empire;
	}

	/**
	 * Add the supremacy token
	 * 
	 * @param action
	 * @param consumer
	 * @return
	 */
	public EmpireProductionWrapper supremacy(ActionSupremacy action, Consumer<EmpireEvent> consumer) {
		Tokens tokens = production.computeSupremacy(action.isGeneral());

		SupremacyEvent event = new SupremacyEvent(tokens);
		consumer.accept(event);

		return supremacy(event);
	}

	/**
	 * Recyle a card in the production line
	 * 
	 * @param action
	 * @param consumer
	 * @return
	 */
	public EmpireProductionWrapper recycleProduction(ActionRecycleProduction action, Consumer<EmpireEvent> consumer) {

		RecycleInProductionEvent event = empire.recycleProduction(action.getTargetId());

		consumer.accept(event);

		return recyleProduction(event);
	}

	/**
	 * Affect some currently produced resources to a card in production
	 * 
	 * @param consumer
	 * @param action
	 * @return
	 */
	public EmpireProductionWrapper affectToProduction(ActionAffectToProduction action, Consumer<EmpireEvent> consumer) {

		AffectProductionEvent event = empire.affectToProduction(action.getTargetId(), action.getSlots());
		Tokens available = production.getAvailable();
		// check affectation
		Tokens remaining = available.subtract(event.getConsumed());
		if (remaining.entrySet().stream().anyMatch(e -> e.getValue() < 0))
			throw new IllegalAffectationException();

		consumer.accept(event);
		return affectToProduction(event);
	}

	private EmpireProductionWrapper affectToProduction(AffectProductionEvent event) {
		return new EmpireProductionWrapper(empire.apply(event), production.affect(event.getConsumed()));
	}

	public Tokens getAvailable() {
		return production.getAvailable();
	}

	public Token getStep() {
		return production.getStep();
	}

	public EmpireProductionWrapper convert(Consumer<EmpireEvent> consumer) {

		int amount = production.getAvailable().get(production.getStep());

		RecycleEvent evt = empire.recycle(amount);
		consumer.accept(evt);

		return convert(evt);
	}

	private EmpireProductionWrapper supremacy(SupremacyEvent event) {
		return new EmpireProductionWrapper(empire.addTokens(event.getGain()), production);
	}

	private EmpireProductionWrapper recyleProduction(RecycleInProductionEvent event) {
		return new EmpireProductionWrapper(empire.apply(event), production);
	}

	private EmpireProductionWrapper convert(RecycleEvent evt) {
		return new EmpireProductionWrapper(empire.apply(evt), new CurrentProduction(production.getStep(),
				production.getAvailable().subtract(production.getStep().token(evt.getQuantity()))));
	}

}
