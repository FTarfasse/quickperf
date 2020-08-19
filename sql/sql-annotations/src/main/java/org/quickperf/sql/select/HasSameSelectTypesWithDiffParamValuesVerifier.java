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

package org.quickperf.sql.select;

import org.quickperf.SystemProperties;
import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.DisableSameSelectTypesWithDifferentParamValues;
import org.quickperf.sql.framework.HibernateSuggestion;
import org.quickperf.sql.framework.JdbcSuggestion;
import org.quickperf.sql.framework.MicronautSuggestion;
import org.quickperf.sql.framework.SqlFrameworksInClassPath;
import org.quickperf.sql.select.analysis.SelectAnalysis;

public class HasSameSelectTypesWithDiffParamValuesVerifier implements VerifiablePerformanceIssue<DisableSameSelectTypesWithDifferentParamValues, SelectAnalysis> {

    public static final HasSameSelectTypesWithDiffParamValuesVerifier INSTANCE = new HasSameSelectTypesWithDiffParamValuesVerifier();

    private HasSameSelectTypesWithDiffParamValuesVerifier() { }

    @Override
    public PerfIssue verifyPerfIssue(DisableSameSelectTypesWithDifferentParamValues annotation
                                   , SelectAnalysis selectAnalysis) {

        if(selectAnalysis.hasSameSelectTypesWithDifferentParamValues()) {
            return buildPerfIssue(selectAnalysis);
        }

        return PerfIssue.NONE;

    }

    private PerfIssue buildPerfIssue(SelectAnalysis selectAnalysis) {

        if(!SystemProperties.SIMPLIFIED_SQL_DISPLAY.evaluate()) {
            String description = buildDescriptionWithRoundTripsAndPossiblyNPlusOneSelect();
            return new PerfIssue(description);
        }

        String baseDescription = buildBaseDescription();
        return new PerfIssue(baseDescription);
    }

    private String buildBaseDescription() {
        return "Same SELECT types with different parameter values";
    }

    private String buildDescriptionWithRoundTripsAndPossiblyNPlusOneSelect() {

        String description = buildBaseDescription()
                + System.lineSeparator()
                + System.lineSeparator()
                + JdbcSuggestion.SERVER_ROUND_TRIPS.getMessage();

        if(SqlFrameworksInClassPath.INSTANCE.containsHibernate()) {
            String nPlusOneSelectMessage = HibernateSuggestion.N_PLUS_ONE_SELECT
                    .getMessage();
            description += System.lineSeparator()
                    + nPlusOneSelectMessage;
        }

        if(SqlFrameworksInClassPath.INSTANCE.containsMicronaut()) {
            String micronautNPlusOneSelectMessage = MicronautSuggestion.N_PLUS_ONE_SELECT
                    .getMessage();
            description += System.lineSeparator() + micronautNPlusOneSelectMessage;
        }

        return description;
    }

}
