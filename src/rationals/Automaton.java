package rationals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rationals.transformations.TransformationsToolBox;

/**
 * A class defining Automaton objects
 * 
 * This class defines the notion of automaton. Following notations are used to
 * describe this class.
 * <p>
 * An automaton is a 5-uple <em>A = (X , Q , I , T , D)</em> where
 * <ul>
 * <li><em>X</em> is a finite set of labels named alphabet ,
 * <li><em>Q</em> is a finite set of states,
 * <li><em>I</em>, included in <em>Q</em>, is the set of initial states,
 * <li><em>T</em>, included in <em>Q</em>, is the set of terminal states
 * <li>and <em>D</em> is the set of transitions, which is included in
 * <em>Q times X times Q</em> (transitions are triples <em>(q , l , q')</em>
 * where <em>q, q'</em> are states and <em>l</em> a label).
 * </ul>
 * The empty word, usually denoted by <em>epsilon</em> will be denoted here by
 * the symbol <em>@</em>.
 * <p>
 * In this implementation of automaton, any object may be a label, states are
 * instance of class <tt>State</tt> and transitions are intances of class
 * <tt>Transition</tt>. Only automata should create instances of states
 * through <tt>Automaton</tt> method <tt>newState</tt>.
 * @author yroos@lifl.fr
 * @author bailly@lifl.fr
 * @version $Id: Automaton.java,v 1.1 2013/03/08 17:53:37 gesteban Exp $
 * @see Transition State
 */
public class Automaton implements Acceptor, StateMachine,
		Rational, Cloneable {
	/* the identification of this automaton */
	private Object id;

	/**
	 * @return Returns the id.
	 */
	public Object getId() {
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(Object id) {
		this.id = id;
	}

	// The set of all objects which are labels of
	// transitions of this automaton.
	protected Set alphabet;

	// The set of all states of this automaton.
	private Set states;

	//the set of initial states
	private Set initials;

	//the set of terminale states
	private Set terminals;

	// Allows acces to transitions of this automaton
	// starting from a given state and labelled by
	// a given object. The keys of this map are instances
	// of class Key and
	// values are sets of transitions.
	private Map transitions;

	// Allows acces to transitions of this automaton
	// arriving to a given state and labelled by
	// a given object. The keys of this map are instances
	// of class Key and
	// values are sets of transitions.
	private Map reverse;

	//bonte
	private StateFactory stateFactory = new DefaultStateFactory(this);

	/**
	 * @return
	 */
	public StateFactory getStateFactory() {
		return this.stateFactory;
	}

	/**
	 * @param factory
	 */
	public void setStateFactory(StateFactory factory) {
		this.stateFactory = factory;
		factory.setAutomaton(this);
	}

	/**
	 * Returns an automaton which recognizes the regular language associated
	 * with the regular expression <em>@</em>, where <em>@</em> denotes the
	 * empty word.
	 * @return an automaton which recognizes <em>@</em>
	 */
	public static Automaton epsilonAutomaton() {
		Automaton v = new Automaton();
		v.addState(true, true);
		return v;
	}

	/**
	 * Returns an automaton which recognizes the regular language associated
	 * with the regular expression <em>l</em>, where <em>l</em> is a given
	 * label.
	 * 
	 * @param label
	 *            any object that will be used as a label.
	 * @return an automaton which recognizes <em>label</em>
	 */
	public static Automaton labelAutomaton(Object label) {
		Automaton v = new Automaton();
		State start = v.addState(true, false);
		State end = v.addState(false, true);
		try {
			v.addTransition(new Transition(start, label, end));
		} catch (NoSuchStateException x) {
		}
		return v;
	}

	/**
	 * Returns an automaton which recognizes the regular language associated
	 * with the regular expression <em>u</em>, where <em>u</em> is a given
	 * word.
	 * 
	 * @param word a List of Object interpreted as a word
	 * @return an automaton which recognizes <em>label</em>
	 */
	public static Automaton labelAutomaton(List word) {
		Automaton v = new Automaton();
		State start = null;
		if (word.isEmpty()) {
			v.addState(true, true);
			return v;
		} else
			start = v.addState(true, false);
		State end = null;
		try {
			for (Iterator i = word.iterator(); i.hasNext();) {
				Object o = i.next();
				end = v.addState(false, !i.hasNext());
				v.addTransition(new Transition(start, o, end));
				start = end;
			}
		} catch (NoSuchStateException x) {
		}
		return v;
	}

	/**
	 * Creates a new empty automaton which contains no state and no transition.
	 * An empty automaton recognizes the empty language.
	 */
	public Automaton() {
		this(null);
	}

	/**
	 * Create a new empty automaton with given state factory.
	 * 
	 * @param sf
	 *            the StateFactory object to use for creating new states. May be
	 *            null.
	 */
  public Automaton(StateFactory sf) {
    this.stateFactory = sf == null ? new DefaultStateFactory(this) : sf;
    alphabet = new HashSet();
    states = stateFactory.stateSet();
    initials = stateFactory.stateSet();
    terminals = stateFactory.stateSet();
    transitions = new HashMap();
    reverse = new HashMap();
  }
  
	/**
	 * Returns a new instance of state which will be initial and terminal or not
	 * depending of parameters.
	 * 
	 * @param initial
	 *            if true, the new state will be initial; otherwise this state
	 *            will be non initial.
	 * @param terminal
	 *            if true, the new state will be terminal; otherwise this state
	 *            will be non terminal.
	 * @return a new state, associated with this automaton. This new state
	 *         should be used only with this automaton in order to create a new
	 *         transition for this automaton.
	 * @see Transition
	 */
	public State addState(boolean initial, boolean terminal) {

		State state = stateFactory.create(initial, terminal);
		if (initial)
			initials.add(state);
		if (terminal)
			terminals.add(state);
		states.add(state);
		return state;
	}

	/**
	 * Returns the alphabet <em>X</em> associated with this automaton.
	 * 
	 * @return the alphabet <em>X</em> associated with this automaton.
	 */
	public Set alphabet() {
		return alphabet;
	}

	/**
	 * Returns the set of states <em>Q</em> associated with this automaton.
	 * 
	 * @return the set of states <em>Q</em> associated with this automaton.
	 *         Objects which are contained in this set are instances of class
	 *         <tt>State</tt>.
	 * @see State
	 */
	public Set states() {
		return states;
	}

	/**
	 * Returns the set of initial states <em>I</em> associated with this
	 * automaton.
	 * 
	 * @return the set of initial states <em>I</em> associated with this
	 *         automaton. Objects which are contained in this set are instances
	 *         of class <tt>State</tt>.
	 * @see State
	 */
	public Set initials() {
		return initials;
	}

	/**
	 * Returns the set of terminal states <em>T</em> associated with this
	 * automaton.
	 * 
	 * @return set of terminal states <em>T</em> associated with this
	 *         automaton. Objects which are contained in this set are instances
	 *         of class <tt>State</tt>.
	 * @see State
	 */
	public Set terminals() {
		return terminals;
	}

	// Computes and return the set of all accessible states, starting
	// from a given set of states and using transitions
	// contained in a given Map
	protected Set access(Set start, Map map) {
		Set current = start;
		Set old;
		do {
			old = current;
			current = stateFactory.stateSet();
			Iterator i = old.iterator();
			while (i.hasNext()) {
				State e = (State) i.next();
				current.add(e);
				Iterator j = alphabet.iterator();
				while (j.hasNext()) {
					Iterator k = find(map, e, j.next()).iterator();
					while (k.hasNext()) {
						current.add(((Transition) k.next()).end());
					}
				}
			}
		} while (current.size() != old.size());
		return current;
	}

	/**
	 * Returns the set of all accessible states in this automaton.
	 * 
	 * @return the set of all accessible states in this automaton. A state
	 *         <em>s</em> is accessible if there exists a path from an initial
	 *         state to <em>s</em>. Objects which are contained in this set
	 *         are instances of class <tt>State</tt>.
	 * @see State
	 */
	public Set accessibleStates() {
		return access(initials, transitions);
	}

	/**
	 * Returns the set of states that can be accessed in this automaton starting
	 * from given set of states
	 * 
	 * @param states
	 *            a non null set of starting states
	 * @return a - possibly empty - set of accessible states
	 */
	public Set accessibleStates(Set states) {
		return access(states, transitions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rationals.Rational#accessibleStates(rationals.State)
	 */
	public Set accessibleStates(State state) {
		Set s = stateFactory.stateSet();
		s.add(state);
		return access(s, transitions);
	}

	/**
	 * Returns the set of co-accesible states for a given set of states, that is
	 * the set of states from this automaton from which there exists a path to a
	 * state in <code>states</code>.
	 * 
	 * @param states
	 *            a non null set of ending states
	 * @return a - possibly empty - set of coaccessible states
	 */
	public Set coAccessibleStates(Set states) {
		return access(states, reverse);
	}

	/**
	 * Returns the set of all co-accessible states in this automaton.
	 * 
	 * @return the set of all co-accessible states in this automaton. A state
	 *         <em>s</em> is co-accessible if there exists a path from this
	 *         state <em>s</em> to a terminal state. Objects which are
	 *         contained in this set are instances of class <tt>State</tt>.
	 * @see State
	 */
	public Set coAccessibleStates() {
		return access(terminals, reverse);
	}

	/**
	 * Returns the set of all states which are co-accessible and accessible in
	 * this automaton.
	 * 
	 * @return the set of all states which are co-accessible and accessible in
	 *         this automaton. A state <em>s</em> is accessible if there
	 *         exists a path from an initial state to <em>s</em>. A state
	 *         <em>s</em> is co-accessible if there exists a path from this
	 *         state <em>s</em> to a terminal state. Objects which are
	 *         contained in this set are instances of class <tt>State</tt>.
	 * @see State
	 */
	public Set accessibleAndCoAccessibleStates() {
		Set ac = accessibleStates();
		ac.retainAll(coAccessibleStates());
		return ac;
	}

	// Computes and return the set of all transitions, starting
	// from a given state and labelled by a given label
	// contained in a given Map
	protected Set find(Map m, State e, Object l) {
		Key n = new Key(e, l);
		if (!m.containsKey(n))
			return new HashSet();
		return (Set) m.get(n);
	}

	// add a given transition in a given Map
	protected void add(Map m, Transition t) {
		Key n = new Key(t.start(), t.label());
		Set s;
		if (!m.containsKey(n)) {
			s = new HashSet();
			m.put(n, s);
		} else
			s = (Set) m.get(n);
		s.add(t);
	}

	/**
	 * Returns the set of all transitions of this automaton
	 * 
	 * @return the set of all transitions of this automaton Objects which are
	 *         contained in this set are instances of class <tt>Transition</tt>.
	 * @see Transition
	 */
	public Set delta() {
		Set s = new HashSet();
		Iterator i = transitions.values().iterator();
		while (i.hasNext()) {
			s.addAll((Set) i.next());
		}
		return s;
	}

	/**
	 * Returns the set of all transitions of this automaton starting from a
	 * given state and labelled b a given label.
	 * 
	 * @param state
	 *            a state of this automaton.
	 * @param label
	 *            a label used in this automaton.
	 * @return the set of all transitions of this automaton starting from state
	 *         <tt>state</tt> and labelled by <tt>label</tt>. Objects which
	 *         are contained in this set are instances of class
	 *         <tt>Transition</tt>.
	 * @see Transition
	 */
	public Set delta(State state, Object label) {
		return find(transitions, state, label);
	}

	/**
	 * Returns the set of all transitions from state <code>from</code> to
	 * state <code>to</code>.
	 * 
	 * @param from
	 *            starting state
	 * @param to
	 *            ending state
	 * @return a Set of Transition objects
	 */
	public Set deltaFrom(State from, State to) {
		Set t = delta(from);
		for (Iterator i = t.iterator(); i.hasNext();) {
			Transition tr = (Transition) i.next();
			if (!to.equals(tr.end()))
				i.remove();
		}
		return t;
	}

	/**
	 * Return all transitions from a State
	 * 
	 * @param state
	 *            start state
	 * @return a new Set of transitions (maybe empty)
	 */
	public Set delta(State state) {
		Set s = new HashSet();
		Iterator alphit = alphabet().iterator();
		while (alphit.hasNext()) {
			s.addAll(delta(state, alphit.next()));
		}
		return s;
	}

	/**
	 * Returns all transitions from a given set of states.
	 * 
	 * @param s
	 *            a Set of State objects
	 * @return a Set of Transition objects
	 */
	public Set delta(Set s) {
		Set ds = new HashSet();
		Iterator i = s.iterator();
		while (i.hasNext()) {
			ds.addAll(delta((State) i.next()));
		}
		return ds;
	}

	/**
	 * Return a mapping from couples (q,q') of states to all (q,l,q')
	 * transitions from q to q'
	 * 
	 * @return a Map
	 */
	public Map couples() {
		// loop on transition map keys
		Iterator it = transitions.entrySet().iterator();
		Map ret = new HashMap();
		while (it.hasNext()) {
			Map.Entry e = (Map.Entry) it.next();
			// get start and end state
			State st = ((Key) e.getKey()).s;
			Iterator trans = ((Set) e.getValue()).iterator();
			while (trans.hasNext()) {
				Transition tr = (Transition) trans.next();
				State nd = tr.end();
				Couple cpl = new Couple(st, nd);
				Set s = (Set) ret.get(cpl);
				if (s == null)
					s = new HashSet();
				s.add(tr);
				ret.put(cpl, s);
			}
		}
		return ret;
	}

	/**
	 * Returns the set of all transitions of the reverse of this automaton
	 * 
	 * @return the set of all transitions of the reverse of this automaton. A
	 *         reverse of an automaton <em>A = (X , Q , I , T , D)</em> is the
	 *         automaton <em>A' = (X , Q , T , I , D')</em> where <em>D'</em>
	 *         is the set <em>{ (q , l , q') | (q' , l , q) in D}</em>.
	 *         Objects which are contained in this set are instances of class
	 *         <tt>Transition</tt>.
	 * @see Transition
	 */
	public Set deltaMinusOne(State state, Object label) {
		return find(reverse, state, label);
	}

	/**
	 * Adds a new transition in this automaton if it is a new transition for
	 * this automaton. The parameter is considered as a new transition if there
	 * is no transition in this automaton which is equal to the parameter in the
	 * sense of method <tt>equals</tt> of class <tt>Transition</tt>.
	 * 
	 * @param transition
	 *            the transition to add.
	 * @throws NoSuchStateException
	 *             if <tt>transition</tt> is <tt>null</<tt>
	 * or if <tt>transition</tt> = <em>(q , l , q')</em> and <em>q</em> or
	 * <em>q'</em> does not belong to <em>Q</em> the set of the states
	 * of this automaton.
	 */
	public void addTransition(Transition transition)
			throws NoSuchStateException {
		if (!states.contains(transition.start())
				|| !states.contains(transition.end()))
			throw new NoSuchStateException();
		if (!alphabet.contains(transition.label())) {
			alphabet.add(transition.label());
		}
		add(transitions, transition);
		add(reverse, new Transition(transition.end(), transition.label(),
				transition.start()));
	}

	/**
	 * the project method keeps from the Automaton only the transitions labelled
	 * with the letters contained in the set alph, effectively computing a
	 * projection on this alphabet.
	 * 
	 * @param alph
	 *            the alphabet to project on
	 */
	public void projectOn(Set alph) {
		// remove unwanted transitions from ret
		Iterator trans = transitions.entrySet().iterator();
		Set newtrans = new HashSet();
		while (trans.hasNext()) {
			Map.Entry entry = (Map.Entry) trans.next();
			Key k = (Key) entry.getKey();
			Iterator tit = ((Set) entry.getValue()).iterator();
			while (tit.hasNext()) {
				Transition tr = (Transition) tit.next();
				if (!alph.contains(k.l)) {
					// create epsilon transition
					newtrans.add(new Transition(k.s, null, tr.end()));
					// remove transtion
					tit.remove();
				}
			}
		}
		// add newly created transitions
		if (!newtrans.isEmpty()) {
			trans = newtrans.iterator();
			while (trans.hasNext()) {
				Transition tr = (Transition) trans.next();
				add(transitions, tr);
				add(reverse, new Transition(tr.end(), tr.label(), tr.start()));
			}
		}
		// remove alphabet
		alphabet.retainAll(alph);
	}

	/**
	 * returns a textual representation of this automaton.
	 * 
	 * @return a textual representation of this automaton based on the converter
	 *         <tt>toAscii</tt>.
	 * @see rationals.converters.toAscii
	 */
	public String toString() {
		return new rationals.converters.toAscii().toString(this);
	}

	/**
	 * returns a copy of this automaton.
	 * 
	 * @return a copy of this automaton with new instances of states and
	 *         transitions.
	 */
	public Object clone() {
		Automaton b;
		b = new Automaton();
		Map map = new HashMap();
		Iterator i = states().iterator();
		while (i.hasNext()) {
			State e = (State) i.next();
			map.put(e, b.addState(e.isInitial(), e.isTerminal()));
		}
		i = delta().iterator();
		while (i.hasNext()) {
			Transition t = (Transition) i.next();
			try {
				b.addTransition(new Transition((State) map.get(t.start()), t
						.label(), (State) map.get(t.end())));
			} catch (NoSuchStateException x) {
			}
		}
		return b;
	}

	private class Key {
		private State s;

		private Object l;

		protected Key(State s, Object l) {
			this.s = s;
			this.l = l;
		}

		public boolean equals(Object o) {
			if (o == null)
				return false;
			try {
				Key t = (Key) o;
				boolean ret = (l == null ? t.l == null : l.equals(t.l))
						&& (s == null ? t.s == null : s.equals(t.s));
				return ret;
			} catch (ClassCastException x) {
				return false;
			}
		}

		public int hashCode() {
			int x, y;
			if (s == null)
				x = 0;
			else
				x = s.hashCode();
			if (l == null)
				y = 0;
			else
				y = l.hashCode();
			return y << 16 | x;
			//            return new java.awt.Point(x, y).hashCode();
		}
	}

	/**
	 * Returns true if this automaton accepts given word -- ie. sequence of
	 * letters. Note that this method accepts words with letters not in this
	 * automaton's alphabet, effectively recognizing all words from any alphabet
	 * projected to this alphabet.
	 * <p>
	 * If you need standard recognition, use
	 * 
	 * @see{accept(java.util.List)}.
	 * @param word
	 * @return
	 */
	public boolean prefixProjection(List word) {
		Set s = stepsProject(word);
		return !s.isEmpty();
	}

	/**
	 * Return the set of steps this automaton will be in after reading word.
	 * Note this method skips letters not in alphabet instead of rejecting them.
	 * 
	 * @param l
	 * @return
	 */
	public Set stepsProject(List word) {
		Set s = initials();
		Iterator it = word.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (!alphabet.contains(o))
				continue;
			s = step(s, o);
			if (s.isEmpty())
				return s;
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rationals.Acceptor#accept(java.util.List)
	 */
	public boolean accept(List word) {
		Set s = TransformationsToolBox.epsilonClosure(steps(word),this);
		s.retainAll(terminals());
		return !s.isEmpty();
	}

	/**
	 * Return true if this automaton can accept the given word starting from
	 * given set. <em>Note</em> The ending state(s) need not be terminal for
	 * this method to return true.
	 * 
	 * @param state
	 *            a starting state
	 * @param word
	 *            a List of objects in this automaton's alphabet
	 * @return true if there exists a path labelled by word from s to at least
	 *         one other state in this automaton.
	 */
	public boolean accept(State state, List word) {
		Set s = stateFactory.stateSet();
		s.add(state);
		return !steps(s, word).isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rationals.Acceptor#steps(java.util.List)
	 */
	public Set steps(List word) {
		Set s = TransformationsToolBox.epsilonClosure(initials(), this);
		return steps(s, word);
	}

	/**
	 * Return the set of states this automaton will be in after reading the word
	 * from start states s.
	 * 
	 * @param s
	 *            the set of starting states
	 * @param word
	 *            the word to read.
	 * @return the set of reached states.
	 */
	public Set steps(Set s, List word) {
		Iterator it = word.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			s = step(s, o);
			if (s.isEmpty())
				return s;
		}
		return s;
	}

	/**
	 * Return the set of states this automaton will be in after reading the word
	 * from singler start state s.
	 * 
	 * @param st
	 *            the starting state
	 * @param word
	 *            the word to read.
	 * @return the set of reached states.
	 */
	public Set steps(State st, List word) {
		Set s = stateFactory.stateSet();
		s.add(st);
		Iterator it = word.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			s = step(s, o);
			if (s.isEmpty())
				return s;
		}
		return s;
	}

	/**
	 * Return the list of set of states this automaton will be in after reading
	 * word from start state. Is start state is null, assume reading from
	 * initials().
	 * 
	 * @param word
	 * @param start
	 */
	public List traceStates(List word, State start) {
		List ret = new ArrayList();
		Set s = null;
		if (start != null) {
			s = stateFactory.stateSet();
			s.add(start);
		} else {
			s = initials();
		}
		Iterator it = word.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (!alphabet.contains(o))
				continue;
			s = step(s, o);
			ret.add(s);
			if (s.isEmpty())
				return null;
		}
		return ret;
	}

	/**
	 * Returns the size of the longest word recognized by this automaton where
	 * letters not belonging to its alphabet are ignored.
	 * 
	 * 
	 * @param word
	 * @return
	 */
	public int longestPrefixWithProjection(List word) {
		int lret = 0;
		Set s = initials();
		Iterator it = word.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if ((o == null) || !alphabet.contains(o)) {
				lret++;
				continue;
			}
			s = step(s, o);
			if (s.isEmpty())
				break;
			lret++;
		}
		return lret;
	}


	/**
	 * Return the set of states accessible in one transition from given set of
	 * states s and letter o.
	 * 
	 * @param s
	 * @param o
	 * @return
	 */
	public Set step(Set s, Object o) {
		Set ns = stateFactory.stateSet();
		Set ec = TransformationsToolBox.epsilonClosure(s, this);
		Iterator it = ec.iterator();
		while (it.hasNext()) {
			State st = (State) it.next();
			Iterator it2 = delta(st).iterator();
			while (it2.hasNext()) {
				Transition tr = (Transition) it2.next();
				if (tr.label() != null && tr.label().equals(o))
					ns.add(tr.end());
			}
		}
		return ns;
	}


	/**
	 * @param tr
	 * @param msg
	 */
	public void updateTransitionWith(Transition tr, Object msg) {
		Object lbl = tr.label();
		alphabet.remove(lbl);
		alphabet.add(msg);
		/* update transition map */
		Key k = new Key(tr.start(), lbl);
		Set s = (Set) transitions.remove(k);
		if (s != null)
			transitions.put(new Key(tr.start(), msg), s);
		/* update reverse map */
		k = new Key(tr.end(), lbl);
		s = (Set) reverse.remove(k);
		if (s != null)
			reverse.put(new Key(tr.end(), msg), s);
		tr.setLabel(msg);
	}

	/**
	 * @param st
	 * @return
	 */
	public Set deltaMinusOne(State st) {
		Set s = new HashSet();
		Iterator alphit = alphabet().iterator();
		while (alphit.hasNext()) {
			s.addAll(deltaMinusOne(st, alphit.next()));
		}
		return s;
	}

	/**
	 * Enumerate all prefix of words of length lower or equal than i 
	 * in this automaton.
	 * 
	 * @param i maximal length of words.
	 * @return a Set of List of Object
	 */
	public Set enumerate(int ln) {
		Set ret = new HashSet();
		class EnumState {
			/**
			 * @param s
			 * @param list
			 */
			public EnumState(State s, List list) {
				st = s;
				word = new ArrayList(list);
			}

			State st;

			List word;
		}
		;
		LinkedList ll = new LinkedList();
		List cur = new ArrayList();
		for (Iterator i = initials.iterator(); i.hasNext();) {
			State s = (State) i.next();
			if (s.isTerminal())
				ret.add(new ArrayList());
			ll.add(new EnumState(s, cur));
		}

		do {
			EnumState st = (EnumState) ll.removeFirst();
			Set trs = delta(st.st);
			List word = st.word;
			for (Iterator k = trs.iterator(); k.hasNext();) {
				Transition tr = (Transition) k.next();
				word.add(tr.label());
				if (word.size() <= ln) {
					EnumState en = new EnumState(tr.end(), word);
					ll.add(en);
					ret.add(en.word);
				}
				word.remove(word.size() - 1);
			}
		} while (!ll.isEmpty());
		return ret;
	}

}
