package org.teslasoft.core.api;

/**********************************
* Test class. Please do not use it
***********************************/

@Deprecated
public class Deprecation {

    @Deprecated
    public static int deprecatedMethod() {
        return 0;
    }

    private int variable;

    public int getVariable() {
        return variable;
    }

    public void setVariable(int variable) {
        this.variable = variable;
    }
}
