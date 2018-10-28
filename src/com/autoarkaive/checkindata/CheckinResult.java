package com.autoarkaive.checkindata;

import java.io.Serializable;

/**
 * Result of a checkin, sent from the phone to the server
 * @author jamie
 *
 */
public interface CheckinResult extends Serializable {

	/**
	 * True iff the checkin succeeded.
	 * @return
	 */
	boolean succeeded();
	
}
