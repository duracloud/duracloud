/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;



/**
 * @author: Andrew Woods
 * Date: Aug 4, 2010
 */
public interface ServiceResultListener {

    public enum State {
        STARTED, IN_PROGRESS, COMPLETE, STOPPED;
    }

    public void processServiceResult(ServiceResult result);

    public StatusMsg getProcessingStatus();

    public void setTotalWorkItems(long total);

    public void setProcessingState(State state);


    public static class StatusMsg {
        private long passed;
        private long failed;
        private long total;
        private State state;
        private String phase;
        private String previousPhaseStatus;

        public StatusMsg(long passed,
                         long failed,
                         long total,
                         State state,
                         String phase,
                         String previousPhaseStatus) {
            this.passed = passed;
            this.failed = failed;
            this.total = total;
            this.state = state;
            this.phase = phase;
            this.previousPhaseStatus = previousPhaseStatus;
        }

        public StatusMsg(String msg) {
            String[] msgs = msg.split(" \\.\\.\\. ");
            if (msgs.length == 2) {
                previousPhaseStatus = msgs[1];
            }

            String text = msgs[0];

            int dotIndex = text.indexOf(".");
            this.phase = text.substring(0, dotIndex);
            int colonIndex = text.indexOf(": ");
            this.state = State.valueOf(text.substring(dotIndex + 1,
                                                      colonIndex));

            String afterName = text.substring(colonIndex + 1, text.length());
            String[] parts = afterName.split("/");
            long passPlusFail = Long.parseLong(parts[0].trim());

            String[] moreParts = parts[1].split("\\[");
            if (moreParts[0].trim().equals("?")) {
                this.total = -1;
            } else {
                this.total = Long.parseLong(moreParts[0].trim());
            }

            if (moreParts.length == 2) {
                int failTextIndex = moreParts[1].indexOf("failure");
                String num = moreParts[1].substring(0, failTextIndex).trim();
                this.failed = Long.parseLong(num);
            } else {
                this.failed = 0;
            }

            this.passed = passPlusFail - this.failed;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(phase);
            sb.append(".");
            sb.append(state.name());
            sb.append(": ");
            sb.append(passed + failed);
            sb.append("/");
            sb.append(total > -1 ? total : "?");

            if (failed > 0) {
                String failSuffix = "failures";
                if (failed == 1) {
                    failSuffix = "failure";
                }
                sb.append(" [" + failed + " " + failSuffix + "]");
            }

            if (previousPhaseStatus != null) {
                sb.append(" ... ");
                sb.append(previousPhaseStatus);
            }
            return sb.toString();
        }

        public long getPassed() {
            return passed;
        }

        public long getFailed() {
            return failed;
        }

        public long getTotal() {
            return total;
        }

        public State getState() {
            return state;
        }

        public String getPhase() {
            return phase;
        }

        public String getPreviousPhaseStatus() {
            return previousPhaseStatus;
        }
    }



}
