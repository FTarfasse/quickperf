package org.quickperf.sql.execution;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.SqlExecutions;
import org.quickperf.sql.annotation.AnalyzeSql;
import org.quickperf.sql.select.analysis.SelectAnalysis;
import org.quickperf.sql.select.analysis.SelectAnalysisExtractor;

public class AnalyzeSqlVerifier implements VerifiablePerformanceIssue<AnalyzeSql, SqlAnalysis> {

    public static AnalyzeSqlVerifier INSTANCE = new AnalyzeSqlVerifier();

    private AnalyzeSqlVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(AnalyzeSql annotation, SqlAnalysis sqlAnalysis) {
        //SelectAnalysis selectAnalysis = SelectAnalysisExtractor.INSTANCE.extractPerfMeasureFrom(sqlExecutions);

        long number = sqlAnalysis.getSelectAnalysis().getSelectNumber().getValue();

        System.out.println("JDBC execution: " + sqlAnalysis.getJdbcQueryExecutionNumber().getValue());

        Long selectNumber = sqlAnalysis.getSelectAnalysis().getSelectNumber().getValue();
        System.out.println("SELECT: " + selectNumber);
       /*
        if (number > 0) {
            return new PerfIssue(number + " select detected");
        }
        */

        // "[JDBC QUERY EXECUTION (executeQuery, executeBatch, ...)]"
        //sqlExecutions.toString();

        return PerfIssue.NONE;
    }

}
