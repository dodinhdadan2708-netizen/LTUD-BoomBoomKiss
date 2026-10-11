package boomboomkiss.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

final class AttackResult {
    private final List<Position> opened = new ArrayList<Position>();
    private final Queue<Position> pending = new ArrayDeque<Position>();
    private boolean kissHit;
    private boolean finished;

    List<Position> getOpened() { return Collections.unmodifiableList(opened); }
    boolean isKissHit() { return kissHit; }

    private void requireResolving() {
        if (finished) throw new IllegalStateException("Attack result is complete");
    }

    void recordOpened(Position position) { requireResolving(); opened.add(position); }
    void markKissHit() { requireResolving(); kissHit = true; }
    void enqueue(Position position) { requireResolving(); pending.add(position); }
    boolean hasPending() { return !pending.isEmpty(); }
    Position nextPending() { requireResolving(); return pending.remove(); }
    void finish() { requireResolving(); finished = true; }
}
