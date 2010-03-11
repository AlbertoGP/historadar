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
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import java.util.Map;
import java.util.SortedSet;
import java.util.prefs.Preferences;

import org.matracas.historadar.Document;

/**
 * Visualization of a document's content.
 */
public class DocumentView extends JTextPane
{
    public DocumentView()
    {
        super();
        setMinimumSize(new java.awt.Dimension(100, 100));
    }
    
    public void setText(Document document, Document.SegmentList segments)
    {
        if (document == null) return;
        if ("text/html".equals(getContentType())) {
            setText(document.getXMLString(segments, document.getXMLVocabulary("html")));
            StyleSheet styleSheet = ((HTMLDocument) getDocument()).getStyleSheet();
            styleSheet.addRule("span { background:#CCCCCC; }");
            SortedSet<String> types = new java.util.TreeSet<String>();
            for (Document.Segment segment : segments) {
                types.add(segment.get("pattern-name"));
            }
            for (String type : types) {
                styleSheet.addRule("span." + type + " { background:" + getColor(type) + "; }");
            }
            ToolTipManager.sharedInstance().registerComponent(this);
        }
        else {
            setText(document.getXMLString(segments));
        }
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
    
}
