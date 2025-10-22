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

package pascal.taie.analysis.dataflow.analysis;

import pascal.taie.analysis.dataflow.fact.SetFact;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.exp.RValue;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Stmt;

/**
 * Implementation of classic live variable analysis.
 */
public class LiveVariableAnalysis extends
        AbstractDataflowAnalysis<Stmt, SetFact<Var>> {

    public static final String ID = "livevar";

    public LiveVariableAnalysis(AnalysisConfig config) {
        super(config);
    }

    @Override
    public boolean isForward() {
        return false;
    }

    @Override
    public SetFact<Var> newBoundaryFact(CFG<Stmt> cfg) {
        // 后向分析的边界条件：exit 处的 OUT = ∅
        return new SetFact<>();
    }

    @Override
    public SetFact<Var> newInitialFact() {
        // 初始条件：对所有非边界结点，IN/OUT 初始为 ∅
        return new SetFact<>();
    }

    @Override
    public void meetInto(SetFact<Var> fact, SetFact<Var> target) {
        // 活跃变量分析的 meet 为并集
        target.union(fact);
    }

    @Override
    public boolean transferNode(Stmt stmt, SetFact<Var> in, SetFact<Var> out) {
        // in = USE(stmt) ∪ (out − DEF(stmt))
        SetFact<Var> newIn = out.copy();
        stmt.getDef().ifPresent(def -> {
            if (def instanceof Var v) {
                newIn.remove(v);
            }
        });
        for (RValue use : stmt.getUses()) {
            if (use instanceof Var v) {
                newIn.add(v);
            }
        }
        if (!newIn.equals(in)) {
            in.set(newIn);
            return true;
        }
        return false;
    }
}
