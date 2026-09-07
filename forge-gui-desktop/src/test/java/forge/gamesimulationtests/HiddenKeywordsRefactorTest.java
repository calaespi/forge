package forge.gamesimulationtests;

import forge.ai.simulation.SimulationTest;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.staticability.StaticAbilityCantGainControl;
import forge.game.zone.ZoneType;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HiddenKeywordsRefactorTest extends SimulationTest {

    @Test
    public void testGuardianBeastPreventsControlGain() {
        Game game = initAndCreateGame();
        Player p1 = game.getPlayers().get(0);
        Player p2 = game.getPlayers().get(1);

        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p1);

        Card guardian = createCard("Guardian Beast", p1);
        guardian.setGameTimestamp(game.getNextTimestamp());
        p1.getZone(ZoneType.Battlefield).add(guardian);
        guardian.setTapped(false);

        Card artifact = createCard("Sol Ring", p1);
        artifact.setGameTimestamp(game.getNextTimestamp());
        p1.getZone(ZoneType.Battlefield).add(artifact);

        game.getAction().checkStaticAbilities();

        Assert.assertTrue(StaticAbilityCantGainControl.cantGainControl(artifact),
                "Guardian Beast should prevent control gain on noncreature artifacts");
        Assert.assertFalse(artifact.canBeControlledBy(p2),
                "Opponent should not be able to gain control of protected artifact");
        Assert.assertTrue(artifact.canBeControlledBy(p1),
                "Controller should still be able to control their own artifact");
    }

    @Test
    public void testGuardianBeastDoesNotApplyWhileTapped() {
        Game game = initAndCreateGame();
        Player p1 = game.getPlayers().get(0);
        Player p2 = game.getPlayers().get(1);

        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p1);

        Card guardian = createCard("Guardian Beast", p1);
        guardian.setGameTimestamp(game.getNextTimestamp());
        p1.getZone(ZoneType.Battlefield).add(guardian);
        guardian.setTapped(true);

        Card artifact = createCard("Sol Ring", p1);
        artifact.setGameTimestamp(game.getNextTimestamp());
        p1.getZone(ZoneType.Battlefield).add(artifact);

        game.getAction().checkStaticAbilities();

        Assert.assertFalse(StaticAbilityCantGainControl.cantGainControl(artifact),
                "Tapped Guardian Beast should not prevent control gain");
        Assert.assertTrue(artifact.canBeControlledBy(p2),
                "Opponent should be able to gain control while Guardian Beast is tapped");
    }

    @Test
    public void testZilorthaLethalDamageByPower() {
        Game game = initAndCreateGame();
        Player p1 = game.getPlayers().get(0);

        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p1);

        Card zilortha = createCard("Zilortha, Strength Incarnate", p1);
        zilortha.setGameTimestamp(game.getNextTimestamp());
        p1.getZone(ZoneType.Battlefield).add(zilortha);

        Card bear = createCard("Grizzly Bears", p1);
        bear.setGameTimestamp(game.getNextTimestamp());
        p1.getZone(ZoneType.Battlefield).add(bear);

        game.getAction().checkStaticAbilities();

        Assert.assertTrue(bear.isLethalDamageByPower(),
                "Creatures you control should use power for lethal damage under Zilortha");
        Assert.assertEquals(bear.getLethal(), bear.getNetPower(),
                "getLethal should return power while LethalDamageByPower is active");
        Assert.assertEquals(zilortha.getLethal(), zilortha.getNetPower(),
                "Zilortha itself should also use power for lethal damage");
    }
}
