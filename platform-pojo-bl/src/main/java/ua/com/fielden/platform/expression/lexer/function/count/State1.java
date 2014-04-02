package ua.com.fielden.platform.expression.lexer.function.count;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;

public class State1 extends AbstractState {

    public State1() {
        super("S1", false);
    }

    @Override
    protected AbstractState transition(final char symbol) throws NoTransitionAvailable {
        if (symbol == 'o' || symbol == 'O') {
            return getAutomata().getState("S2");
        }
        throw new NoTransitionAvailable("Invalid symbol '" + symbol + "'", this, symbol);
    }

}
