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
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Calendar;
import java.util.Vector;

public class TimeScale extends JPanel
    implements MouseInputListener
{
    protected Vector<ActionListener> actionListeners;
    protected String actionCommand;
    protected Vector<Date> dates;
    protected Date begin, end;
    protected Calendar calendar;
    protected int fontSize;
    protected String fontFamily;
    
    public TimeScale()
    {
        actionListeners = new Vector<ActionListener>();
        actionCommand = "time-scale";
        addMouseListener(this);
        addMouseMotionListener(this);
        
        dates = new Vector<Date>();
        begin = null;
        end   = null;
        calendar = Calendar.getInstance();
        
        Font font = javax.swing.UIManager.getFont("Label.font");
        fontSize = font.getSize();
        fontFamily = font.getFamily();
        
        int width = 4 * fontSize;
        setPreferredSize(new Dimension(40, 100));
        setMinimumSize(new Dimension(40, 40));
        setSize(new Dimension(40, 100));
    }
    
    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
        //g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setFont(getFont());
        g2.setColor(Color.BLACK);
        int fontOffset = fontSize;
        int x, y, lastLabelY;
        x = 0;
        y = 0;
        lastLabelY = -fontSize-1;
        int index = 0;
        for (Date date : dates) {
            y = index * getHeight() / dates.size();
            //g2.setColor(Color.RED);
            //g2.drawLine(0, y, getWidth(), y);
            if (y - lastLabelY > fontSize) {
                calendar.setTime(date);
                //g2.setColor(Color.BLACK);
                g2.drawString(String.valueOf(calendar.get(Calendar.YEAR)), 0, y + fontOffset);
                lastLabelY = y;
            }
            ++index;
        }
    }
    
    public void add(Date date)
    {
        if (null == begin) begin = date;
        if (null == end)   end   = date;
        dates.add(date);
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
        System.err.println("clicked time scale at " + e.getX() + ", " + e.getY());
    }
    
    // MouseListener
    public void mouseEntered(MouseEvent e) {}
    
    public void mouseExited(MouseEvent e) {}
    
    public void mousePressed(MouseEvent e) {}
    
    public void mouseReleased(MouseEvent e) {}
    
    // MouseMotionListener
    public void mouseDragged(MouseEvent e) {}
    
    public void mouseMoved(MouseEvent e) {}
    
}
