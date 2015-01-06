/* ****************************************************************************
 * Copyright (c) 2011-2014 VMware, Inc. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * ****************************************************************************/

package com.vmware.upgrade.progress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vmware.upgrade.TestGroups;
import com.vmware.upgrade.progress.ExecutionState;
import com.vmware.upgrade.progress.ExecutionStateAggregator;
import com.vmware.upgrade.progress.impl.DefaultExecutionStateAggregator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Predicate;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for {@link DefaultExecutionStateAggregator}
 *
 * These test cases verify that valid state transitions of the child tasks produce valid state
 * transitions of the parent task when parent task state is calculated using
 * {@code ExecutionStateAggregator#aggegate(Collection<ExecutionState> ExecutionStates)}.
 *
 * @see {@link ExecutionState}
 *
 * @author Zach Shepherd <shepherdz@vmware.com>
 * @version 1.0
 * @since 1.0
 */
public class DefaultExecutionStateAggregatorTest {
    private static final String DEPTH_TWO_PROVIDER = "depth-two-provider";

    /**
     * Perform tests for depth-two tasks
     *
     * @param before
     *          the {@code ExecutionState}s of the children before
     * @param after
     *          the {@code ExecutionState}s of the children after
     */
    @Test(groups = { TestGroups.UNIT }, dataProvider = DEPTH_TWO_PROVIDER)
    public void exhaustiveDepthTwoTest(List<ExecutionState> before, List<ExecutionState> after) {

        ExecutionStateAggregator stateAggregator = new DefaultExecutionStateAggregator();

        final ExecutionState beforeState = stateAggregator.aggregate(before);
        final ExecutionState afterState = stateAggregator.aggregate(after);

        String errorMessage = beforeState + " cannot transition to " + afterState;

        AssertJUnit.assertTrue(errorMessage, beforeState.equals(afterState) || beforeState.canTransitionTo(afterState));
    }

    @SuppressWarnings("unchecked")
    @DataProvider(name = DEPTH_TWO_PROVIDER)
    public Object[][] generateAllDepthTwoTransitions() {
        return mapToObjectArray(ListUtils.union(ListUtils.union(
                computeDepthTwoPairWithStableTransitionStates(),
                computeDepthTwoPairWithoutStableTransitionStates()),
                computeDepthTwoTripleTransitionStates()));
    }

    private Set<ExecutionState> computeStateTransitions(final ExecutionState state) {
        EnumSet<ExecutionState> states = EnumSet.allOf(ExecutionState.class);
        CollectionUtils.filter(states, new Predicate(){
            @Override
            public boolean evaluate(Object object) {
                return state.canTransitionTo((ExecutionState) object);
            }});
        return states;
    }

    private Map<ExecutionState, Set<ExecutionState>> computeAllStateTransitions() {
        Map<ExecutionState, Set<ExecutionState>> stateTransitions = new HashMap<ExecutionState, Set<ExecutionState>>();

        for (ExecutionState state : EnumSet.allOf(ExecutionState.class)) {
            stateTransitions.put(state, computeStateTransitions(state));
        }

        return stateTransitions;
    }

    private List<Mapping<List<ExecutionState>>> computeDepthTwoPairWithStableTransitionStates() {
        Map<ExecutionState, Set<ExecutionState>> stateTransitions = computeAllStateTransitions();
        Set<ExecutionState> states = EnumSet.allOf(ExecutionState.class);

        List<Mapping<List<ExecutionState>>> depthTwoTransitions = new ArrayList<Mapping<List<ExecutionState>>>();

        for (Map.Entry<ExecutionState, Set<ExecutionState>> stateTransition : stateTransitions.entrySet()) {
            ExecutionState initialState = stateTransition.getKey();
            Set<ExecutionState> finalStates = stateTransition.getValue();
            for (ExecutionState finalState : finalStates) {
                for (ExecutionState stableState : states) {
                    List<ExecutionState> depthTwoInitialStates = Arrays.asList(new ExecutionState[]{initialState, stableState});
                    List<ExecutionState> depthTwoFinalStates = Arrays.asList(new ExecutionState[]{finalState, stableState});
                    depthTwoTransitions.add(new Mapping<List<ExecutionState>>(depthTwoInitialStates, depthTwoFinalStates));
                }
            }
        }

        return depthTwoTransitions;
    }

    private List<Mapping<List<ExecutionState>>> computeDepthTwoPairWithoutStableTransitionStates() {
        Map<ExecutionState, Set<ExecutionState>> stateTransitions = computeAllStateTransitions();

        List<Mapping<List<ExecutionState>>> depthTwoTransitions = new ArrayList<Mapping<List<ExecutionState>>>();

        for (Map.Entry<ExecutionState, Set<ExecutionState>> stateTransitionA : stateTransitions.entrySet()) {
            ExecutionState initialStateA = stateTransitionA.getKey();
            Set<ExecutionState> finalStatesA = stateTransitionA.getValue();
            for (ExecutionState finalStateA : finalStatesA) {
                for (Map.Entry<ExecutionState, Set<ExecutionState>> stateTransitionB : stateTransitions.entrySet()) {
                    ExecutionState initialStateB = stateTransitionB.getKey();
                    Set<ExecutionState> finalStatesB = stateTransitionB.getValue();
                    for (ExecutionState finalStateB : finalStatesB) {
                        List<ExecutionState> depthTwoInitialStates = Arrays.asList(new ExecutionState[]{initialStateA, initialStateB});
                        List<ExecutionState> depthTwoFinalStates = Arrays.asList(new ExecutionState[]{finalStateA, finalStateB});
                        depthTwoTransitions.add(new Mapping<List<ExecutionState>>(depthTwoInitialStates, depthTwoFinalStates));
                    }
                }
            }
        }

        return depthTwoTransitions;
    }

    private List<Mapping<List<ExecutionState>>> computeDepthTwoTripleTransitionStates() {
        Map<ExecutionState, Set<ExecutionState>> stateTransitions = computeAllStateTransitions();
        Set<ExecutionState> states = EnumSet.allOf(ExecutionState.class);

        List<Mapping<List<ExecutionState>>> depthTwoTransitions = new ArrayList<Mapping<List<ExecutionState>>>();

        for (Map.Entry<ExecutionState, Set<ExecutionState>> stateTransitionA : stateTransitions.entrySet()) {
            ExecutionState initialStateA = stateTransitionA.getKey();
            Set<ExecutionState> finalStatesA = stateTransitionA.getValue();
            for (ExecutionState finalStateA : finalStatesA) {
                for (Map.Entry<ExecutionState, Set<ExecutionState>> stateTransitionB : stateTransitions.entrySet()) {
                    ExecutionState initialStateB = stateTransitionB.getKey();
                    Set<ExecutionState> finalStatesB = stateTransitionB.getValue();
                    for (ExecutionState finalStateB : finalStatesB) {
                        for (ExecutionState stableState : states) {
                            List<ExecutionState> depthTwoInitialStates = Arrays.asList(new ExecutionState[]{initialStateA, initialStateB, stableState});
                            List<ExecutionState> depthTwoFinalStates = Arrays.asList(new ExecutionState[]{finalStateA, finalStateB, stableState});
                            depthTwoTransitions.add(new Mapping<List<ExecutionState>>(depthTwoInitialStates, depthTwoFinalStates));
                        }
                    }
                }
            }
        }

        return depthTwoTransitions;
    }

    private <T> Object[][] mapToObjectArray(List<Mapping<T>> mappingList) {
        Object[][] objects = new Object[mappingList.size()][];
        for (int index = 0; index < mappingList.size(); index++) {
            objects[index] = new Object[] {mappingList.get(index).getSource(), mappingList.get(index).getDestination()};
        }

        return objects;
    }

    /**
     * Represents a mapping between two elements (such as a before and after {@code ProgressReport}
     * or the before and after {@code ExecutionState}s of the children of a {@code ProgressReport}
     */
    private static class Mapping<T> {
        private final T source;
        private final T destination;

        public Mapping(T source, T destination) {
            this.source = source;
            this.destination = destination;
        }

        public T getSource() {
            return source;
        }

        public T getDestination() {
            return destination;
        }
    }
}
