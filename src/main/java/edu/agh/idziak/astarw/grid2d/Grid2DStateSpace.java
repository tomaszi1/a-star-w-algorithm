package edu.agh.idziak.astarw.grid2d;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import edu.agh.idziak.astarw.EntityState;
import edu.agh.idziak.astarw.Position;
import edu.agh.idziak.astarw.StateSpace;
import edu.agh.idziak.common.CombinationsGenerator;
import edu.agh.idziak.common.DoubleIterator;
import edu.agh.idziak.common.SingleTypePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Tomasz on 29.06.2016.
 */
public class Grid2DStateSpace implements StateSpace<Grid2DGlobalState, Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(Grid2DStateSpace.class);

    private final int[][] space;

    public Grid2DStateSpace(int[][] space) {
        this.space = Preconditions.checkNotNull(space);
    }

    @Override
    public Set<Grid2DGlobalState> getNeighborStatesOf(Grid2DGlobalState globalState) {
        List<List<EntityState<Integer>>> choiceArray = new ArrayList<>();

        for (EntityState<Integer> state : globalState.getEntityStates()) {
            List<EntityState<Integer>> neighborStates = ImmutableList.copyOf(getNeighborStatesOf(state));
            choiceArray.add(neighborStates);
        }

        List<List<EntityState<Integer>>> combinations = CombinationsGenerator.generateCombinations(choiceArray, this::areAllStatesUnique);

        Set<Grid2DGlobalState> neighborStates = combinations.stream()
                .map(Grid2DGlobalState::fromEntityStates)
                .collect(Collectors.toSet());
        LOG.trace("Neighbor states of {} are {}",globalState,neighborStates);
        return neighborStates;
    }

    private boolean areAllStatesUnique(List<EntityState<Integer>> entityStates) {
        Set<EntityState<Integer>> set = new HashSet<>();
        for (EntityState<Integer> entityState : entityStates) {
            if (set.contains(entityState))
                return false;
            set.add(entityState);
        }
        return true;
    }

    public Set<EntityState<Integer>> getNeighborStatesOf(EntityState<Integer> entityState) {
        List<Integer> positions = entityState.get();
        if (positions.size() != 2) {
            throw new IllegalStateException("Invalid state size, required=2, got=" + positions.size());
        }
        int row = positions.get(0);
        int col = positions.get(1);

        Set<EntityState<Integer>> states = new HashSet<>();

        if (row > 0) {
            addState(row - 1, col, states);
        }
        if (row < space.length - 1) {
            addState(row + 1, col, states);
        }
        if (col > 0) {
            addState(row, col - 1, states);
        }
        if (col < space[0].length - 1) {
            addState(row, col + 1, states);
        }

        return states;
    }

    private void addState(int destRow, int destCol, Set<EntityState<Integer>> states) {
        int weight = space[destRow][destCol];
        if (weight < 10) {
            states.add(new Grid2DEntityState(destRow, destCol));
        }
    }

    public Integer getHeuristicDistance(Position<Integer> start, Position<Integer> end) {
        int startRow = start.get().get(0);
        int startCol = start.get().get(1);
        int endRow = end.get().get(0);
        int endCol = end.get().get(1);

        return (int) Math.sqrt(Math.pow(startRow - endRow, 2) + Math.pow(startCol - endCol, 2));
    }

    @Override
    public Integer getHeuristicDistance(Grid2DGlobalState start, Grid2DGlobalState end) {
        List<EntityState<Integer>> startEntityStates = start.getEntityStates();
        List<EntityState<Integer>> endEntityStates = end.getEntityStates();

        DoubleIterator<EntityState<Integer>> it = new DoubleIterator<>(startEntityStates, endEntityStates);

        int sum = 0;

        while (it.hasNext()) {
            SingleTypePair<EntityState<Integer>> pair = it.next();
            sum += getHeuristicDistance(pair.getOne(), pair.getTwo());
        }

        return sum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int[] row : space) {
            sb.append(Arrays.toString(row)).append("\n");
        }
        return sb.toString();
    }
}
