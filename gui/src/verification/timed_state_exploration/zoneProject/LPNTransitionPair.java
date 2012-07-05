package verification.timed_state_exploration.zoneProject;

/**
 * The LPNTransitionPair gives a packaged pairing of a transition index together
 * with the index of the LPN that the transition is in. The class simply has the two
 * member variables for the transition index and the LPN index with getters and
 * setters, so it is possible to use it as a general pairing of two integers. However,
 * it is recommended that it is only used where the semantics apply. That is, it
 * is recommended that it is only used in the context of pairing a transition index
 * with an LPN index.
 * 
 * If the index of a Transition is t and the index of the LPN that the Transition
 * occurs in in l, the LPNTransitionPair is thought of as (l,t). In particular, the
 * natural ordering on the LPNTransitionPair is the dictionary ordering on this
 * pairs of this form.
 * 
 * @author Andrew N. Fisher
 *
 */
public class LPNTransitionPair implements Comparable<LPNTransitionPair>{
	
	/*
	 * Abstraction Function : Given a Transition with index t in an LPN that has
	 * 		index l, the LPNTransitionPair represents the pairing (t, l). The
	 * 		index of the transition (t) is stored in _transitionIndex and the index
	 * 		of the LPN (l) is stored in _lpnIndex.
	 */
	
	/*
	 * Representation Invariant : none.
	 */
	
	// Value for indicating a single LPN is in use.
	public static final int SINGLE_LPN = -2;
	
	// Value for indicating the zero timer.
	public static final int ZERO_TIMER = -1;
	
	private int _lpnIndex;
	private int _transitionIndex;
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _lpnIndex;
		result = prime * result + _transitionIndex;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof LPNTransitionPair))
			return false;
		LPNTransitionPair other = (LPNTransitionPair) obj;
		if (_lpnIndex != other._lpnIndex)
			return false;
		if (_transitionIndex != other._transitionIndex)
			return false;
		return true;
	}	
	
	/**
	 * Creates a LPNTransitionPair with both indicies set to 0.
	 */
	public LPNTransitionPair(){
		_lpnIndex = 0;
		_transitionIndex = 0;
	}
	
	/**
	 * Creates a pairing of an LPN index and is associated transition index.
	 * @param lpnIndex
	 * 			The LPN that the transition is in.
	 * @param transitionIndex
	 * 			The index of the transition.
	 */
	public LPNTransitionPair(int lpnIndex, int transitionIndex){
		this._lpnIndex = lpnIndex;
		this._transitionIndex = transitionIndex;
	}
	
	/**
	 * Gets the index of the LPN that the transition is in.
	 * @return
	 * 		The index of the LPN that the transition is in.
	 */
	public int get_lpnIndex() {
		return _lpnIndex;
	}
	
	/**
	 * Sets the index of the LPN that the transition is in.
	 * @param lpnIndex
	 * 			The index of the Lpn that the transition is in.
	 */
	public void set_lpnIndex(int lpnIndex) {
		this._lpnIndex = lpnIndex;
	}
	
	/**
	 * Get the index of the transition.
	 * @return
	 * 		The index of the transition.
	 */
	public int get_transitionIndex() {
		return _transitionIndex;
	}
	
	/**
	 * Sets the index of the transition.
	 * @param transitionIndex
	 * 			The index of the transition.
	 */
	public void set_transitionIndex(int transitionIndex) {
		this._transitionIndex = transitionIndex;
	}
	
	public String toString(){	
		String result = "(LPN Index, Transition Index) = (";
		
		result += _lpnIndex + ", " + _transitionIndex + ")";
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public LPNTransitionPair clone(){
		
		LPNTransitionPair newPair = new LPNTransitionPair();
		
		newPair._lpnIndex = this._lpnIndex;
		newPair._transitionIndex = this._transitionIndex;
		
		return newPair;
	}

	/**
	 * Determines whether this LPNTransitionPair is less than the otherPair
	 * LPNTransitionPair. The ordering is the dictionary ordering on pairs
	 * (l,t) where t is the index of the Transition and l is the index of the
	 * LPN that the Transition is in.
	 * @param otherValue
	 * 		The value to determine whether this LPNTransitionPair is less than.
	 * @return
	 * 		A negative integer, zero, or positive integer depending on if this
	 * 		LPNTransitionPair is less than, equal, or greater than the otherPair
	 * 		LPNTransitionPair.
	 */
	public int compareTo(LPNTransitionPair otherPair) {

		/*
		 * If it is known that the terms will not be too large, than a clever
		 * implementation would be
		 * 		return dlpnIndex * constant + dtransitionIndex
		 * where dlpnIndex is the difference in the _lpnIndex member variables and
		 * dtransitionIndex is the difference in the _transitionIndex member variables.
		 * 
		 * For this to work the constant has to be chosen such that 
		 * 		|dtransitionIndex| < constant
		 * so that the return value has the proper sign when dlpnIndex and
		 * dtransitionIndex have opposite signs. Since the number of transition
		 * is not bounded, this is not practical.
		 * 
		 * The roles of dlpnIndex and dtransitionIndex could be reversed (that is
		 * we could do the dictionary ordering of the reversed pair). But the problem
		 * still remains and this ordering would be much less intuitive.
		 */
		
		int dlpnIndex = this._lpnIndex - otherPair._lpnIndex;
		
		if(dlpnIndex != 0){
			return dlpnIndex;
		}
		else{
			// Reaching here means that the first values are equal.
			// So the sign is determined by the second pair.
			return this._transitionIndex - otherPair._transitionIndex;
		}
	}
}
