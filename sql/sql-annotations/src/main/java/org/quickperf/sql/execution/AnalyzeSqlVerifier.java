package org.quickperf.sql.execution;

import net.ttddyy.dsproxy.QueryType;
import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.measure.PerfMeasure;
import org.quickperf.sql.SqlExecutions;
import org.quickperf.sql.annotation.AnalyzeSql;
import org.quickperf.sql.select.analysis.SelectAnalysis;
import org.quickperf.unit.Count;
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
        StringBuilder sqlReport = new StringBuilder();
        try (PrintWriter pw = PrintWriterBuilder.INSTANCE.buildPrintWriterFrom(writerFactoryClass)) {
            //SelectAnalysis type = sqlAnalysis.getValue();
            //if(type instanceof SelectAnalysis){
            // demeter law
            SelectAnalysis selectAnalysis = sqlAnalysis.getSelectAnalysis();
            Count selectNumber = selectAnalysis.getSelectNumber();
            sqlReport.append("SELECT: ")
                     .append((long) selectNumber.getValue())
                     .append(System.lineSeparator());



            SqlExecutions sqlExecutions = sqlAnalysis.getSqlExecutions();
            long insertCount = sqlExecutions.retrieveQueryNumberOfType(QueryType.INSERT);

            //}

            pw.printf(annotation.format(), sqlReport);
//          sqlReport.append(sqlAnalysis.getSqlExecutions().toString());

        }

        return PerfIssue.NONE;
    }

}
