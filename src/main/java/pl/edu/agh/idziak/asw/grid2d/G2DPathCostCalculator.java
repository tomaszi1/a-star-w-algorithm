package pl.edu.agh.idziak.asw.grid2d;

import pl.edu.agh.idziak.asw.CollectivePath;
import pl.edu.agh.idziak.common.WalkingPairIterator;

import java.util.List;

/**
 * Created by Tomasz on 13.08.2016.
 */
public class G2DPathCostCalculator {

    public static double calculateCost(CollectivePath<G2DCollectiveState, G2DEntityState, Integer> path,
                                       G2DStateSpace stateSpace) {
        List<G2DCollectiveState> states = path.get();

        WalkingPairIterator<G2DCollectiveState> it = new WalkingPairIterator<>(states);

        double cost = 0.0;

        while (it.hasNext()) {
            it.next();

            cost += stateSpace.getHeuristicCost(it.getFirst(), it.getSecond());
        }
        return cost;
    }
}