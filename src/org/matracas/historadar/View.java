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

package org.matracas.historadar;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Hashtable;
import java.util.prefs.Preferences;

import org.matracas.historadar.Document;
import org.matracas.historadar.ui.DocumentView;
import org.matracas.historadar.ui.Radar;
import org.matracas.historadar.nlp.OCR;
import org.matracas.historadar.nlp.Metadata;
import org.matracas.historadar.nlp.NER;
import org.matracas.historadar.nlp.ner.SimpleRegexp;

/**
 * Main class of HistoRadar, with the GUI application.
 */
public class View implements ActionListener
{
    protected Preferences prefs;
    
    private JFrame window;
    private JSplitPane view;
    private JPanel documentPane;
    private JPanel metadataPane;
    private DocumentView documentView;
    private DocumentView documentSourceView;
    private Radar radar;
    
    protected Document.Collection documents;
    protected String documentDate;
    
    public View(String[] args)
    {
        prefs = Preferences.userNodeForPackage(View.class);
        
        JFrame.setDefaultLookAndFeelDecorated(false);
        window = new JFrame("HistoRadar View");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800,600);
        buildGUI(window);
        window.setVisible(true);
        
        if (args.length == 1) {
            loadCollection(new File(args[0]));
            showDocument(null);
        }
        else {
            documents = null;
        }
    }
    
    protected boolean loadCollection(File directory)
    {
        documents = new Document.Collection(directory);
        
        visualizeEntities(documents);
        
        System.err.println("----------- " + radar.getMinimumSize().getWidth());
        view.setDividerLocation(view.getWidth() - view.getDividerSize() - (int) radar.getMinimumSize().getWidth());
        
        return true;
    }
    
    protected void visualizeEntityTypes(Document.Collection documents)
    {
        if (null == documents) return;
        
        java.util.Set<String> types = new java.util.TreeSet<String>();
        java.util.Vector<Map<String, Integer> > typeCounts = new java.util.Vector<Map<String, Integer> >();
        
        int maxCount = 0;
        java.util.Vector<String> documentDates = new java.util.Vector<String>();
        for (Document document : documents) {
            Document.SegmentList segments = annotateDocument(document);
            documentDates.add(documentDate);
            Map<String, Integer> typeCount = new Hashtable<String, Integer>();
            for (Document.Segment segment : segments) {
                String type = segment.get("pattern-name");
                types.add(type);
                Integer count = typeCount.get(type);
                if (null == count) {
                    count = new Integer(0);
                }
                ++count;
                typeCount.put(type, count);
                if (count > maxCount) maxCount = count;
            }
            typeCounts.add(typeCount);
        }
        
        radar.setDataSize(types.size(), documents.size());
        
        int row = 0;
        double[] counts = new double[types.size()];
        for (Map<String, Integer> typeCount : typeCounts) {
            int i = 0;
            for (String type : types) {
                Integer count = typeCount.get(type);
                if (null == count) counts[i] = 0.0;
                else               counts[i] = ((double) count) / maxCount;
                ++i;
            }
            radar.setRow(documentDates.get(row), row, counts);
            ++row;
        }
        System.err.println("row = " + row);
    }
    
    protected void visualizeEntities(Document.Collection documents)
    {
        if (null == documents) return;
        
        java.util.Set<String> types = new java.util.TreeSet<String>();
        java.util.Vector<Map<String, Integer> > typeCounts = new java.util.Vector<Map<String, Integer> >();
        
        int maxCount = 0;
        java.util.Vector<String> documentDates = new java.util.Vector<String>();
        for (Document document : documents) {
            Document.SegmentList segments = annotateDocument(document);
            documentDates.add(documentDate);
            Map<String, Integer> typeCount = new Hashtable<String, Integer>();
            for (Document.Segment segment : segments) {
                String type = document.getPlainText(segment);
                types.add(type);
                Integer count = typeCount.get(type);
                if (null == count) {
                    count = new Integer(0);
                }
                ++count;
                typeCount.put(type, count);
                if (count > maxCount) maxCount = count;
            }
            typeCounts.add(typeCount);
        }
        
        radar.setDataSize(types.size(), documents.size());
        
        int row = 0;
        double[] counts = new double[types.size()];
        for (Map<String, Integer> typeCount : typeCounts) {
            int i = 0;
            for (String type : types) {
                Integer count = typeCount.get(type);
                if (null == count) counts[i] = 0.0;
                else               counts[i] = ((double) count) / maxCount;
                ++i;
            }
            radar.setRow(documentDates.get(row), row, counts);
            ++row;
        }
        System.err.println("row = " + row + ", documents.size() = " + documents.size() + ", documentDates.size() = " + documentDates.size());
    }
    
    public void actionPerformed(ActionEvent e)
    {
        String command = e.getActionCommand();
        doCommand(command);
    }
    
    protected void doCommand(String command)
    {
        if ("quit".equals(command)) {
            System.exit(0);
        }
        else if ("load-collection".equals(command)) {
            if (null == documents) {
                String lastCollectionLoaded = prefs.get("defaultCollection", ".");
                JFileChooser fileChooser = new JFileChooser(lastCollectionLoaded);
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = fileChooser.showOpenDialog(window);
                if (JFileChooser.APPROVE_OPTION == returnValue) {
                    File directory = fileChooser.getSelectedFile();
                    prefs.put("defaultCollection", directory.getAbsolutePath());
                    loadCollection(directory);
                }
                else {
                    System.err.println("Collection loading cancelled");
                }
            }
            
            if (documents != null) showDocument(null);
        }
        else {
            System.err.println("Error: Unexpected command: '" + command + "'");
            System.exit(1);
        }
    }
    
    protected Document.SegmentList annotateDocument(Document document)
    {
        if (null == document) return null;
        
        System.err.println("Processing document " + document.getIdentifier());
        
        // OCR correction
        System.err.println("Correcting OCR...");
        OCR ocr = new OCR(documents);
        int corrections = ocr.correctDocument(document);
        System.err.println("Corrected " + corrections + " errors from the OCR text");
        
        // Metadata extraction
        System.err.println("Extracting metadata...");
        Metadata metadata = new Metadata(documents);
        Document.Metadata entries = metadata.getMetadata(document);
        Document.Metadata.Values values;
        metadataPane.removeAll();
        values = entries.get(Metadata.title);
        if (values != null) {
            Iterator valueIterator = values.iterator();
            while (valueIterator.hasNext()) {
                metadataPane.add(label("Title: " + valueIterator.next()));
            }
        }
        values = entries.get(Metadata.date);
        if (values != null) {
            Iterator<String> valueIterator = values.iterator();
            while (valueIterator.hasNext()) {
                documentDate = valueIterator.next();
                metadataPane.add(label("Date: " + documentDate));
            }
        }
        metadataPane.add(label("Identifier: " + document.getIdentifier()));
        
        System.err.println("Tagging text...");
        Document.SegmentList segments;
        NER tagger = new SimpleRegexp(documents);
        segments = tagger.getEntities(document);
        
        return segments;
    }
    
    protected JTextField label(String text)
    {
        JTextField label = new JTextField(text);
        label.setEditable(false);
        //label.setBorder(null);
        return label;
    }
    
    protected void showDocument(Document document)
    {
        if (null == document && documents != null) {
            document = documents.getDocument(null);
        }
        
        if (document == null) return;
        
        Document.SegmentList segments = annotateDocument(document);
        
        
        documentView.setText(document, segments);
        documentSourceView.setText(document, segments);
        
        System.err.println("Text set");
        
        window.validate();
    }
    
    private void buildGUI(JFrame frame)
    {
        documentView = new DocumentView();
        documentView.setEditable(false);
        documentView.setContentType("text/html");
        
        documentSourceView = new DocumentView();
        documentSourceView.setEditable(false);
        documentSourceView.setContentType("text/plain");
        
        radar = new Radar();
        radar.addActionListener(this);
        radar.setActionCommand("radar-click");
        
        metadataPane = new JPanel();
        documentPane = new JPanel();
        
        documentPane.setLayout(new BoxLayout(documentPane, BoxLayout.PAGE_AXIS));
        metadataPane.setLayout(new BoxLayout(metadataPane, BoxLayout.PAGE_AXIS));
        documentPane.add(metadataPane);
        
        JTabbedPane tabbedDocumentView = new JTabbedPane();
        tabbedDocumentView.addTab("Document", null, new JScrollPane(documentView), "The annotated document");
        tabbedDocumentView.addTab("Source", null, new JScrollPane(documentSourceView), "XML source of the annotated document");
        documentPane.add(tabbedDocumentView);
        
        view = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, documentPane, new JScrollPane(radar));
        view.setOneTouchExpandable(true);
        view.setResizeWeight(0.0);
        
        frame.getContentPane().add(view);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem item;
        menu.add(item = new JMenuItem("Load collection"));
        item.addActionListener(this);
        item.setActionCommand("load-collection");
        menu.addSeparator();
        menu.add(item = new JMenuItem("Quit"));
        item.addActionListener(this);
        item.setActionCommand("quit");
        frame.setJMenuBar(menuBar);
    }
    
    public static void main(final String[] args)
    {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() 
                {
                    View view = new View(args);
                }
            });
    }
}
