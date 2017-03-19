package pl.edu.agh.idziak.asw.impl.gridarray;

/**
 * Created by Tomasz on 13.03.2017.
 */
public class G2DNonCollectiveDevZonesFinder {

//    private int warningProximity = 2;
//    private int expansionFactor = 1;
//
//    @Override
//    public Set<G2DSubspace> findDeviationZones(G2DOptInputPlan inputPlan, CollectivePath<G2DOptCollectiveState> collectivePath) {
//        Accumulator acc = new Accumulator(inputPlan, collectivePath);
//
//        for (G2DOptCollectiveState currentCollectiveState : collectivePath.get()) {
//            List<List<Map.Entry<?, G2DEntityState>>> setsOfProximateStates = buildSetsOfProximateStates(currentCollectiveState);
//
//            Iterator<DeviationZoneBuilder> devZoneCacheIterator = acc.devZonesCache.iterator();
//
//            while (devZoneCacheIterator.hasNext()) {
//                DeviationZoneBuilder openDevZone = devZoneCacheIterator.next();
//                List<Map.Entry<?, G2DEntityState>> matchingProximateSet =
//                        findMatchingOpenSet(setsOfProximateStates, openDevZone.entities);
//
//                if (matchingProximateSet != null) {
//                    matchingProximateSet.forEach(entry -> openDevZone.containedStates.add(entry.getValue()));
//                    setsOfProximateStates.remove(matchingProximateSet);
//                } else {
//                    closeDeviationZone(acc, openDevZone, currentCollectiveState);
//                    devZoneCacheIterator.remove();
//                }
//            }
//
//            setsOfProximateStates.forEach(entries -> {
//                ImmutableSet.Builder<Object> entitySetBuilder = ImmutableSet.builder();
//                HashSet<G2DEntityState> stateSet = new HashSet<>();
//
//                entries.forEach(entry -> {
//                    entitySetBuilder.add(entry.getKey());
//                    stateSet.add(entry.getValue());
//                });
//
//                DeviationZoneBuilder newDevZoneBuilder = new DeviationZoneBuilder(entitySetBuilder.build(), stateSet);
//                acc.devZonesCache.add(newDevZoneBuilder);
//            });
//
//
//        }
//
//        acc.devZonesCache.forEach(deviationZoneBuilder ->
//                closeDeviationZone(acc, deviationZoneBuilder, lastElementOf(collectivePath.get())));
//        return ImmutableSet.copyOf(acc.outputDeviationZones);
//    }
//
//    private void closeDeviationZone(Accumulator acc, DeviationZoneBuilder devZoneToClose, G2DOptCollectiveState currentState) {
//        G2DOptStateSpace stateSpace = acc.inputPlan.getStateSpace();
//        Set<G2DEntityState> finalStateSet = new HashSet<>(devZoneToClose.containedStates);
//
//        devZoneToClose.containedStates.forEach(entityState ->
//                finalStateSet.addAll(stateSpace.getNeighborStatesOf(entityState)));
//
//        G2DOptCollectiveState lastContainedCollectiveState =
//                findLastCollectiveStateContainedInDevZone(acc, currentState, finalStateSet);
//
//        // Map<Object, G2DEntityState> targetCollectiveStateForDevZone = openDevZone.entities.stream().collect(
//        //         Collectors.toMap(o -> o, lastContainedCollectiveState::getStateForEntity)); // TODO collective dev zones strategy
//        //
//        // acc.outputDeviationZones.add(
//        //         new G2DNonCollectiveSubspace(expandedStateSet, G2DOptCollectiveState
//        //                 .from(targetCollectiveStateForDevZone)));
//
//        devZoneToClose.entities.forEach(entity -> {
//            G2DEntityState stateForEntity = lastContainedCollectiveState.getStateForEntity(entity);
//            G2DOptCollectiveState targetState = G2DOptCollectiveState.from(ImmutableMap.of(entity, stateForEntity));
//            acc.outputDeviationZones.add(new G2DNonCollectiveSubspace(finalStateSet, targetState));
//        });
//    }
//
//    private G2DOptCollectiveState findLastCollectiveStateContainedInDevZone(Accumulator acc, G2DOptCollectiveState currentState, Set<G2DEntityState> expandedStateSet) {
//        List<G2DOptCollectiveState> collectivePath = acc.collectivePath.get();
//        int indexOfLastContainedState = collectivePath.indexOf(currentState);
//        int size = collectivePath.size();
//        G2DOptCollectiveState lastContainedCollectiveState = collectivePath.get(indexOfLastContainedState);
//
//        for (int i = indexOfLastContainedState + 1; i < size; i++) {
//            G2DOptCollectiveState collectiveState = collectivePath.get(i);
//            if (!expandedStateSet.containsAll(collectiveState.getEntityStates().values())) {
//                break;
//            }
//            lastContainedCollectiveState = collectiveState;
//        }
//        return lastContainedCollectiveState;
//    }
//
//    private static List<Map.Entry<?, G2DEntityState>> findMatchingOpenSet(List<List<Map.Entry<?, G2DEntityState>>> setsOfProximateStates, Set<?> entitySet) {
//        return setsOfProximateStates.stream()
//                                    .filter(setOfProximateStates -> {
//                                        if (setOfProximateStates.size() != entitySet.size()) {
//                                            return false;
//                                        }
//                                        long matchingElements = setOfProximateStates.stream()
//                                                                                    .map(Map.Entry::getKey)
//                                                                                    .filter(entitySet::contains)
//                                                                                    .count();
//                                        return matchingElements == entitySet.size();
//                                    })
//                                    .findFirst()
//                                    .orElse(null);
//    }
//
//
//    private List<List<Map.Entry<?, G2DEntityState>>> buildSetsOfProximateStates(G2DOptCollectiveState collectiveState) {
//        List<List<Map.Entry<?, G2DEntityState>>> setsOfProximateStates = new ArrayList<>();
//
//        for (Map.Entry<?, G2DEntityState> entry : collectiveState.getEntityStates().entrySet()) {
//            G2DEntityState entityState = entry.getValue();
//            List<Map.Entry<?, G2DEntityState>> existingSetToExpand = null;
//            outsideLoop:
//            for (List<Map.Entry<?, G2DEntityState>> setOfProximateStates : setsOfProximateStates) {
//                for (Map.Entry<?, G2DEntityState> stateEntry : setOfProximateStates) {
//                    G2DEntityState cachedState = stateEntry.getValue();
//                    if (areStatesProximate(entityState, cachedState)) {
//                        existingSetToExpand = setOfProximateStates;
//                        break outsideLoop;
//                    }
//                }
//            }
//            if (existingSetToExpand != null) {
//                existingSetToExpand.add(entry);
//            } else {
//                List<Map.Entry<?, G2DEntityState>> newProximateSet = new LinkedList<>();
//                newProximateSet.add(entry);
//                setsOfProximateStates.add(newProximateSet);
//            }
//        }
//        return setsOfProximateStates;
//    }
//
//    private boolean areStatesProximate(G2DEntityState state1, G2DEntityState state2) {
//        int manhattanDistance = Math.abs(state1.getCol() - state2.getCol())
//                + Math.abs(state1.getRow() - state2.getRow());
//        return manhattanDistance <= warningProximity;
//    }
//
//    private class Accumulator {
//
//        private final G2DOptInputPlan inputPlan;
//        private final CollectivePath<G2DOptCollectiveState> collectivePath;
//        private final Set<DeviationZoneBuilder> devZonesCache = new HashSet<>();
//        private final List<G2DSubspace> outputDeviationZones = new LinkedList<>();
//
//        private Accumulator(G2DOptInputPlan inputPlan, CollectivePath<G2DOptCollectiveState> collectivePath) {
//            this.inputPlan = inputPlan;
//            this.collectivePath = collectivePath;
//        }
//
//    }
//
//    private class DeviationZoneBuilder {
//
//        private final Set<Object> entities;
//        private final Set<G2DEntityState> containedStates;
//
//        private DeviationZoneBuilder(Set<Object> entities, HashSet<G2DEntityState> initialStateSet) {
//            this.entities = ImmutableSet.copyOf(entities);
//            this.containedStates = initialStateSet;
//        }
//    }
}