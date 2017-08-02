package pl.edu.agh.idziak.asw.impl.grid2d;

import pl.edu.agh.idziak.asw.common.ArrayBasedCache;
import pl.edu.agh.idziak.asw.common.Utils;
import pl.edu.agh.idziak.asw.impl.grid2d.internal.GridEntityStateUniquenessCache;
import pl.edu.agh.idziak.asw.impl.grid2d.internal.GridStateChangeCache;
import pl.edu.agh.idziak.asw.model.StateSpace;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static pl.edu.agh.idziak.asw.common.Utils.arrayCopy;

/**
 * Created by Tomasz on 29.06.2016.
 */
public class GridStateSpace implements StateSpace<GridCollectiveState> {

    private static final int MOORE_NEIGHBORS_COUNT = 9;
    private static final int VON_NEUMANN_NEIGHBORS_COUNT = 5;

    private final Neighborhood neighborhood = Neighborhood.MOORE; // FIXME parametrize

    private final byte[][] space;
    private final int rows;
    private final int cols;
    private final int neighborsCount;
    private final Map<Integer, Integer> collectiveStateNeighborsCount;


    private ArrayBasedCache<GridCollectiveState> stateCache;

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public enum Neighborhood {
        VON_NEUMANN, MOORE
    }

    public GridStateSpace(byte[][] space) {
        this.space = arrayCopy(checkNotNull(space));
        this.rows = space.length;
        this.cols = space[0].length;
        this.stateCache = new ArrayBasedCache<>(Math.max(rows, cols),
                state -> new GridCollectiveState(Utils.arrayCopy(state)));
        neighborsCount = getNeighborsCount(neighborhood);
        collectiveStateNeighborsCount = new HashMap<>();
    }

    public GridStateSpace(int[][] space) {
        this(Utils.toByteArray(space));
    }

    public boolean isValidState(GridCollectiveState state) {
        byte[] array = state.getArray();
        return isValidState(array);
    }

    private boolean isValidState(byte[] array) {
        GridEntityStateUniquenessCache cache = newEntityStateUniquenessCache();

        for (int i = 0; i < array.length; i += 2) {
            if (!cache.checkIfUniqueAndStore(array[i], array[i + 1])) {
                return false;
            }
        }
        return true;
    }


    public List<GridCollectiveState> getNeighborStatesOf(GridCollectiveState collectiveState) {
        byte[] thisStateArray = collectiveState.getArray();
        int colStatesCount = thisStateArray.length / 2;
        List<GridCollectiveState> outputList = new ArrayList<>(getCollectiveStateNeighborsCount(colStatesCount));
        List<List<byte[]>> combinatorialArray = buildCombinatorialArray(thisStateArray);

        generateNeighborStateList(combinatorialArray, collectiveState, outputList);

        return outputList;
    }

    public List<GridEntityState> getNeighborStatesOf(GridEntityState entityState) {
        return getNeighborStatesOf((byte) entityState.getRow(), (byte) entityState.getCol())
                .stream()
                .map(bytes -> GridEntityState.of(bytes[0], bytes[1]))
                .collect(Collectors.toList());
    }

    private boolean isValidStateTransition(byte[] currentStateArray, byte[] neighborStateArray) {
        GridEntityStateUniquenessCache uniquenessCache = newEntityStateUniquenessCache();
        GridStateChangeCache stateChangeCache = newStateChangeCache();

        for (int j = 0; j < neighborStateArray.length; j += 2) {
            byte fromRow = currentStateArray[j];
            byte fromCol = currentStateArray[j + 1];
            byte toRow = neighborStateArray[j];
            byte toCol = neighborStateArray[j + 1];

            if (!uniquenessCache.checkIfUniqueAndStore(toRow, toCol)
                    || !stateChangeCache.checkIfValidStateChangeAndStore(fromRow, fromCol, toRow, toCol))
                return false;
        }
        return true;
    }

    private GridStateChangeCache newStateChangeCache() {
        return new GridStateChangeCache(rows, cols);
    }

    private GridEntityStateUniquenessCache newEntityStateUniquenessCache() {
        return new GridEntityStateUniquenessCache(rows, cols);
    }

    private int getCollectiveStateNeighborsCount(int colStatesCount) {
        return collectiveStateNeighborsCount.computeIfAbsent(colStatesCount,
                csc -> (int) Math.pow(neighborsCount, csc));
    }

    private void generateNeighborStateList(List<List<byte[]>> combinatorialArray, GridCollectiveState sourceState, List<GridCollectiveState> outputStateList) {
        int collectiveStatesCount = combinatorialArray.size();
        byte[] currentCombination = initializeFirstCombination(combinatorialArray, collectiveStatesCount);
        if (isValidStateTransition(sourceState.getArray(), currentCombination)) {
            outputStateList.add(stateCache.get(currentCombination));
        }

        int[] verticalPositions = new int[collectiveStatesCount];
        int horizPos = 0;
        while (true) {
            List<byte[]> vertList = combinatorialArray.get(horizPos);
            int newVertPos = ++verticalPositions[horizPos];
            if (newVertPos < vertList.size()) {
                byte[] entityState = vertList.get(newVertPos);
                currentCombination[2 * horizPos] = entityState[0];
                currentCombination[2 * horizPos + 1] = entityState[1];
                if (isValidStateTransition(sourceState.getArray(), currentCombination)) {
                    outputStateList.add(stateCache.get(currentCombination));
                }
                horizPos = 0;
            } else {
                verticalPositions[horizPos] = 0;
                currentCombination[2 * horizPos] = vertList.get(0)[0];
                currentCombination[2 * horizPos + 1] = vertList.get(0)[1];
                if (++horizPos >= verticalPositions.length) {
                    break;
                }
            }
        }
    }

    private byte[] initializeFirstCombination(List<List<byte[]>> combinatorialArray, int collectiveStatesCount) {
        byte[] currentCombination = new byte[collectiveStatesCount * 2];

        // initialize first combination
        for (int i = 0; i < collectiveStatesCount; i++) {
            byte[] entityState = combinatorialArray.get(i).get(0);
            currentCombination[2 * i] = entityState[0];
            currentCombination[2 * i + 1] = entityState[1];
        }
        return currentCombination;
    }

    private List<List<byte[]>> buildCombinatorialArray(byte[] states) {
        List<List<byte[]>> output = new ArrayList<>(states.length);
        int colStatesCount = states.length;
        for (int i = 0; i < colStatesCount; i += 2) {
            output.add(getNeighborStatesOf(states[i], states[i + 1]));
        }
        return output;
    }

    private ArrayList<byte[]> getNeighborStatesOf(byte row, byte col) {
        ArrayList<byte[]> states = new ArrayList<>(neighborsCount);

        addState(row, col, states);
        if (col > 0)
            addState(row, (byte) (col - 1), states);
        if (col < cols - 1)
            addState(row, (byte) (col + 1), states);

        if (row > 0) {
            addState((byte) (row - 1), col, states);
            if (neighborhood == Neighborhood.MOORE) {
                if (col > 0)
                    addState((byte) (row - 1), (byte) (col - 1), states);
                if (col < cols - 1)
                    addState((byte) (row - 1), (byte) (col + 1), states);
            }
        }

        if (row < rows - 1) {
            addState((byte) (row + 1), col, states);
            if (neighborhood == Neighborhood.MOORE) {
                if (col > 0)
                    addState((byte) (row + 1), (byte) (col - 1), states);
                if (col < cols - 1)
                    addState((byte) (row + 1), (byte) (col + 1), states);
            }
        }

        return states;
    }

    private void addState(byte destRow, byte destCol, ArrayList<byte[]> states) {
        byte weight = space[destRow][destCol];
        if (weight <= 0) {
            states.add(new byte[]{destRow, destCol});
        }
    }

    public byte[][] getGridArray() {
        return space;
    }

    public int[][] getGridIntegerArray() {
        return Utils.toIntegerArray(space);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (byte[] row : space) {
            sb.append(Arrays.toString(row))
                    .append("\n");
        }
        return sb.toString();
    }

    private static int getNeighborsCount(Neighborhood neighborhood) {
        switch (neighborhood) {
            case VON_NEUMANN:
                return VON_NEUMANN_NEIGHBORS_COUNT;
            case MOORE:
                return MOORE_NEIGHBORS_COUNT;
        }
        throw new AssertionError("Should not happen, unknown neighborhood");
    }

    GridCollectiveState collectiveStateFrom(byte[] array) {
        if (!isValidState(array)) {
            return null;
        }
        return new GridCollectiveState(array);
    }

    GridCollectiveState collectiveStateFrom(List<GridEntityState> states) {
        byte[] array = new byte[states.size() * 2];
        for (int i = 0; i < states.size(); i++) {
            array[2 * i] = (byte) states.get(i).getRow();
            array[2 * i + 1] = (byte) states.get(i).getCol();
        }
        return collectiveStateFrom(array);
    }
}