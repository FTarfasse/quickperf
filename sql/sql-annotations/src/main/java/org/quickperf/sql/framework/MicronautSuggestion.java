package org.quickperf.sql.framework;

public enum MicronautSuggestion implements QuickPerfSuggestion {

        N_PLUS_ONE_SELECT() {

            @Override
            public String getMessage() {
                String lightBulb = "\uD83D\uDCA1";
                String message =  System.lineSeparator()
                        + lightBulb + " Perhaps you are facing a N+1 select issue"
                        + System.lineSeparator()
                        + "\t With Micronaut Data, you may fix it by using the @Join annotation on your repository interface:"
                        + System.lineSeparator()
                        + "\t https://micronaut-projects.github.io/micronaut-data/latest/guide/#joinQueries";
                return message;
            }

        }
}
