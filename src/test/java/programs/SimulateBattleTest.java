package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.Program;
import junit.framework.TestCase;

import java.util.Collections;

public class SimulateBattleTest extends TestCase {

    private SimulateBattleImpl simulator;
    private MockPrintBattleLog log;

    protected void setUp() {
        log = new MockPrintBattleLog();
        simulator = new SimulateBattleImpl(log);
    }

    public void testSimulate() throws InterruptedException {
        Unit playerUnit = new Unit("Player", "T1", 10, 5, 5, "P", null, null, 0, 0);
        Unit computerUnit = new Unit("Computer", "T1", 10, 5, 5, "P", null, null, 0, 1);
        
        playerUnit.setProgram(new MockProgram(computerUnit));
        computerUnit.setProgram(new MockProgram(playerUnit));

        Army playerArmy = new Army();
        playerArmy.setUnits(Collections.singletonList(playerUnit));
        
        Army computerArmy = new Army();
        computerArmy.setUnits(Collections.singletonList(computerUnit));

        simulator.simulate(playerArmy, computerArmy);
        
        // Хотя бы один должен умереть
        assertTrue(!playerUnit.isAlive() || !computerUnit.isAlive());
        assertTrue(log.called);
    }

    private static class MockPrintBattleLog implements PrintBattleLog {
        boolean called = false;
        @Override
        public void printBattleLog(Unit attacker, Unit target) {
            called = true;
        }
    }

    private static class MockProgram extends Program {
        private final Unit target;

        MockProgram(Unit target) {
            super(null, null, null, null);
            this.target = target;
        }

        @Override
        public Unit attack() {
            if (target != null && target.isAlive()) {
                target.setHealth(target.getHealth() - 10);
                if (target.getHealth() <= 0) {
                    target.setAlive(false);
                }
            }
            return target;
        }
    }
}
