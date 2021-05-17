package org.processmining.logfiltering.legacy.plugins.logfiltering.ui;

import javax.swing.JPanel;

import org.processmining.logfiltering.legacy.plugins.logfiltering.listener.LogFilteringSettingsListener;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 June 2010
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */

@SuppressWarnings("serial")
public abstract class myStep extends JPanel {
	public abstract boolean precondition();
	public abstract void readSettings();
	public abstract void setListener(LogFilteringSettingsListener listener);
}
