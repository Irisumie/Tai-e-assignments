/*
 * Tai-e: A Static Analysis Framework for Java
 *
 * Copyright (C) 2022 Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2022 Yue Li <yueli@nju.edu.cn>
 *
 * This file is part of Tai-e.
 *
 * Tai-e is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * Tai-e is distributed in the hope that it will be useful,but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Tai-e. If not, see <https://www.gnu.org/licenses/>.
 */

package pascal.taie.analysis.dataflow.solver;

import pascal.taie.analysis.dataflow.analysis.DataflowAnalysis;
import pascal.taie.analysis.dataflow.fact.DataflowResult;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.analysis.graph.cfg.Edge;

class IterativeSolver<Node, Fact> extends Solver<Node, Fact> {

    public IterativeSolver(DataflowAnalysis<Node, Fact> analysis) {
        super(analysis);
    }

    @Override
    protected void doSolveForward(CFG<Node> cfg, DataflowResult<Node, Fact> result) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doSolveBackward(CFG<Node> cfg, DataflowResult<Node, Fact> result) {
        boolean changed;
        do {
            changed = false;
            for (Node node : cfg) {
                // OUT[node] = meet over successors' IN
                Fact newOut = analysis.newInitialFact();
                for (Edge<Node> e : cfg.getOutEdgesOf(node)) {
                    Node succ = e.getTarget();
                    Fact succIn = result.getInFact(succ);
                    if (analysis.needTransferEdge(e)) {
                        succIn = analysis.transferEdge(e, succIn);
                    }
                    analysis.meetInto(succIn, newOut);
                }
                result.setOutFact(node, newOut);

                // transfer：由 OUT 推导并更新 IN
                Fact inFact = result.getInFact(node);
                boolean nodeChanged = analysis.transferNode(node, inFact, newOut);
                if (nodeChanged) {
                    changed = true;
                }
            }
        } while (changed);
    }
}
