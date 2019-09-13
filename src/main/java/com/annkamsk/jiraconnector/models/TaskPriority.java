package com.annkamsk.jiraconnector.models;

public enum TaskPriority {

    LOW {
        @Override
        public String toString() {
            return "Low";
        }

        @Override
        public Integer toInteger() {
            return 3;
        }
    },
    MEDIUM {
        @Override
        public String toString() {
            return "Medium";
        }

        @Override
        public Integer toInteger() {
            return 2;
        }
    },
    HIGH {
        @Override
        public String toString() {
            return "High";
        }

        @Override
        public Integer toInteger() {
            return 1;
        }
    };

    public static TaskPriority fromInteger(int value) {
        switch (value) {
            case 1:
                return HIGH;
            case 2:
                return MEDIUM;
            case 3:
                return LOW;
            default:
                throw new IllegalArgumentException();
        }
    }

    public abstract String toString();

    public abstract Integer toInteger();
}
