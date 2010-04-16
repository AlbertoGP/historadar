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

import javax.swing.JLabel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;

public class TimeScale extends JLabel
    implements MouseInputListener
{
    protected Vector<ActionListener> actionListeners;
    protected String actionCommand;
    protected Vector<Date> dates;
    protected Date begin, end;
    protected int fontSize;
    protected String fontFamily;
    protected String dateFormatPattern;
    protected java.text.SimpleDateFormat dateFormat;
    protected int dateTextWidth, dateTextHeight;
    
    public TimeScale()
    {
        actionListeners = new Vector<ActionListener>();
        actionCommand = "time-scale";
        addMouseListener(this);
        addMouseMotionListener(this);
        
        dates = new Vector<Date>();
        begin = null;
        end   = null;
        dateFormatPattern = "yyyy-MM-dd";
        dateFormat = (java.text.SimpleDateFormat) java.text.SimpleDateFormat.getDateInstance();
        dateFormat.applyPattern(dateFormatPattern);
        dateFormat.setLenient(true);
        
        Font font = javax.swing.UIManager.getFont("Label.font");
        fontSize = font.getSize();
        fontFamily = font.getFamily();
        
        adjustSize();
    }
    
    protected void adjustSize()
    {
        dateTextWidth  = dateFormatPattern.length() * fontSize * 3 / 5;
        dateTextHeight = fontSize;
        int width  = dateTextWidth * dates.size();
        int height = dateTextHeight * 2;
        if (width  < fontSize) width  = fontSize;
        if (height < fontSize) height = fontSize;
        
        Dimension size = new Dimension(dateTextWidth, height);
        setMinimumSize(new Dimension(dateTextWidth, fontSize));
        setPreferredSize(size);
        setSize(size);
    }
    
    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
        g2.clearRect(getX(), getY(), getWidth(), getHeight());
        g2.setFont(getFont());
        g2.setColor(Color.BLACK);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        int fontOffset = fontSize;
        int x0, y0, x, y, lastLabelX;
        x0 = 0;//getX();
        y0 = 0;//getY();
        x = 0;
        y = fontSize + fontSize/2;
        int step = dateTextWidth + fontSize;
        lastLabelX = -step-1;
        int index = 0;
        for (Date date : dates) {
            x = index * getWidth() / dates.size();
            g2.setColor(Color.GRAY);
            if (x - lastLabelX > step) {
                g2.drawLine(x0 + x, y0,
                            x0 + x, y0 + getHeight());
                g2.setColor(Color.BLACK);
                g2.drawString(dateFormat.format(date), x0 + x + 2, y0 + y);
                lastLabelX = x;
            }
            else {
                g2.drawLine(x0 + x, y0 + getHeight()-fontSize/4,
                            x0 + x, y0 + getHeight());
            }
            ++index;
        }
    }
    
    public void add(Date date)
    {
        if (null == begin) begin = date;
        if (null == end)   end   = date;
        dates.add(date);
        adjustSize();
    }
    
    public void add(String date)
    {
        add(parseDate(date));
    }
    
    public void set(int index, String date)
    {
        set(index, parseDate(date));
    }
    
    public void set(int index, Date date)
    {
        if (null == begin) begin = date;
        if (null == end)   end   = date;
        if (dates.size() <= index) dates.setSize(index + 1);
        dates.set(index, date);
        adjustSize();
    }
    
    protected Date parseDate(String date)
    {
        try {
            if (date != null) return dateFormat.parse(date);
        }
        catch (java.text.ParseException e) {
            System.err.println("Error when parsing date '" + date + "'\n" + e);
        }
        
        return new java.util.Date();
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
