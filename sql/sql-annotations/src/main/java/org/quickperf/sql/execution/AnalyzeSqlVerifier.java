package org.quickperf.sql.execution;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.AnalyzeSql;
import org.quickperf.writer.PrintWriterBuilder;
import org.quickperf.writer.WriterFactory;

import java.io.PrintWriter;

public class AnalyzeSqlVerifier implements VerifiablePerformanceIssue<AnalyzeSql, SqlAnalysis> {

    public static AnalyzeSqlVerifier INSTANCE = new AnalyzeSqlVerifier();

    private AnalyzeSqlVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(AnalyzeSql annotation, SqlAnalysis sqlAnalysis) {
        //SelectAnalysis selectAnalysis = SelectAnalysisExtractor.INSTANCE.extractPerfMeasureFrom(sqlExecutions);
        Class<? extends WriterFactory> writerFactoryClass = annotation.writerFactory();
        try (PrintWriter pw = PrintWriterBuilder.INSTANCE.buildPrintWriterFrom(writerFactoryClass)) {
            String sqlReport = "";
            long number = sqlAnalysis.getSelectAnalysis().getSelectNumber().getValue();
            long selectNumber = sqlAnalysis.getSelectAnalysis().getSelectNumber().getValue();
//            pw.append("JDBC execution: " + sqlAnalysis.getJdbcQueryExecutionNumber().getValue());

            sqlReport += "SELECT: " + selectNumber;
            // "[JDBC QUERY EXECUTION (executeQuery, executeBatch, ...)]"
            //sqlExecutions.toString();

//            return new PerfIssue(pw.toString());
            pw.printf(annotation.format(), sqlReport);

        }

        return PerfIssue.NONE;
    }

}
