package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.*;

/**
 * Симуляция боя по раундам согласно ТЗ:
 * - в каждом раунде юниты сортируются по убыванию baseAttack
 * - ходы чередуются между армиями
 * - если у армии закончились юниты на ход, она "ждёт"
 * - юнит, погибший до своего хода, удаляется из очереди немедленно
 * - после каждой атаки печатается лог
 */
public final class SimulateBattleImpl implements SimulateBattle {

    private final PrintBattleLog printBattleLog;

    public SimulateBattleImpl(PrintBattleLog printBattleLog) {
        this.printBattleLog = Objects.requireNonNull(printBattleLog);
    }

    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        if (playerArmy == null || computerArmy == null) return;

        while (hasAlive(playerArmy) && hasAlive(computerArmy)) {
            TurnQueues q = buildQueues(playerArmy, computerArmy);

            boolean playerTurn = shouldPlayerStart(q.player, q.computer);

            // раунд продолжается, пока кто-то ещё может ходить в этом раунде
            while (!q.player.isEmpty() || !q.computer.isEmpty()) {

                if (playerTurn) {
                    takeTurn(q.player, q.playerIndex, q.computer, q.computerIndex);
                } else {
                    takeTurn(q.computer, q.computerIndex, q.player, q.playerIndex);
                }
                playerTurn = !playerTurn;

                // бой заканчивается, когда у одной армии не осталось живых
                if (!hasAlive(playerArmy) || !hasAlive(computerArmy)) {
                    return;
                }
            }
            // раунд завершён -> следующий раунд, очереди пересоберутся с учётом потерь
        }
    }

    private void takeTurn(NavigableSet<UnitRef> attackers,
                          Map<Unit, UnitRef> attackersIndex,
                          NavigableSet<UnitRef> defenders,
                          Map<Unit, UnitRef> defendersIndex) throws InterruptedException {

        if (attackers.isEmpty()) return; // армия ждёт

        UnitRef attackerRef = attackers.pollFirst(); // сильнейший из оставшихся
        Unit attacker = attackerRef.unit;

        // если умер до своего хода или нет программы — пропускаем
        if (attacker == null || !attacker.isAlive() || attacker.getProgram() == null) {
            return;
        }

        Unit target = attacker.getProgram().attack(); // может бросить InterruptedException
        printBattleLog.printBattleLog(attacker, target);

        if (target == null) return;

        // если цель умерла и ещё не походила — убрать из очереди защитников
        if (!target.isAlive()) {
            UnitRef targetRef = defendersIndex.get(target);
            if (targetRef != null) {
                defenders.remove(targetRef);
            }
        }
    }

    private boolean hasAlive(Army army) {
        List<Unit> units = army.getUnits();
        if (units == null) return false;
        for (Unit u : units) {
            if (u != null && u.isAlive()) return true;
        }
        return false;
    }

    private boolean shouldPlayerStart(NavigableSet<UnitRef> player, NavigableSet<UnitRef> computer) {
        if (player.isEmpty()) return false;
        if (computer.isEmpty()) return true;
        return player.first().attack >= computer.first().attack;
    }

    private TurnQueues buildQueues(Army playerArmy, Army computerArmy) {
        NavigableSet<UnitRef> playerQ = new TreeSet<>(UnitRef.ORDER);
        NavigableSet<UnitRef> computerQ = new TreeSet<>(UnitRef.ORDER);

        Map<Unit, UnitRef> playerIdx = new IdentityHashMap<>();
        Map<Unit, UnitRef> computerIdx = new IdentityHashMap<>();

        addAliveUnits(playerArmy, playerQ, playerIdx);
        addAliveUnits(computerArmy, computerQ, computerIdx);

        return new TurnQueues(playerQ, computerQ, playerIdx, computerIdx);
    }

    private void addAliveUnits(Army army, NavigableSet<UnitRef> queue, Map<Unit, UnitRef> index) {
        List<Unit> units = army.getUnits();
        if (units == null) return;

        int seq = 0;
        for (Unit u : units) {
            if (u == null || !u.isAlive()) continue;
            UnitRef ref = new UnitRef(u, u.getBaseAttack(), seq++);
            queue.add(ref);
            index.put(u, ref);
        }
    }

    private static final class TurnQueues {
        final NavigableSet<UnitRef> player;
        final NavigableSet<UnitRef> computer;
        final Map<Unit, UnitRef> playerIndex;
        final Map<Unit, UnitRef> computerIndex;

        TurnQueues(NavigableSet<UnitRef> player,
                   NavigableSet<UnitRef> computer,
                   Map<Unit, UnitRef> playerIndex,
                   Map<Unit, UnitRef> computerIndex) {
            this.player = player;
            this.computer = computer;
            this.playerIndex = playerIndex;
            this.computerIndex = computerIndex;
        }
    }

    private static final class UnitRef {
        final Unit unit;
        final int attack;
        final int seq;

        UnitRef(Unit unit, int attack, int seq) {
            this.unit = unit;
            this.attack = attack;
            this.seq = seq;
        }

        static final Comparator<UnitRef> ORDER = (a, b) -> {
            int c = Integer.compare(b.attack, a.attack); // desc by attack
            if (c != 0) return c;

            c = Integer.compare(a.seq, b.seq); // stable order within the same attack
            if (c != 0) return c;

            // ensure strict ordering for TreeSet
            return Integer.compare(System.identityHashCode(a.unit), System.identityHashCode(b.unit));
        };
    }
}