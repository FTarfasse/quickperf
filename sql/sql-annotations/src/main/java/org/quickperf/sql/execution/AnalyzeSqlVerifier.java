/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

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
            SqlExecutions sqlExecutions = sqlAnalysis.getSqlExecutions();

            long selectCount = sqlExecutions.retrieveQueryNumberOfType(QueryType.SELECT);
            if (selectCount > 0){
                sqlReport.append("SELECT: ")
                         .append(selectCount)
                         .append(System.lineSeparator());
            }

            long insertCount = sqlExecutions.retrieveQueryNumberOfType(QueryType.INSERT);
            if(insertCount > 0){
                sqlReport.append("INSERT: ")
                        .append(insertCount)
                        .append(System.lineSeparator());
            }

            long updateCount = sqlExecutions.retrieveQueryNumberOfType(QueryType.UPDATE);
            if(updateCount > 0){
                sqlReport.append("UPDATE: ")
                        .append(updateCount)
                        .append(System.lineSeparator());
            }

            long deleteCount = sqlExecutions.retrieveQueryNumberOfType(QueryType.DELETE);
            if(deleteCount > 0){
                sqlReport.append("DELETE: ")
                        .append(deleteCount)
                        .append(System.lineSeparator());
            }

            pw.printf(annotation.format(), sqlReport);

        }

        return PerfIssue.NONE;
    }

}