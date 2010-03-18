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
import java.awt.Cursor;
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
import org.matracas.historadar.nlp.ner.OpenNlpNER;
import org.matracas.historadar.nlp.ner.StanfordNER;

/**
 * Main class of HistoRadar, with the GUI application.
 */
public class View
    implements ActionListener
{
    protected Preferences prefs;
    
    private JFrame window;
    private JSplitPane view;
    private JPanel documentPane;
    private JPanel metadataPane;
    private JTextField searchBox;
    private DocumentView documentView;
    private DocumentView documentSourceView;
    private Radar radar;
    private JPanel overlay;
    private JProgressBar progressBar;
    
    protected Document.Collection documents;
    protected Document currentDocument;
    protected String documentDate;
    protected Map<String, Document.SegmentList> segmentsInDocuments;
    
    protected OCR ocr;
    protected Metadata metadata;
    protected NER tagger;
    
    public View(String[] args)
    {
        prefs = Preferences.userNodeForPackage(View.class);
        
        JFrame.setDefaultLookAndFeelDecorated(false);
        window = new JFrame("HistoRadar View");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800,600);
        buildGUI(window);
        window.setVisible(true);
        
        overlay = new JPanel();
        overlay.setLayout(new BorderLayout());
        progressBar = new JProgressBar();
        overlay.add(progressBar, BorderLayout.CENTER);
        
        currentDocument = null;
        if (args.length == 1) {
            loadCollection(new File(args[0]));
        }
        else {
            documents = null;
        }
        showDocument();
    }
    
    protected void visualize(Document.Collection documents)
    {
        window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        visualizeEntities(documents);
        view.setDividerLocation(view.getWidth() - view.getDividerSize() - (int) radar.getPreferredSize().getWidth());
        showDocument();
        
        window.setCursor(Cursor.getDefaultCursor());
    }
    
    protected boolean loadCollection(File directory)
    {
        documents = new Document.Collection(directory);
        segmentsInDocuments = null;
        
        ocr      = new OCR(documents);
        metadata = new Metadata(documents);
        tagger   = new SimpleRegexp(documents);
        
        visualize(documents);
        
        return true;
    }
    
    protected void processCollection(Document.Collection documents)
    {
        segmentsInDocuments = new Hashtable<String, Document.SegmentList>();
        int progress = 0;
        for (Document document : documents) {
            progressBar.setValue(++progress);
            Document.SegmentList segments = annotateDocument(document);
            segmentsInDocuments.put(document.getIdentifier(), segments);
        }
        documents.sort();
    }
    
    protected void visualizeEntityTypes(Document.Collection documents)
    {
        if (null == documents) return;
        
        if (null == segmentsInDocuments) processCollection(documents);
        
        java.util.Set<String> types = new java.util.TreeSet<String>();
        java.util.Vector<Map<String, Integer> > typeCounts = new java.util.Vector<Map<String, Integer> >();
        
        int maxCount = 0;
        for (Document document : documents) {
            Document.SegmentList segments = segmentsInDocuments.get(document.getIdentifier());
            if (null == segments) segments = annotateDocument(document);
            
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
        
        radar.setDataSize(documents.size(), types.size());
        
        setRows(radar, types, typeCounts, maxCount);
    }
    
    protected java.util.Vector<String> rowLabels, columnLabels;
    protected void visualizeEntities(Document.Collection documents)
    {
        if (null == documents) return;
        
        if (null == segmentsInDocuments) processCollection(documents);
        
        java.util.Set<String> types = new java.util.TreeSet<String>();
        java.util.Vector<Map<String, Integer> > typeCounts = new java.util.Vector<Map<String, Integer> >();
        
        int maxCount = 0;
        for (Document document : documents) {
            Document.SegmentList segments = segmentsInDocuments.get(document.getIdentifier());
            if (null == segments) segments = annotateDocument(document);
            
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
        
        radar.setDataSize(documents.size(), types.size());
        
        setRows(radar, types, typeCounts, maxCount);
    }
    
    protected void setRows(Radar radar, java.util.Set<String> types, java.util.Vector<Map<String, Integer> > typeCounts, int maxCount)
    {
        rowLabels    = new java.util.Vector<String>();
        columnLabels = new java.util.Vector<String>();
        columnLabels.addAll(types);
        
        int row = 0;
        double[] counts = new double[types.size()];
        Iterator<Document> documentIterator = documents.iterator();
        for (Map<String, Integer> typeCount : typeCounts) {
            int i = 0;
            for (String type : types) {
                Integer count = typeCount.get(type);
                if (null == count || 0 == count) {
                    counts[i] = 0.0;
                }
                else {
                    // Map values to luminosity asymptotically towards 1.0,
                    // to emphasize differences when the number of mentions
                    // is low, starting with 0.2 for single occurrences
                    // to make them visible.
                    counts[i] = (((double) count) - 0.8) / ((double) count);
                }
                ++i;
            }
            
            String date;
            Document.Metadata.Values values;
            values = documentIterator.next().getMetadata().get(Document.Metadata.date);
            if (values != null) date = values.lastElement();
            else                date = "BOGUS";
            rowLabels.add(date);
            
            radar.setColumn(date, row, counts);
            ++row;
        }
    }
    
    public void actionPerformed(ActionEvent e)
    {
        String command = e.getActionCommand();
        if ("quit".equals(command)) {
            System.exit(0);
        }
        else if ("load-collection".equals(command)) {
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
            
            showDocument();
        }
        else if ("ner-engine-simple-regexp".equals(command)) {
            tagger = new SimpleRegexp(documents);
            segmentsInDocuments = null;
            visualize(documents);
        }
        else if ("ner-engine-opennlp-maxent".equals(command)) {
            tagger = new OpenNlpNER(documents);
            segmentsInDocuments = null;
            visualize(documents);
        }
        else if ("ner-engine-stanford".equals(command)) {
            tagger = new StanfordNER(documents);
            segmentsInDocuments = null;
            visualize(documents);
        }
        else if ("radar".equals(command)) {
            Radar.ActionEvent event = (Radar.ActionEvent) e;
            if (columnLabels != null && rowLabels != null) {
                switch (event.getAction()) {
                case SCREEN_CLICK:
                    showDocument(documents.get(event.getColumn()));
                    searchBox.setText(columnLabels.get(event.getRow()));
                    break;
                case SCREEN_MOUSEOVER:
                    radar.setLabel(rowLabels.get(event.getColumn()) + ", " + columnLabels.get(event.getRow()));
                    break;
                default:
                    System.err.println("unexpected action in Radar.ActionEvent: " + event.getAction());
                }
            }
        }
        else if ("radar-image-fuzzy".equals(command)) {
            radar.setFuzzy(true);
            radar.repaint();
        }
        else if ("radar-image-sharp".equals(command)) {
            radar.setFuzzy(false);
            radar.repaint();
        }
        else if ("radar-image-zoom-1".equals(command)) {
            radar.setZoom(1);
        }
        else if ("radar-image-zoom-2".equals(command)) {
            radar.setZoom(2);
        }
        else if ("radar-image-zoom-3".equals(command)) {
            radar.setZoom(3);
        }
        else if ("radar-image-zoom-4".equals(command)) {
            radar.setZoom(4);
        }
        else if ("radar-image-zoom-10".equals(command)) {
            radar.setZoom(10);
        }
        else if ("radar-image-zoom-20".equals(command)) {
            radar.setZoom(20);
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
        int corrections = ocr.correctDocument(document);
        System.err.println("Corrected " + corrections + " errors from the OCR text");
        
        // Metadata extraction
        System.err.println("Extracting metadata...");
        Document.Metadata entries = metadata.getMetadata(document);
        document.getMetadata().putAll(entries);
        
        System.err.println("Tagging text...");
        Document.SegmentList segments;
        segments = tagger.getEntities(document);
        
        return segments;
    }
    
    protected JTextField label(String text)
    {
        JTextField label = new JTextField(text);
        label.setEditable(false);
        return label;
    }
    
    protected void showDocument()
    {
        showDocument(currentDocument);
    }
    
    protected void showDocument(Document document)
    {
        if (null == document && documents != null) {
            document = documents.getDocument(null);
        }
        
        if (document == null) return;
        
        window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        Document.Metadata.Values values;
        metadataPane.removeAll();
        values = document.getMetadata().get(Document.Metadata.title);
        if (values != null) {
            Iterator valueIterator = values.iterator();
            while (valueIterator.hasNext()) {
                metadataPane.add(label("Title: " + valueIterator.next()));
            }
        }
        values = document.getMetadata().get(Document.Metadata.date);
        if (values != null) {
            Iterator<String> valueIterator = values.iterator();
            while (valueIterator.hasNext()) {
                documentDate = valueIterator.next();
                metadataPane.add(label("Date: " + documentDate));
            }
        }
        metadataPane.add(label("Identifier: " + document.getIdentifier()));
        window.setTitle("HistoRadar View - " + document.getIdentifier());
        
        Document.SegmentList segments;
        if (segmentsInDocuments != null) {
            segments = segmentsInDocuments.get(document.getIdentifier());
        }
        else {
            segments = null;
        }
        if (null == segments) segments = annotateDocument(document);
        
        documentView.setText(document, segments);
        documentSourceView.setText(document, segments);
        
        window.validate();
        
        window.setCursor(Cursor.getDefaultCursor());
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
        radar.setActionCommand("radar");
        
        metadataPane = new JPanel();
        documentPane = new JPanel();
        
        documentPane.setLayout(new BoxLayout(documentPane, BoxLayout.PAGE_AXIS));
        metadataPane.setLayout(new BoxLayout(metadataPane, BoxLayout.PAGE_AXIS));
        documentPane.add(metadataPane);
        documentPane.add(searchBox = new JTextField());
        
        JTabbedPane tabbedDocumentView = new JTabbedPane();
        tabbedDocumentView.addTab("Document", null, new JScrollPane(documentView), "The annotated document");
        tabbedDocumentView.addTab("Source", null, new JScrollPane(documentSourceView), "XML source of the annotated document");
        documentPane.add(tabbedDocumentView);
        
        view = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, documentPane, radar);
        view.setOneTouchExpandable(true);
        view.setResizeWeight(0.0);
        
        frame.getContentPane().add(view);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menu, submenu;
        JMenuItem item;
        ButtonGroup group;
        
        menu = new JMenu("File");
        menu.add(item = new JMenuItem("Load collection"));
        item.addActionListener(this);
        item.setActionCommand("load-collection");
        menu.addSeparator();
        menu.add(item = new JMenuItem("Quit"));
        item.addActionListener(this);
        item.setActionCommand("quit");
        menuBar.add(menu);
        
        menu = new JMenu("NER");
        menu.setToolTipText("Named Entity Recognition");
        group = new ButtonGroup();
        menu.add(item = new JRadioButtonMenuItem("Simple regexp (built-in)"));
        item.addActionListener(this);
        item.setActionCommand("ner-engine-simple-regexp");
        item.setSelected(true);
        group.add(item);
        menu.add(item = new JRadioButtonMenuItem("OpenNLP Maxent"));
        item.addActionListener(this);
        item.setActionCommand("ner-engine-opennlp-maxent");
        group.add(item);
        menu.add(item = new JRadioButtonMenuItem("Stanford NER"));
        item.addActionListener(this);
        item.setActionCommand("ner-engine-stanford");
        group.add(item);
        menuBar.add(menu);
        
        menu = new JMenu("Radar");
        group = new ButtonGroup();
        menu.add(item = new JRadioButtonMenuItem("Fuzzy"));
        item.addActionListener(this);
        item.setActionCommand("radar-image-fuzzy");
        item.setSelected(true);
        group.add(item);
        menu.add(item = new JRadioButtonMenuItem("Sharp"));
        item.addActionListener(this);
        item.setActionCommand("radar-image-sharp");
        group.add(item);
        menu.add(submenu = new JMenu("Zoom"));
        group = new ButtonGroup();
        submenu.add(item = new JRadioButtonMenuItem("1×"));
        item.addActionListener(this);
        item.setActionCommand("radar-image-zoom-1");
        group.add(item);
        submenu.add(item = new JRadioButtonMenuItem("2×"));
        item.addActionListener(this);
        item.setActionCommand("radar-image-zoom-2");
        group.add(item);
        submenu.add(item = new JRadioButtonMenuItem("3×"));
        item.addActionListener(this);
        item.setActionCommand("radar-image-zoom-3");
        group.add(item);
        submenu.add(item = new JRadioButtonMenuItem("4×"));
        item.addActionListener(this);
        item.setActionCommand("radar-image-zoom-4");
        item.setSelected(true);
        group.add(item);
        submenu.add(item = new JRadioButtonMenuItem("10×"));
        item.addActionListener(this);
        item.setActionCommand("radar-image-zoom-10");
        group.add(item);
        submenu.add(item = new JRadioButtonMenuItem("20×"));
        item.addActionListener(this);
        item.setActionCommand("radar-image-zoom-20");
        group.add(item);

        menuBar.add(menu);
        
        menu = new JMenu("About");
        menu.add(new JMenuItem("Version 2010-03-18 08:48"));
        menuBar.add(menu);
        
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
