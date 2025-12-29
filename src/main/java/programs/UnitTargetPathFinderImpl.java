package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.UnitTargetPathFinder;
import com.battle.heroes.army.programs.Edge;

import java.util.*;

public final class UnitTargetPathFinderImpl implements UnitTargetPathFinder {

    private static final int WIDTH = 27;
    private static final int HEIGHT = 21;

    // 8 направлений (включая диагонали)
    private static final int[] DX = {-1, -1, -1, 0, 0, 1, 1, 1};
    private static final int[] DY = {-1, 0, 1, -1, 1, -1, 0, 1};

    @Override
    public List<Edge> getTargetPath(Unit attackUnit,
                                    Unit targetUnit,
                                    List<Unit> existingUnitList) {

        if (attackUnit == null || targetUnit == null) {
            return Collections.emptyList();
        }

        int sx = attackUnit.getxCoordinate();
        int sy = attackUnit.getyCoordinate();
        int tx = targetUnit.getxCoordinate();
        int ty = targetUnit.getyCoordinate();

        boolean[][] blocked = buildBlockedMap(existingUnitList, attackUnit, targetUnit);

        PriorityQueue<Node> open = new PriorityQueue<>();
        boolean[][] visited = new boolean[WIDTH][HEIGHT];
        Node[][] parent = new Node[WIDTH][HEIGHT];

        Node start = new Node(sx, sy, 0, heuristic(sx, sy, tx, ty));
        open.add(start);

        while (!open.isEmpty()) {
            Node cur = open.poll();

            if (visited[cur.x][cur.y]) continue;
            visited[cur.x][cur.y] = true;

            if (cur.x == tx && cur.y == ty) {
                return buildPath(cur);
            }

            for (int i = 0; i < 8; i++) {
                int nx = cur.x + DX[i];
                int ny = cur.y + DY[i];

                if (!inside(nx, ny)) continue;
                if (blocked[nx][ny]) continue;
                if (visited[nx][ny]) continue;

                int g = cur.g + 1;
                int h = heuristic(nx, ny, tx, ty);
                Node next = new Node(nx, ny, g, h);
                next.prev = cur;

                open.add(next);
            }
        }

        return Collections.emptyList(); // путь не найден
    }

    private boolean[][] buildBlockedMap(List<Unit> units, Unit attacker, Unit target) {
        boolean[][] blocked = new boolean[WIDTH][HEIGHT];

        if (units == null) return blocked;

        for (Unit u : units) {
            if (u == null) continue;
            if (u == attacker || u == target) continue;
            if (!u.isAlive()) continue;

            int x = u.getxCoordinate();
            int y = u.getyCoordinate();
            if (inside(x, y)) {
                blocked[x][y] = true;
            }
        }
        return blocked;
    }

    private boolean inside(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    // Chebyshev distance (для диагонального движения)
    private int heuristic(int x, int y, int tx, int ty) {
        return Math.max(Math.abs(x - tx), Math.abs(y - ty));
    }

    private List<Edge> buildPath(Node end) {
        LinkedList<Edge> path = new LinkedList<>();
        Node cur = end;
        while (cur != null) {
            path.addFirst(new Edge(cur.x, cur.y));
            cur = cur.prev;
        }
        return path;
    }

    private static final class Node implements Comparable<Node> {
        final int x, y;
        final int g; // distance from start
        final int f; // g + h
        Node prev;

        Node(int x, int y, int g, int h) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = g + h;
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.f, o.f);
        }
    }
}
