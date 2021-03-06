package fr.keyser.wonderfull.world.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.keyser.fsm.InstanceId;
import fr.keyser.fsm.impl.Automats;
import fr.keyser.fsm.impl.Start;
import fr.keyser.wonderfull.world.GameConfiguration;
import fr.keyser.wonderfull.world.MetaCardDictionnaryLoader;

public class GameBootstraper {

	private static final Logger logger = LoggerFactory.getLogger(GameBootstraper.class);

	private final GameAutomatsBuilder builder;

	private final MetaCardDictionnaryLoader loader;

	private final ActiveGameRepository repository;

	public GameBootstraper(GameAutomatsBuilder builder, MetaCardDictionnaryLoader loader,
			ActiveGameRepository repository) {
		this.builder = builder;
		this.loader = loader;
		this.repository = repository;
	}

	public ActiveGame starts(GameConfiguration conf) {
		boolean wop = Extension.containsWarOrPeace(conf.getDictionaries());

		Automats<GameInfo> automats = builder.build(InstanceId.uuid(), conf.getPlayerCount(), wop);
		logger.trace("Automats {}", automats);

		logger.info("Starting game {} with : {}", automats.getId(), conf);
		automats.submit(Start.start(new GameInfo(Game.bootstrap(loader, conf))));
		ActiveGame activeGame = new ActiveGame(automats);
		repository.register(activeGame);
		return activeGame;
	}

	public void stop(InstanceId gameId, String user) {

		ActiveGame game = repository.findById(gameId);
		if (game != null && game.configuration().getCreator().equals(user)) {
			logger.info("Stoping game {} for {}", gameId, game.getPlayers());
			game.kill();

			repository.unregister(game);
		}
	}
}
