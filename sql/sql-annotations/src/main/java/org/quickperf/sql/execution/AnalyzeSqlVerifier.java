package org.quickperf.sql.execution;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.AnalyzeSql;

public class AnalyzeSqlVerifier implements VerifiablePerformanceIssue<AnalyzeSql, SqlAnalysis> {

    public static AnalyzeSqlVerifier INSTANCE = new AnalyzeSqlVerifier();

    private AnalyzeSqlVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(AnalyzeSql annotation, SqlAnalysis measure) {
        long number = measure.getSelectAnalysis().getSelectNumber().getValue();
        if (number > 0) {
            return new PerfIssue(number + " select detected");
        }

        return PerfIssue.NONE;
    }

}
