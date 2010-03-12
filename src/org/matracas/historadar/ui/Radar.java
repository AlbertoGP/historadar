///////////////////////////////////////////////////////////////////////////
//
//   Copyright 2010 Alberto González Palomo
//   Author: Alberto González Palomo - http://matracas.org/
//
//   This file is part of HistoRadar, the History Radar.
//
//   HistoRadar is free software; you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation; either version 3 of the License, or
//   (at your option) any later version.
//
//   HistoRadar is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.
//
//   You should have received a copy of the GNU General Public License
//   along with HistoRadar; if not, see <http://www.gnu.org/licenses/>.
//
/////////////////////////////////////////////////////////////////////////////

package org.matracas.historadar.ui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.event.ActionListener;
import javax.swing.event.MouseInputListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Vector;

public class Radar extends JPanel
    implements MouseInputListener, MouseWheelListener
{
    protected Vector<ActionListener> actionListeners;
    protected String actionCommand;
    protected HeatMap heatMap;
    
    protected TimeScale timeScale;
    protected JLabel entityLabel;
    protected java.text.SimpleDateFormat dateFormat;
    
    public Radar()
    {
        actionListeners = new Vector<ActionListener>();
        actionCommand = "radar";
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        setLayout(new BorderLayout());
        
        heatMap = new HeatMap();
        add(heatMap, BorderLayout.CENTER);
        
        timeScale = new TimeScale();
        add(timeScale, BorderLayout.WEST);
        entityLabel = new JLabel("TODO: show current entity");
        add(entityLabel, BorderLayout.NORTH);
        
        dateFormat = (java.text.SimpleDateFormat) java.text.SimpleDateFormat.getDateInstance();
        dateFormat.applyPattern("yyyy-MM-dd HH:mm");
        dateFormat.setLenient(true);
    }
    
    public void setDataSize(int width, int height)
    {
        heatMap.setDataSize(width, height);
        validate();
    }
    
    public void setRow(String date, int row, double[] values)
    {
        try {
            if (date != null) timeScale.add(dateFormat.parse(date));
            else              timeScale.add(new java.util.Date());
        }
        catch (java.text.ParseException e) {
            System.err.println("Error when parsing date '" + date + "'\n" + e);
            timeScale.add(new java.util.Date());
        }
        heatMap.setRow(row, values);
        validate();
    }
    
    public void addActionListener(ActionListener listener)
    {
        actionListeners.add(listener);
    }
    
    public void setActionCommand(String command)
    {
        actionCommand = command;
    }
    
    // MouseInputListener = MouseListener + MouseMotionLister
    public void mouseClicked(MouseEvent e)
    {
        System.err.println("clicked radar at " + e.getX() + ", " + e.getY());
    }
    
    // MouseListener
    public void mouseEntered(MouseEvent e) {}
    
    public void mouseExited(MouseEvent e) {}
    
    public void mousePressed(MouseEvent e) {}
    
    public void mouseReleased(MouseEvent e) {}
    
    // MouseMotionListener
    public void mouseDragged(MouseEvent e) {}
    
    public void mouseMoved(MouseEvent e) {}
    
    // MouseWheelListener
    public void mouseWheelMoved(MouseWheelEvent e) {}
    
}
