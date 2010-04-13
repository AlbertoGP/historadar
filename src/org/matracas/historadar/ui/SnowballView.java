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

import org.matracas.historadar.Document;

/**
 * Visualization of the work snowball.
 */
public class SnowballView extends DocumentView
{
    protected int contextRadius = 200;
    
    public SnowballView()
    {
        super();
    }
    
    public SnowballView addEntry(Document document, DocumentView documentView, String searchQuery)
    {
        if (null == document) return this;
        
        int pageNumber      = documentView.getPageNumber();
        int position        = documentView.getCaretPosition();
        HTMLDocument htmlDocument = (HTMLDocument) documentView.getDocument();
        String documentText;
        try {
            documentText = htmlDocument.getText(0, htmlDocument.getLength());
        }
        catch (javax.swing.text.BadLocationException e) {
            // This isn't always accurate, but it's a last resort.
            documentText = document.getPlainText();
        }
        if (position > documentText.length()) {
            System.err.println("Error in SnowballView: caret position " + position + " > document length " + documentText.length() + "\n" + documentText);
        }
        
        int contextBegin, contextEnd;
        contextBegin = position;
        contextEnd   = position + 1;
        char c;
        boolean minimumReached;
        while (contextBegin > 0) {
            minimumReached = position - contextBegin > contextRadius;
            c = documentText.charAt(contextBegin);
            if (!Character.isLetter(c) && minimumReached) break;
            --contextBegin;
        }
        while (contextEnd < documentText.length()) {
            minimumReached = contextEnd - position > contextRadius;
            c = documentText.charAt(contextEnd);
            if (!Character.isLetter(c) && minimumReached) break;
            ++contextEnd;
        }
        String context = documentText.substring(contextBegin + 1, contextEnd);
        if (searchQuery != null && searchQuery.length() > 0) {
            java.util.regex.Matcher matcher = searchPattern(searchQuery).matcher(context);
            context = matcher.replaceAll("<b>$0</b>");
        }
        
        String content = "<div style='font-family:sans-serif'>";
        if (document.getURI() != null) {
            content += "<h2>Document <a href='" + document.getURI() + "'>" + document.getIdentifier() + "</a></h2>";
        }
        else {
            content += "<h2>Document " + document.getIdentifier() + "</h2>";
        }
        content += "<table border='1'>";
        Document.Metadata.Values values;
        values = document.getMetadata().get(Document.Metadata.date);
        if (values != null) {
            java.util.Iterator<String> valueIterator = values.iterator();
            while (valueIterator.hasNext()) {
                content += "<tr><td>Date</td><td colspan='2'>" + valueIterator.next() + "</td></tr>";
            }
        }
            
        content += "<tr><td>Search query</td><td>" + searchQuery + "</td>";
        if (documentView.getMatchCount() > 0) {
            content += "<td>" + (documentView.getCurrentMatch() + 1) + "</td></tr>";
        }
        else {
            content += "<td>-</td></tr>";
        }
        content += "<tr><td>Position</td><td>" + position + "</td>";
        content += "<td>" + (position * 100 / documentText.length()) + "%</td></tr>";
        content += "<tr><td>Page</td><td colspan='2'>" + pageNumber + "</td></tr>";
        java.util.Date date = new java.util.Date();
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        content += "<tr><td>Date bookmarked</td><td colspan='2'>" + dateFormat.format(date) + "</td></tr>";
        content += "<tr><td colspan='3' style='font-family:serif'>" + context + "</td></tr>";
        content += "<tr><td>Notes</td><td colspan='2'>&nbsp;</td></tr>";
        content += "</table></div> <br />";
        HTMLDocument snowball = (HTMLDocument) getDocument();
        insertHTML(snowball.getLength(), content);
        setCaretPosition(snowball.getLength());
        
        return this;
    }
}
