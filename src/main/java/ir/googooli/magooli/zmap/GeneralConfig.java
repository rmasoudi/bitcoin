/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ir.googooli.magooli.zmap;

import java.util.List;

/**
 *
 * @author fr
 */
public class GeneralConfig {
    private String input;
    private String output;
    private List<String> terms;
    private String index;

    /**
     * @return the input
     */
    public String getInput() {
        return input;
    }

    /**
     * @param input the input to set
     */
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * @return the output
     */
    public String getOutput() {
        return output;
    }

    /**
     * @param output the output to set
     */
    public void setOutput(String output) {
        this.output = output;
    }

    /**
     * @return the terms
     */
    public List<String> getTerms() {
        return terms;
    }

    /**
     * @param terms the terms to set
     */
    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    /**
     * @return the index
     */
    public String getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(String index) {
        this.index = index;
    }
    
    
}
