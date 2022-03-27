package org.processmining.logfiltering.legacy.plugins.logfiltering.ui;

import org.processmining.logfiltering.legacy.plugins.logfiltering.listener.LogFilteringSettingsListener;

import javax.swing.*;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @version 1.0
 * @date 08 July 2010
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * Architecture of Information Systems Group (AIS)
 * Department of Mathematics and Computer Science
 * University of Technology, Eindhoven, The Netherlands
 * @since 01 June 2010
 */

@SuppressWarnings("serial")
public abstract class myStep extends JPanel {
    public abstract boolean precondition();

    public abstract void readSettings();

    public abstract void setListener(LogFilteringSettingsListener listener);
}
