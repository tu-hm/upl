package org.upl.parser.grammar.lr0;

public record Action(ActionType actionType, int number) {
    @Override
    public boolean equals(Object o) {
        if (o instanceof Action(ActionType type, int number1)) {
            return actionType.equals(type) && number == number1;
        }
        return false;
    }

    @Override
    public String toString() {
        if (actionType.equals(ActionType.ACCEPT)) return "acc";
        char c;
        if (actionType.equals(ActionType.SHIFT)) c = 's';
        else if (actionType.equals(ActionType.REDUCE)) c = 'r';
        else throw new RuntimeException("??? " + actionType);
        return String.format("%c%d", c, number);
    }
}
