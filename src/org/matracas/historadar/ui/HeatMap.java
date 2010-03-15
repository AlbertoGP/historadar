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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import javax.swing.event.MouseInputListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Vector;

public class HeatMap extends JPanel
    implements MouseInputListener, MouseWheelListener
{
    protected Vector<ActionListener> actionListeners;
    protected String actionCommand;
    protected int dataWidth, dataHeight;
    protected BufferedImage image;
    
    public HeatMap()
    {
        image = null;
        dataWidth  = 0;
        dataHeight = 0;
        actionListeners = new Vector<ActionListener>();
        actionCommand = "heatmap";
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }
    
    public void setDataSize(int width, int height)
    {
        dataWidth  = width;
        dataHeight = height;
        if (dataWidth  < 1) dataWidth  = 1;
        if (dataHeight < 1) dataHeight = 1;
        image = new BufferedImage(dataWidth, dataHeight,
                                  BufferedImage.TYPE_INT_RGB);
        setSize(dataWidth, dataHeight);
        setMinimumSize(getSize());
    }
    
    public Dimension getPreferredSize()
    {
        return new Dimension(dataWidth * 4, dataHeight * 4);
    }
    
    public void setRow(int row, double[] values)
    {
        if (row < 0 || row >= dataHeight) return;
        
        int end = values.length;
        if (end > dataWidth) end = dataWidth;
        
        for (int i = 0; i < end; ++i) {
            image.setRGB(i, row, ((int) (values[i] * 255.0)) << 8);
        }
    }
    
    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.RED);
        //g2.fillRect(0, 0, getWidth(), getHeight());
        if (image != null) g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }
    
    protected int getRow(int y)
    {
        return y * dataHeight / getHeight();
    }
    
    protected int getColumn(int x)
    {
        return x * dataWidth / getWidth();
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
        public enum Action { CLICK, MOVE };
        
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
    
    // MouseInputListener = MouseListener + MouseMotionLister
    public void mouseClicked(MouseEvent e)
    {
        ActionEvent event = new ActionEvent(this,
                                            getRow(e.getY()),
                                            getColumn(e.getX()),
                                            actionCommand,
                                            ActionEvent.Action.CLICK);
        dispatch(event);
    }
    
    public void mouseMoved(MouseEvent e)
    {
        ActionEvent event = new ActionEvent(this,
                                            getRow(e.getY()),
                                            getColumn(e.getX()),
                                            actionCommand,
                                            ActionEvent.Action.MOVE);
        dispatch(event);
    }
    
    // MouseListener
    public void mouseEntered(MouseEvent e) {}
    
    public void mouseExited(MouseEvent e) {}
    
    public void mousePressed(MouseEvent e) {}
    
    public void mouseReleased(MouseEvent e) {}
    
    // MouseMotionListener
    public void mouseDragged(MouseEvent e) {}
    
    // MouseWheelListener
    public void mouseWheelMoved(MouseWheelEvent e) {}
    
}
