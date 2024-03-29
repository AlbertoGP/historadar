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
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;

public class Radar extends JPanel
    implements ActionListener
{
    protected Vector<ActionListener> actionListeners;
    protected String actionCommand;
    
    protected boolean fuzzy;
    protected HeatMap heatMap;
    protected TimeScale timeScale;
    protected JLabel entityLabel;
    
    public Radar()
    {
        actionListeners = new Vector<ActionListener>();
        actionCommand = "radar";
        
        setLayout(new BorderLayout());
        
        JPanel screen = new JPanel();
        screen.setLayout(new BorderLayout());
        
        fuzzy = true;
        heatMap = new HeatMap();
        heatMap.addActionListener(this);
        heatMap.setActionCommand("heatmap");
        heatMap.setFuzzy(fuzzy);
        screen.add(heatMap, BorderLayout.CENTER);
        
        timeScale = new TimeScale();
        
        JScrollPane screenScrollPane;
        screenScrollPane = new JScrollPane(screen, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        screenScrollPane.getVerticalScrollBar()  .setUnitIncrement(4);
        screenScrollPane.getHorizontalScrollBar().setUnitIncrement(4);
        // This way the time scale is drawn incorrectly, probably because
        // we would have to keep track of scrolling ourselves:
        //screenScrollPane.setColumnHeaderView(timeScale);
        // ... so just do it the simple way:
        screen.add(timeScale, BorderLayout.NORTH);
        
        add(screenScrollPane, BorderLayout.CENTER);
        
        entityLabel = new JLabel(" ");
        add(entityLabel, BorderLayout.NORTH);
    }
    
    public void setFuzzy(boolean fuzzy)
    {
        heatMap.setFuzzy(fuzzy);
    }
    
    public boolean getFuzzy()
    {
        return heatMap.getFuzzy();
    }
    
    public void setZoom(int level)
    {
        heatMap.setZoom(level);
    }
    
    public int getZoom()
    {
        return heatMap.getZoom();
    }
    
    public void setDataSize(int width, int height)
    {
        heatMap.setDataSize(width, height);
        validate();
    }
    
    public void setRow(String date, int row, double[] values)
    {
        timeScale.set(row, date);
        heatMap.setRow(row, values);
    }
    
    public void setColumn(String date, int column, double[] values)
    {
        timeScale.set(column, date);
        heatMap.setColumn(column, values);
    }
    
    public void setLabel(String text)
    {
        entityLabel.setText(text);
    }
    
    public void addActionListener(ActionListener listener)
    {
        actionListeners.add(listener);
    }
    
    public void setActionCommand(String command)
    {
        actionCommand = command;
    }
    
    public static class ActionEvent extends java.awt.event.ActionEvent
    {
        public enum Action { SCREEN_CLICK, SCREEN_MOUSEOVER, SCREEN_LEAVE };
        
        protected int row, column;
        protected Action action;
        
        public ActionEvent(Object source, int row, int column, String command, Action action)
        {
            super(source, ActionEvent.ACTION_PERFORMED, command);
            this.row    = row;
            this.column = column;
            this.action = action;
        }
        
        public int getRow()    { return row;    }
        public int getColumn() { return column; }
        public Action getAction() { return action; }
    }
    
    protected void dispatch(ActionEvent event)
    {
        for (ActionListener listener : actionListeners) {
            listener.actionPerformed(event);
        }
    }
    
    // ActionListener
    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        if ("heatmap".equals(e.getActionCommand())) {
            HeatMap heatmap = (HeatMap) e.getSource();
            HeatMap.ActionEvent event = (HeatMap.ActionEvent) e;
            switch (event.getAction()) {
            case CLICK:
                dispatch(new ActionEvent(this,
                                         event.getRow(), event.getColumn(),
                                         actionCommand,
                                         ActionEvent.Action.SCREEN_CLICK
                                         ));
                break;
            case MOVE:
                dispatch(new ActionEvent(this,
                                         event.getRow(), event.getColumn(),
                                         actionCommand,
                                         ActionEvent.Action.SCREEN_MOUSEOVER
                                         ));
                break;
            case LEAVE:
                dispatch(new ActionEvent(this,
                                         event.getRow(), event.getColumn(),
                                         actionCommand,
                                         ActionEvent.Action.SCREEN_LEAVE
                                         ));
                break;
            default:
                System.err.println("unexpected action in Radar.ActionEvent: " + event.getAction());
            }
        }
    }
    
}
