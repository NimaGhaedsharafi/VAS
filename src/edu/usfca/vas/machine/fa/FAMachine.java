/*

[The "BSD licence"]
Copyright (c) 2004 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package edu.usfca.vas.machine.fa;

import edu.usfca.xj.foundation.XJXMLSerializable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import com.thoughtworks.xstream.XStream;

import sun.misc.Queue;
import sun.reflect.generics.tree.Tree;

public class FAMachine implements XJXMLSerializable {

    public static final int MACHINE_TYPE_DFA = 0;
    public static final int MACHINE_TYPE_NFA = 1;

    protected FAStates states = new FAStates();
    protected FAAlphabet alphabet = new FAAlphabet();
    protected FATransitions transitions = new FATransitions();

    protected Set<String> stateSet;

    protected int type = MACHINE_TYPE_DFA;

    protected transient String debugString;
    protected transient String debugLastSymbol;

    public FAMachine() {
        init();
    }

    public FAMachine(Set<Set<String>> statesSet, Set<FATransition> transitionsSet, String startState, List finalStates) {
        init();
        addState(statesSet, startState, finalStates);
        addTransitions(transitionsSet);
    }

    public void init() {
        alphabet.setMachine(this);

        alphabet.setSymbolsString("01");
        stateSet = new HashSet<String>();
    }

    public void setStates(FAStates states) {
        this.states = states;
    }

    public FAStates getStates() {
        return states;
    }

    public void setAlphabet(FAAlphabet alphabet) {
        this.alphabet = alphabet;
        alphabet.setMachine(this);
    }

    public FAAlphabet getAlphabet() {
        return alphabet;
    }

    public void setTransitions(FATransitions transitions) {
        this.transitions = transitions;
    }

    public FATransitions getTransitions() {
        return transitions;
    }

    public void addState(FAState s) {
        states.addState(s);
    }

    public void addState(Set<Set<String>> set, String startState, List finalStates) {
        Iterator<Set<String>> iterator = set.iterator();
        while(iterator.hasNext()) {
            Set stateSet = (HashSet)iterator.next();
            FAState state = new FAState(stateSet.toString());

            for(int f=0; f<finalStates.size(); f++) {                
                if(stateSet.contains(finalStates.get(f))) {
                    state.accepted = true;
                    break;
                }
            }

            if(state.name.equals(startState))
                state.start = true;
            addState(state);
        }
    }

    public void removeState(FAState s) {
        states.removeState(s);
        transitions.removeState(s.name);
    }

    public void renameState(FAState s, String oldName, String newName) {
        s.name = newName;
        transitions.renameState(oldName, newName);
    }

    public boolean containsStateName(String name) {
        return states.contains(name);
    }

    public List getStateList() {
        return states.getStates();
    }

    public List getStateNames() {
        return states.getStateNames();
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
    
    public void setSymbolsString(String s) {
        alphabet.setSymbolsString(s);
    }

    public String getSymbolsString() {
        return alphabet.getSymbolsString();
    }

    public void addSymbol(String s) {
        alphabet.addSymbol(s);
    }

    public Set getSymbols() {
        return alphabet.getSymbols();
    }

    public void addTransitionPattern(String s1, String pattern, String s2) {
        transitions.addTransitionPattern(s1, pattern, s2);
    }

    public boolean containsTransition(String s1, String symbol, String s2) {
        return transitions.containsTransition(s1, symbol, s2);
    }

    public void addTransitions(Set<FATransition> set) {
        Iterator<FATransition> iterator = set.iterator();
        while(iterator.hasNext()) {
            transitions.addTransition(iterator.next());
        }
    }

    public void removeTransitionPattern(String s1, String pattern, String s2) {
        for(int i=0; i<pattern.length(); i++)
            transitions.removeTransition(s1, pattern.substring(i, i+1), s2);
    }

    public void clear() {
        states.clear();
        transitions.clear();
    }

    public String check() {
        if(type == MACHINE_TYPE_DFA) {
            String error = states.check();
            if(error != null)
                return error;

            error = transitions.check(alphabet.getSymbols().size(), states);
            if(error != null)
                return error;
        }
        return null;
    }

    public boolean accept(String s) {
        reset();
        stateSet = getStartStates();
        
        for(int i=0; i<s.length(); i++) {
            put(s.charAt(i));
        }
        return isAcceptedState(stateSet);
    }

    public boolean isAccepting() {
        return isAcceptedState(stateSet);
    }

    public void setStateSet(Set<String> stateSet) {
        this.stateSet = stateSet;
    }

    public Set<String> getStateSet() {
        return stateSet;
    }

    public Set getLastTransitionSet() {
        return getTransitions().getLastTransitionSet();
    }

    public Set<String> getStartStates() {
        return transitions.getEpsilonClosureStateSet(states.getStartState());
    }

    public Set<String> getNextStateSet(Set<String> stateSet, String symbol) {
        return getStateSet(stateSet, symbol);
    }

    public boolean isAcceptedState(String state) {
        return states.isAccepted(state);
    }

    public boolean isAcceptedState(Set<String> stateSet) {
        return states.isAccepted(stateSet);
    }

    // *** Conversion

    public FAMachine convertNFA2DFA() {
        Set<Set<String>> dfaStatesSet = new HashSet<Set<String>>();
        Set<FATransition> transitionsSet = new HashSet<FATransition>();

        String startState = states.getStartState();

        Set<String> startSet = new HashSet<String>();
        startSet.add(startState);
        dfaStatesSet.add(startSet);
        recursiveBuildDFA(startSet, dfaStatesSet, transitionsSet);

        return new FAMachine(dfaStatesSet, transitionsSet, startSet.toString(), states.getFinalStates());
    }
    public FAMachine minimizeDFA()
    {
    	XStream X = new XStream();
    	FAMachine that = (FAMachine) X.fromXML(X.toXML(this));
    	FAMachine minimizedDFA = new FAMachine();
    	
    	minimizedDFA.setType(MACHINE_TYPE_DFA);
    	
    	minimizedDFA.setAlphabet(that.getAlphabet());
    	minimizedDFA.setStates(that.removeUnreachableState());
    	minimizedDFA.setTransitions(minimizedDFA.clearExtraTransitions(that));
    	minimizedDFA.distinguish();
    	
    	return minimizedDFA;
    }
    
    private void distinguish()
    {	
    	int size = states.getStates().size();
    	FAStates states = this.getStates();
    	
    	String alpha = this.getAlphabet().getSymbolsString();
        		
    	// Pair Chart Initializing
    	String table[][] = new String[size - 1][size - 1];
    	
    	// Neutralization Pair Chart
    	for(int i = 0; i < size -1; i++)
    	{
    		for(int j = 0; j < size -1; j++)
        	{
    			if(j + 1 > size - i - 1)
    				break;
    			table[i][j] = "0"; 
        	}
    	}
    
    	// Declaration of States for checking
    	FAState s1, s2;
    	
    	// Checking All States for s1 State
    	for(int i = 0; i < size - 1; i++)
    	{
    		// set s1 to current State with index of i
    		s1 = (FAState) states.getStates().get(i);
    		for(int j = 0; j < size - 1 - i; j++)
        	{	
    			// set s2 to current State with index of j
    			s2 = (FAState) states.getStates().get(size - 1 - j);
    			
    			// Checking for any conflict for pre-ignorance 
    			if((s1.accepted ^ s2.accepted))
    			{
    				table[i][j] = "-1";
    				continue;
    			}
    			
    			int result = 1;
    			
    			// Does States Have Same Transitions
    			for(int k = 0; k < alpha.length(); k++)
    			{
    				// Finding Next States
    				String S1NextState = this.getTransitions().containsTransition(s1.name, String.format("%c", alpha.charAt(k)));
    				String S2NextState = this.getTransitions().containsTransition(s2.name, String.format("%c", alpha.charAt(k)));
    				
    				//
    				if(S1NextState.compareTo(S2NextState) != 0)
    				{
    					// Find Next States Index for Checking in Table and also Blacklist
    					int ns1 = states.containsIndex(S1NextState);
    					int ns2 = states.containsIndex(S2NextState);
        				
        				// to be Match to Table Limits
        				if(ns1 > size - 1 || ns1 > ns2)
        				{
        					ns1 = ns1 + ns2;
        					ns2 = ns1 - ns2;
        					ns1 = ns1 - ns2;
        				}
        				
        				if(table[ns1][size - 1 - ns2].compareTo("-1") == 0)
        				{
        					result = -1;
        					break;
        				}
        				else if(table[ns1][size - 1 - ns2].compareTo("0") == 0)
        				{
        					result = 0;
        				}
    				}
    			}
    			table[i][j] = String.format("%d", result);
        	}
    	}

    	this.findRemainig(table);
    	
    	this.merge(table);
	}

	private void merge(String[][] table)
	{		
		int size = states.getStates().size();
		FAState s1, s2;
		String alpha = this.getAlphabet().getSymbolsString();
		FAStates states = this.getStates();
		
		int count = 0;
		
		for(int i = 0; i < size - 1; i++)
		{
			for(int j = 0; j < size - 1 - i; j++)
			{
				if(table[i][j].equals("1"))
				{
					// set s1 to current State with index of i
		    		s1 = (FAState) states.getStates().get(i - count);
		    		// set s2 to current State with index of size - 1 - j
		    		s2 = (FAState) states.getStates().get(size - 1 - j - count);
		    		
		    		String name = s1.name + "=" + s2.name;
		    		this.getTransitions().renameState(s2.name, name);
		    		s2.name = name;
		    		
		    		for(int k = 0; k < alpha.length(); k++)
		    		{
		    			if(this.getTransitions().containsTransition(s1.name, String.format("%c", alpha.charAt(k)), s2.name))
		    			{
		    				this.getTransitions().addTransition(s2.name, String.format("%c", alpha.charAt(k)), s2.name);
		    			}
		    			this.getTransitions().removeTransition(s1.name, String.format("%c", alpha.charAt(k)));
		    			this.getTransitions().removeTransition(s2.name, String.format("%c", alpha.charAt(k)), s1.name);
		    		}
		    		if(s1.start)
		    		{
		    			s2.start = true;
		    		}
		    		
		    		this.getStates().removeState(s1);
		    		count++;
				}
			}
		}
		return;
	}

	private void findRemainig(String[][] table)
	{
		int size = states.getStates().size();
		FAState s1, s2;
		String alpha = this.getAlphabet().getSymbolsString();
		FAStates states = this.getStates();
		
		// Traverse DFS Table
		for(int i = 0; i < size; i++)
		{
			for(int j = 0; j < size - 1 - i; j++)
			{
				if(table[i][j].equals("0"))
				{
					// set s1 to current State with index of i
		    		s1 = (FAState) states.getStates().get(i);
		    		// set s2 to current State with index of size - 1 - j
		    		s2 = (FAState) states.getStates().get(size - 1 - j);
		    		
					LinkedList<String> list = new LinkedList<String>();
					int res = LetItGo(table, s1, s2, list, alpha);
					if(res == alpha.length())
					{
						table[i][j] = "1";
					}
					else
					{
						table[i][j] = "-1";
					}
					
				}
			}
		}
	}

	private int LetItGo(String[][] table, FAState s1, FAState s2, LinkedList<String> list, String alpha) 
	{
		int res = 0;
		for(int k = 0; k < alpha.length(); k++)
		{
			// Finding Next States
			String S1NextState = this.getTransitions().containsTransition(s1.name, String.format("%c", alpha.charAt(k)));
			String S2NextState = this.getTransitions().containsTransition(s2.name, String.format("%c", alpha.charAt(k)));
			
			int i = states.containsIndex(S1NextState);
			int j = states.containsIndex(S2NextState);

			// to be Match to Table Limits
			if(i > table.length - 1 || i > j)
			{
				i = i + j;
				j = i - j;
				i = i - j;
			}
			
			if(S1NextState.equals(S2NextState))
			{
				res += 1;
			}
			else if(table[i][states.getStates().size() - 1 - j].equals("1"))
			{
				res += 1;
			}
			else if(table[i][states.getStates().size() - 1 - j].equals("-1"))
			{
				break;
			}
			else if(list.contains(S1NextState + "-" + S2NextState + ",") || list.contains(S2NextState + "-" + S1NextState + ","))
			{
				String sname, state;
				if(list.contains(S1NextState + "-" + S2NextState + ","))
				{
					sname = S1NextState + "-" + S2NextState + ",";
				}
				else
				{
					sname = S2NextState + "-" + S1NextState + ",";
				}
				
				state = list.pop();
				int indexComma, indexS1, indexS2;
				String listS1, listS2;
				while(state.compareTo(sname) != 0)
				{
					indexComma = state.indexOf('-'); 
					listS1 = state.substring(0, indexComma);
					listS2 = state.substring(indexComma + 1, state.length() - 1);
					indexS1 = states.containsIndex(listS1);
					indexS2 = states.containsIndex(listS2);
					
					if(indexS1 > table.length - 1 || indexS1 > indexS2)
					{
						indexS1 = indexS1 + indexS2;
						indexS2 = indexS1 - indexS2;
						indexS1 = indexS1 - indexS2;
					}
					table[indexS1][states.getStates().size() - 1 - indexS2] =  "1";
					state = list.pop();
				}
				indexComma = state.indexOf('-'); 
				listS1 = state.substring(0, indexComma);
				listS2 = state.substring(indexComma + 1, state.length() - 1);
				indexS1 = states.containsIndex(listS1);
				indexS2 = states.containsIndex(listS2);
				
				if(indexS1 > table.length - 1 || indexS1 > indexS2)
				{
					indexS1 = indexS1 + indexS2;
					indexS2 = indexS1 - indexS2;
					indexS1 = indexS1 - indexS2;
				}
				table[indexS1][states.getStates().size() - 1 - indexS2] =  "1";
				
				res += 1;
			}
			else 
			{
				FAState newS1 = states.containsObj(S1NextState);
				FAState newS2 = states.containsObj(S2NextState);
				list.add(S1NextState + "-" + S2NextState + ",");
				if(LetItGo(table, newS1, newS2, list, alpha) == alpha.length())
				{
					res += 1;
				}
			}		
		}
		return res;
	}

	private FATransitions clearExtraTransitions(FAMachine that) {
		
		XStream X = new XStream();
		
		String alpha = this.getAlphabet().getSymbolsString();
		
		FATransitions newTrans = (FATransitions) X.fromXML(X.toXML(that.getTransitions()));
		
		FAStates ostates = (FAStates) X.fromXML(X.toXML(that.getStates()));
		FAStates tmp = that.getStates();
		FAStates states = this.getStates();
		
		for(int i = 0; i < states.getStates().size(); i++)
		{
			FAState wrapper = (FAState) states.getStates().get(i);
			tmp.removeState(wrapper);
		}
				
		for(int i = 0; i < tmp.getStates().size(); i++)
		{
			for(int j = 0; j < this.getAlphabet().getSymbols().size(); j++)
			{
				FAState wrapper = (FAState) tmp.getStates().get(i);
				newTrans.removeTransition(wrapper.name, String.format("%c", alpha.charAt(j)));
			}
		}
		
		that.setStates(ostates);
		
		return newTrans;
	}

	public FAStates removeUnreachableState(){
    	
    	// For Increasing Speed
    	int size = this.getStates().getStates().size();
    	int alphaSize = this.getAlphabet().getSymbols().size();
    	String alpha = this.getAlphabet().getSymbolsString();
    	Object[] stateName = this.getStates().getStateNames().toArray();
    	
    	// Queue for Detecting Reachable States 
    	Queue statesQ = new Queue();
    	
    	// a Boolean Array to Store Visited States
    	Boolean visited[] = new Boolean[size];
    	for(int i = 0; i < size; i++)
    	{
    		visited[i] = false;
    	}
    	
    	// Finally it's the Result
    	FAStates states = new FAStates();    	
    	 
    	// Add Start State to Queue
    	FAState start = this.getStates().containsObj(this.getStates().getStartState());
    	states.addState(start);
    	statesQ.enqueue(start.name);
    	 
    	int i = 0;
    	while(!statesQ.isEmpty())
    	{
    		visited[i] = true;
    		String name = "";
    		try { name = (String) statesQ.dequeue();} catch (InterruptedException e) {}
    		
    		int index;
    		for(index = 0 ; index < stateName.length; index++)
    		{
    			if(name.compareTo((String) stateName[index]) == 0)
    			{
    				break;
    			}
    		}
    		FAState wrapper = (FAState)this.getStates().getStates().get(index);
    		if(!states.contains(wrapper.name))
    		{
    			states.addState(wrapper);
    		}

            for(int j = 0; j < alphaSize; j++)
            {
            	String s2 = this.getTransitions().containsTransition(wrapper.name, String.format("%c", alpha.charAt(j)));
            	if(visited[this.getStates().containsIndex(s2)] == false)
            	{
            		visited[this.getStates().containsIndex(s2)] = true;
            		if(!states.contains(s2))
            		{
            			states.addState(this.getStates().containsObj((s2)));
            		}
            		statesQ.enqueue(s2);
            	}
            }
            i++;
    	}
    	
    	return states;    	
    }

    public void recursiveBuildDFA(Set<String> statesSet, Set<Set<String>> dfaStatesSet, Set<FATransition> transitionsSet) {
        Iterator iterator = alphabet.getSymbols().iterator();
        while(iterator.hasNext()) {
            String symbol = (String)iterator.next();
            Set<String> newSet = getStateSet(statesSet, symbol);

            if(newSet.size()>0)
                transitionsSet.add(new FATransition(statesSet.toString(), symbol, newSet.toString()));

            if(!dfaStatesSet.contains(newSet) && newSet.size()>0) {
                dfaStatesSet.add(newSet);
                recursiveBuildDFA(newSet, dfaStatesSet, transitionsSet);
            }
        }
    }

    public Set<String> getStateSet(Set<String> statesSet, String symbol) {
        Set<String> newStateSet = new HashSet<String>();
        Iterator<String> iterator = statesSet.iterator();
        while(iterator.hasNext()) {
            String state = iterator.next();
            Set set = transitions.getClosureStateSet(state, symbol);
            if(set.size()>0)
                newStateSet.addAll(set);
        }
        return newStateSet;
    }

    // *** Debug methods

    public void debugReset(String s) {
        reset();
        debugString = s;
    }

    public boolean debugStepForward() {
        if(debugString.length() == 0)
            return false;

        if(stateSet.isEmpty())
            stateSet = getStartStates();

        transitions.getLastTransitionSet().clear();

        put(debugString.charAt(0));

        debugLastSymbol = debugString.substring(0, 1);
        debugString = debugString.substring(1);

        if(stateSet.isEmpty())
            return false;
        else
            return debugString.length() > 0;
    }

    public String debugLastSymbol() {
        return debugLastSymbol;
    }

    public String debugString() {
        return debugString;
    }
    
    public String toString() {
        String s = "Description of the machine:\n";
        s += states;
        s += transitions;
        return s;
    }

    // *** Processing methods

    public void reset() {
        stateSet.clear();
        if(transitions.getLastTransitionSet() != (null))
        	transitions.getLastTransitionSet().clear();
        debugLastSymbol = "";
    }

    public void put(char c) {
        stateSet = getNextStateSet(stateSet, String.valueOf(c));
    }

}
