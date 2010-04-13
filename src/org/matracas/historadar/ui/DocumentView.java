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

import javax.swing.*;
import java.awt.Font;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.Highlighter;
import java.util.Map;
import java.util.SortedSet;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import org.matracas.historadar.Document;

/**
 * Visualization of a document's content.
 */
public class DocumentView extends JTextPane
{
    protected int matchCount, currentMatch;
    protected Pattern regexpEscape;
    protected Highlighter.HighlightPainter highlightPainter;
    protected java.awt.Stroke stroke;
    private boolean modified;
    
    public DocumentView()
    {
        super();
        setMinimumSize(new java.awt.Dimension(100, 100));
        matchCount   = 0;
        currentMatch = 0;
        regexpEscape = Pattern.compile("([.*?{}$()\\[\\]^\\\\+-])");
        highlightPainter = new MatchHighlightPainter();
        stroke = new java.awt.BasicStroke(3.2f);
        modified = false;
    }
    
    protected class MatchHighlightPainter implements Highlighter.HighlightPainter
    {
        public MatchHighlightPainter()
        {
        }
        
        public void paint(java.awt.Graphics g, int begin, int end, java.awt.Shape bounds, javax.swing.text.JTextComponent component)
        {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
            g2.setColor(java.awt.Color.red);
            g2.setStroke(stroke);
            java.awt.Rectangle beginRectangle, endRectangle;
            java.awt.Rectangle boundsRectangle = bounds.getBounds();
            try {
                beginRectangle = component.modelToView(begin);
                int current = begin;
                while (current < end) {
                    endRectangle   = component.modelToView(current + 1);
                    int x0, y0, x1, y1;
                    x0 = beginRectangle.x;
                    y0 = beginRectangle.y + beginRectangle.height;
                    x1 = endRectangle.x;
                    y1 = endRectangle.y + endRectangle.height;
                    if (current == begin) {
                        g.drawLine(x0    , beginRectangle.y, x0    , y0);
                    }
                    if (current + 1 == end) {
                        g.drawLine(x1    , endRectangle.y, x1    , y1);
                    }
                    if (y1 == y0) {
                        g.drawLine(x0, y0  , x1, y0  );
                        y0 = beginRectangle.y;
                        g.drawLine(x0, y0  , x1, y0  );
                    }
                    else {
                        int left, right;
                        // This is valid for left-to-right text, and would have
                        // to be reversed for right-to-left.
                        left  = boundsRectangle.x;
                        right = left + boundsRectangle.width;
                        g.drawLine(x0, y0  , right, y0  );
                        y0 = beginRectangle.y;
                        g.drawLine(x0, y0  , right, y0  );
                        
                        g.drawLine(left, y1  , x1, y1  );
                        y1 = endRectangle.y;
                        g.drawLine(left, y1  , x1, y1  );
                    }
                    beginRectangle = endRectangle;
                    ++current;
                }
            }
            catch (javax.swing.text.BadLocationException e) {
                System.err.println("Error in MatchHighlightPainter: " + e);
            }
        }
        
    }
    
    public void setText(Document document, Document.SegmentList segments)
    {
        if (document == null) return;
        if ("text/html".equals(getContentType())) {
            setText(document.getXMLString(segments, document.getXMLVocabulary("html")));
            StyleSheet styleSheet = ((HTMLDocument) getDocument()).getStyleSheet();
            styleSheet.addRule("span { background:#CCCCCC; } div.page { background:transparent; border-style:solid; border-width:1px; border-color:black; padding:10px; margin:4px; } ");
            SortedSet<String> types = new java.util.TreeSet<String>();
            for (Document.Segment segment : segments) {
                types.add(segment.get("pattern-name"));
            }
            for (String type : types) {
                styleSheet.addRule("span." + type + " { background:" + getColor(type) + "; }");
            }
            ToolTipManager.sharedInstance().registerComponent(this);
            setCaretPosition(0);
        }
        else {
            setText(document.getXMLString(segments));
        }
    }
    
    public void setText(String text)
    {
        setModified(true);
        super.setText(text);
    }
    
    public void insertHTML(int position, String content)
    {
        try {
            HTMLDocument htmlDocument = (HTMLDocument) getDocument();
            javax.swing.text.html.HTMLEditorKit editorKit = (javax.swing.text.html.HTMLEditorKit) getEditorKit();
            editorKit.insertHTML(htmlDocument, position, content, 0, 0, null);
            setModified(true);
        }
        catch (javax.swing.text.BadLocationException e) {
            System.err.println("Error in DocumentView.insertHTML(): " + e);
        }
        catch (java.io.IOException e) {
            System.err.println("Error in DocumentView.insertHTML(): " + e);
        }
    }
    
    public void setModified(boolean modified)
    {
        this.modified = modified;
    }
    
    public boolean getModified()
    {
        return modified;
    }
    
    public int getMatchCount()
    {
        return matchCount;
    }
    
    public int getCurrentMatch()
    {
        return currentMatch;
    }
    
    public boolean hasNextMatch()
    {
        return currentMatch + 1 < matchCount;
    }
    
    public boolean hasPreviousMatch()
    {
        return currentMatch > 0;
    }
    
    public boolean moveToMatch(int index)
    {
        Highlighter.Highlight[] highlights = getHighlighter().getHighlights();
        if (index >= 0 && index < highlights.length) {
            setCaretPosition(highlights[index].getStartOffset());
            setCaretPosition(highlights[index].getEndOffset());
            
            return true;
        }
        else {
            return false;
        }
    }
    
    public boolean moveToNextMatch()
    {
        boolean moved = moveToMatch(currentMatch + 1);
        if (moved) ++currentMatch;
        
        return moved;
    }
    
    public boolean moveToPreviousMatch()
    {
        boolean moved = moveToMatch(currentMatch - 1);
        if (moved) --currentMatch;
        
        return moved;
    }
    
    protected Pattern searchPattern(String queryString)
    {
        return Pattern.compile("\\b" + queryString,
                               Document.PatternTable.CASE_INSENSITIVE);
    }
    
    public void search(String text)
    {
        Pattern pattern = null;
        if (text != null && text.length() > 0) {
            java.util.regex.Matcher matcher = regexpEscape.matcher(text);
            pattern = searchPattern(matcher.replaceAll("\\\\$0"));
        }
        
        search(pattern);
    }
    
    public void search(java.util.regex.Pattern pattern)
    {
        matchCount   = 0;
        currentMatch = 0;
        Highlighter highlighter = getHighlighter();
        highlighter.removeAllHighlights();
        if (null == pattern) {
            repaint();
            return;
        }
        
        HTMLDocument document = (HTMLDocument) getDocument();
        for (HTMLDocument.Iterator contentIterator = document.getIterator(HTML.Tag.CONTENT);
             contentIterator.isValid();
             contentIterator.next())
            {
                try {
                    String text = document.getText(contentIterator.getStartOffset(),
                                                   contentIterator.getEndOffset() - contentIterator.getStartOffset());
                    java.util.regex.Matcher matcher = pattern.matcher(text);
                    int begin, end;
                    while (matcher.find()) {
                        ++matchCount;
                        begin = contentIterator.getStartOffset() + matcher.start();
                        end   = contentIterator.getStartOffset() + matcher.end();
                        highlighter.addHighlight(begin, end, highlightPainter);
                    }
                }
                catch (javax.swing.text.BadLocationException e) {
                    System.err.println("Error in DocumentView: " + e);
                }
            }
        
        if (matchCount > 0) moveToMatch(0);
        
        repaint();
    }
    
    /**
     * Get the page number for the current caret position.
     *
     * This relies on the pages being marked as "div" elements,
     * with the attribute "number" being the page number.
     *
     * @return the page number if found, or 0 otherwise
     */
    public int getPageNumber()
    {
        int pageNumber = 0;
        HTMLDocument htmlDocument = (HTMLDocument) getDocument();
        
        javax.swing.text.Element page = null;
        javax.swing.text.Element p = htmlDocument.getParagraphElement(getCaretPosition());
        while (p != null) {
            if ("div".equals(p.getName())) {
                javax.swing.text.AttributeSet attributes = p.getAttributes();
                for (java.util.Enumeration<?> e = attributes.getAttributeNames(); e.hasMoreElements();) {
                    String name, value;
                    Object element = e.nextElement();
                    name = element.toString();
                    value = attributes.getAttribute(element).toString();
                    if ("number".equals(name)) pageNumber = Integer.parseInt(value);
                }
            }
            p = p.getParentElement();
        }
        
        return pageNumber;
    }
    
    protected static String[] palette = {
        "#FFAAAA", "#AAFFAA", "#AAAAFF", "#FFFFAA", "#AAFFFF", "#FFAAFF"
    };
    protected Map<String, String> paletteMap = null;
    protected int currentColor = 0;
    
    protected String getColor(String key)
    {
        String color;
        if (null == paletteMap) {
            paletteMap = new java.util.Hashtable<String, String>();
        }
        color = paletteMap.get(key);
        if (null == color) {
            color = palette[currentColor];
            currentColor = (currentColor + 1) % palette.length;
            paletteMap.put(key, color);
        }
        
        return color;
    }
    
    public void load(File file) throws java.io.IOException
    {
        java.io.IOException exception = null;
        
        BufferedReader reader = null;
        try { 
            String plainText = "";
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                plainText += new String(line.getBytes(), "UTF-8") + "\n";
            }
            setText(plainText);
        }
        catch (java.io.IOException e) {
            exception = e;
        }
        
        try {
            if (reader != null) reader.close();
        }
        catch (java.io.IOException e) {
            if (null == exception) exception = e;
        }
        
        if (exception != null) throw exception;
    }
    
    public void save(File file) throws java.io.IOException
    {
        java.io.IOException exception = null;
        
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(getText());
        }
        catch (java.io.IOException e) {
            exception = e;
        }
        
        try {
            if (writer != null) writer.close();
        }
        catch (java.io.IOException e) {
            if (null == exception) exception = e;
        }
        
        if (exception != null) throw exception;
    }
    
}
