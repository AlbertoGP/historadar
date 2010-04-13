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
import org.matracas.historadar.ui.SnowballView;
import org.matracas.historadar.ui.Radar;
import org.matracas.historadar.ui.FileNameRegexpFilter;
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
    private Map<String, JMenuItem> menuItems;
    private Map<String, JButton>   buttons;
    private JSplitPane view;
    private JPanel documentPane;
    private JPanel metadataPane;
    private JTextField searchBox;
    private DocumentView documentView;
    private SnowballView snowballView;
    private JTabbedPane tabbedDocumentView;
    private File snowballFile;
    private Radar radar;
    private int selectedRow, selectedColumn;
    private JPanel progressIndicator;
    private JProgressBar progressBar;
    private SwingWorker worker;
    
    protected Document.Collection documents;
    protected Document currentDocument;
    protected String documentDate;
    protected class SegmentsTable extends Hashtable<String, Document.SegmentList>
    {
    }
    protected SegmentsTable segmentsInDocuments;
    
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
        menuItems = new Hashtable<String, JMenuItem>();
        buttons   = new Hashtable<String, JButton>();
        buildGUI(window);
        window.setVisible(true);
        
        snowballFile = null;
        currentDocument = null;
        worker = null;
        if (args.length == 1) {
            loadCollection(new File(args[0]));
            visualize(documents);
        }
        else {
            documents = null;
        }
        
        selectedRow    = -1;
        selectedColumn = -1;
        
        syncInterface();
    }
    
    protected void visualize(Document.Collection documents)
    {
        window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        if (worker != null) worker.cancel(true);
        worker = new AnnotatorThread();
        worker.execute();
    }
    
    protected class AnnotatorThread extends SwingWorker<SegmentsTable, Void> {
        public AnnotatorThread() {
        }
        
        public SegmentsTable doInBackground() {
            progressBar.setValue(0);
            progressBar.setMaximum(documents.size());
            progressIndicator.setVisible(true);
            long start, stop;
            start = System.currentTimeMillis();
            SegmentsTable segmentsInDocuments = new SegmentsTable();
            int progress = 0;
            for (Document document : documents) {
                if (isCancelled()) break;
                progressBar.setValue(++progress);
                Document.SegmentList segments = annotateDocument(document);
                segmentsInDocuments.put(document.getIdentifier(), segments);
            }
            documents.sort();
            stop = System.currentTimeMillis();
            double elapsed = stop - start;
            java.text.NumberFormat format = java.text.NumberFormat.getInstance(java.util.Locale.ENGLISH);
            format.setMaximumFractionDigits(3);
            if (documents.size() > 0) System.err.println("Processed " + documents.size() + " documents in " + format.format(elapsed / 1000) + "s\nAverage " + format.format(elapsed / 1000 / documents.size()) + "s per document");
            
            return segmentsInDocuments;
        }
        
        public void done() {
            try {
                segmentsInDocuments = get();
                visualizeEntities(documents, segmentsInDocuments);
                view.setDividerLocation(view.getWidth() - view.getDividerSize() - (int) radar.getPreferredSize().getWidth() - 1);
                showDocument();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (java.util.concurrent.ExecutionException e) {
                e.printStackTrace();
            }
            catch (java.util.concurrent.CancellationException e) {
                // OK. This was cancelled on request.
            }
            finally {
                progressIndicator.setVisible(false);
                window.setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    
    protected boolean loadCollection(File directory)
    {
        documents = new Document.Collection(directory);
        segmentsInDocuments = null;
        snowballFile = null;
        
        if (null == ocr)      ocr      = new OCR(documents);
        if (null == metadata) metadata = new Metadata(documents);
        if (null == tagger)   tagger   = new SimpleRegexp(documents);
        
        return true;
    }
    
    protected void visualizeEntityTypes(Document.Collection documents, SegmentsTable segmentsInDocuments)
    {
        if (null == documents || null == segmentsInDocuments) return;
        
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
    protected void visualizeEntities(Document.Collection documents, SegmentsTable segmentsInDocuments)
    {
        if (null == documents || null == segmentsInDocuments) return;
        
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
    
    protected File requestFile(String key, boolean forWriting, String filterDescription, String... extensions)
    {
        return requestFile(key, forWriting, JFileChooser.FILES_ONLY, filterDescription, extensions);
    }
    
    protected File requestDirectory(String key)
    {
        return requestFile(key, false, JFileChooser.DIRECTORIES_ONLY, null);
    }
    
    protected File requestFile(String key, boolean forWriting, int mode, String filterDescription, String... extensions)
    {
        if (null == key) key = "defaultCollection";
        String lastCollectionLoaded = prefs.get(key, ".");
        
        File file = null;
        boolean retry = true;
        while (retry) {
            //JFileChooser fileChooser = new JFileChooser(lastCollectionLoaded);
            //if (lastCollectionLoaded != "." && JFileChooser.DIRECTORIES_ONLY == mode) {
            //    fileChooser.changeToParentDirectory();
            //}
            file = new File(lastCollectionLoaded);
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(file);
            if (mode != 0) fileChooser.setFileSelectionMode(mode);
            if (filterDescription != null) {
                fileChooser.setFileFilter(new FileNameRegexpFilter(FileNameRegexpFilter.FILENAME_EXTENSION, filterDescription, extensions));
            }
            int returnValue;
            if (forWriting) returnValue = fileChooser.showSaveDialog(window);
            else            returnValue = fileChooser.showOpenDialog(window);
            if (JFileChooser.APPROVE_OPTION == returnValue) {
                String fileName = fileChooser.getSelectedFile().getAbsolutePath();
                if (mode == JFileChooser.DIRECTORIES_ONLY) {
                    fileName += "/.";// :-)
                }
                prefs.put(key, file.getAbsolutePath());
            }
            else {
                file = null;
            }
            
            if (forWriting && file != null && file.exists()) {
                int result = ask("The file exists. Overwrite?",
                                 "Overwrite",
                                 "Use another name",
                                 "Cancel");
                switch (result) {
                case JOptionPane.YES_OPTION:
                    retry = false;
                    break;
                case JOptionPane.NO_OPTION:
                    file = null;
                    break;
                case JOptionPane.CANCEL_OPTION:
                    file = null;
                    retry = false;
                    break;
                }
            }
            else {
                retry = false;
            }
        }
        
        return file;
    }
    
    protected int ask(String question, String yes, String no, String cancel)
    {
        String[] options = { yes, no, cancel };
        return JOptionPane.showOptionDialog(window,
                                            question,
                                            "HistoRadar",
                                            JOptionPane.YES_NO_CANCEL_OPTION,
                                            JOptionPane.QUESTION_MESSAGE,
                                            null,
                                            options, options[2]);
    }
    
    public void actionPerformed(ActionEvent event)
    {
        String command = event.getActionCommand();
        if ("quit".equals(command)) {
            System.exit(0);
        }
        else if ("load-collection".equals(command)) {
            File directory = requestDirectory("defaultCollection");
            if (directory != null) {
                loadCollection(directory);
                visualize(documents);
            }
            else {
                System.err.println("Collection loading cancelled");
            }
        }
        else if ("load-snowball".equals(command)) {
            if (null == snowballFile) snowballFile = requestFile("snowball", false, "HTML files", "html", "htm", "xhtml");
            if (snowballFile != null) {
                try {
                    snowballView.load(snowballFile);
                    snowballView.setModified(false);
                }
                catch (java.io.IOException e) {
                    System.err.println(e);
                }
            }
        }
        else if ("save-snowball".equals(command)) {
            if (null == snowballFile) {
                snowballFile = requestFile("snowball", true, "HTML files", "html", "htm", "xhtml");
            }
            if (snowballFile != null) {
                try {
                    snowballView.save(snowballFile);
                    snowballView.setModified(false);
                }
                catch (java.io.IOException e) {
                    System.err.println(e);
                }
            }
        }
        else if ("save-snowball-as".equals(command)) {
            File newSnowballFile;
            newSnowballFile = requestFile("snowball", true, "HTML files", "html", "htm", "xhtml");
            if (newSnowballFile != null) {
                try {
                    snowballView.save(newSnowballFile);
                    snowballFile = newSnowballFile;
                    snowballView.setModified(false);
                }
                catch (java.io.IOException e) {
                    System.err.println(e);
                }
            }
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
            Radar.ActionEvent radarEvent = (Radar.ActionEvent) event;
            if (columnLabels != null && rowLabels != null) {
                switch (radarEvent.getAction()) {
                case SCREEN_CLICK:
                    selectedRow    = radarEvent.getRow();
                    selectedColumn = radarEvent.getColumn();
                    showDocument(documents.get(selectedColumn));
                    searchBox.setText(columnLabels.get(selectedRow));
                    documentView.search(searchBox.getText());
                    updateSearchButtons();
                    break;
                case SCREEN_MOUSEOVER:
                    radar.setLabel(rowLabels.get(radarEvent.getColumn()) + ", " + columnLabels.get(radarEvent.getRow()));
                    break;
                case SCREEN_LEAVE:
                    if (selectedRow >= 0 && selectedColumn >= 0) {
                        radar.setLabel(rowLabels.get(selectedColumn) + ", " + columnLabels.get(selectedRow));
                    }
                    else {
                        radar.setLabel(" ");
                    }
                    break;
                default:
                    System.err.println("unexpected action in Radar.ActionEvent: " + radarEvent.getAction());
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
        else if ("open-homepage".equals(command)) {
            String url = "http://historadar.googlecode.com/";
            try {
                Class<?> desktop = Class.forName("java.awt.Desktop");
                java.net.URI homepage = java.net.URI.create(url);
                desktop.getDeclaredMethod("browse", new Class[] {java.net.URI.class} )
                    .invoke(desktop.getDeclaredMethod("getDesktop")
                            .invoke(null),
                            new Object[] {homepage}
                            );
            }
            catch (Exception ignore) {
                java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(url), null);
                JOptionPane.showInputDialog(window,
                                            "You can copy the address to your web browser",
                                            "HistoRadar's website",
                                            JOptionPane.PLAIN_MESSAGE,
                                            null, null,
                                            url);
            }
        }
        else if ("cancel".equals(command)) {
            if (worker != null) worker.cancel(true);
        }
        else if ("search-next".equals(command)) {
            documentView.moveToNextMatch();
        }
        else if ("search-previous".equals(command)) {
            documentView.moveToPreviousMatch();
        }
        else if ("add-to-snowball".equals(command)) {
            snowballView.addEntry(currentDocument, documentView, searchBox.getText());
            documentView.moveToNextMatch();
        }
        else {
            System.err.println("Error: Unexpected command: '" + command + "'");
            System.exit(1);
        }
        
        syncInterface();
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
        
        currentDocument = document;
        
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
        
        window.setCursor(Cursor.getDefaultCursor());
    }
    
    private void buildGUI(JFrame frame)
    {
        documentView = new DocumentView();
        documentView.setEditable(false);
        documentView.setContentType("text/html");
        
        snowballView = new SnowballView();
        snowballView.setEditable(true);
        snowballView.setContentType("text/html");
        
        radar = new Radar();
        radar.addActionListener(this);
        radar.setActionCommand("radar");
        
        metadataPane = new JPanel();
        documentPane = new JPanel();
        documentPane.setLayout(new BorderLayout());
        
        JPanel documentHeaderPane;
        documentHeaderPane = new JPanel();
        documentHeaderPane.setLayout(new BoxLayout(documentHeaderPane, BoxLayout.PAGE_AXIS));
        metadataPane.setLayout(new BoxLayout(metadataPane, BoxLayout.PAGE_AXIS));
        
        JToolBar searchPane;
        searchPane = new JToolBar();
        searchPane.setFloatable(false);
        searchPane.setRollover(false);
        searchPane.setLayout(new BoxLayout(searchPane, BoxLayout.LINE_AXIS));
        searchPane.add(searchBox = new JTextField());
        searchBox.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyReleased(java.awt.event.KeyEvent e)
                {
                    documentView.search(searchBox.getText());
                    updateSearchButtons();
                }
                
                public void keyTyped(java.awt.event.KeyEvent e)
                {
                    // Ignore
                }
                
                public void keyPressed(java.awt.event.KeyEvent e)
                {
                    // Ignore
                }
                
            });
        button(searchPane, "search-next",     "↓").setToolTipText("Next match");
        button(searchPane, "search-previous", "↑").setToolTipText("Previous match");
        button(searchPane, "add-to-snowball", " + ").setToolTipText("Add to snowball");
        button(searchPane, "save-snowball", "Save").setToolTipText("Save snowball");
        
        documentHeaderPane.add(metadataPane);
        documentHeaderPane.add(searchPane);
        documentPane.add(documentHeaderPane, BorderLayout.NORTH);
        
        tabbedDocumentView = new JTabbedPane();
        tabbedDocumentView.addTab("Document", null, new JScrollPane(documentView), "The annotated document");
        tabbedDocumentView.addTab("Snowball", null, new JScrollPane(snowballView), "Collected notes");
        documentPane.add(tabbedDocumentView, BorderLayout.CENTER);
        
        view = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, documentPane, radar);
        view.setOneTouchExpandable(true);
        view.setResizeWeight(0.0);
        
        frame.getContentPane().add(view);
        progressIndicator = new JPanel();
        progressIndicator.setLayout(new BorderLayout());
        progressBar = new JProgressBar();
        progressIndicator.add(progressBar, BorderLayout.CENTER);
        JButton button = new JButton("Cancel");
        button.addActionListener(this);
        button.setActionCommand("cancel");
        progressIndicator.add(button, BorderLayout.EAST);
        progressIndicator.setVisible(false);
        frame.getContentPane().add(progressIndicator, BorderLayout.NORTH);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menu, submenu;
        JMenuItem item;
        ButtonGroup group;
        
        menu = new JMenu("File");
        menuItem(menu, "load-collection", "Load collection");
        menu.addSeparator();
        menuItem(menu, "load-snowball", "Load snowball");
        menuItem(menu, "save-snowball", "Save snowball");
        menuItem(menu, "save-snowball-as", "Save snowball as...");
        menu.addSeparator();
        menuItem(menu, "quit", "Quit");
        menuBar.add(menu);
        
        menu = new JMenu("NER");
        menu.setToolTipText("Named Entity Recognition");
        menuGroup(menu,
                  new String[][] {
                      {"ner-engine-simple-regexp", "Simple regexp (built-in)"},
                      {"ner-engine-opennlp-maxent", "OpenNLP Maxent"},
                      {"ner-engine-stanford", "Stanford NER"}
                  });
        menuBar.add(menu);
        
        menu = new JMenu("Radar");
        menuGroup(menu,
                  new String[][] {
                      {"radar-image-fuzzy", "Fuzzy"},
                      {"radar-image-sharp", "Sharp"}
                  });
        menu.add(submenu = new JMenu("Zoom"));
        menuGroup(submenu,
                  new String[][] {
                      {"radar-image-zoom-1", "1×"},
                      {"radar-image-zoom-2", "2×"},
                      {"radar-image-zoom-3", "3×"},
                      {"radar-image-zoom-4", "4×"},
                      {"radar-image-zoom-10", "10×"},
                      {"radar-image-zoom-20", "20×"}
                  });
        
        menuBar.add(menu);
        
        menu = new JMenu("About");
        menuItem(menu, null, "Version 2010-04-13 17:26");
        menuItem(menu, "open-homepage", "http://historadar.googlecode.com");
        menuBar.add(menu);
        
        frame.setJMenuBar(menuBar);
    }
    
    private JMenuItem menuItem(JMenu menu, String command, String label)
    {
        JMenuItem item;
        menu.add(item = new JMenuItem(label));
        if (command != null) {
            item.addActionListener(this);
            item.setActionCommand(command);
            menuItems.put(command, item);
        }
        
        return item;
    }
    
    private void menuGroup(JMenu menu, String[][] items)
    {
        ButtonGroup group = new ButtonGroup();
        JMenuItem item;
        String command, label;
        for (int i = 0; i < items.length; ++i) {
            command = items[i][0];
            label   = items[i][1];
            menu.add(item = new JRadioButtonMenuItem(label));
            if (command != null) {
                item.addActionListener(this);
                item.setActionCommand(command);
                menuItems.put(command, item);
            }
            group.add(item);
        }
    }
    
    private JButton button(JComponent component, String command, String label)
    {
        JButton button;
        component.add(button = new JButton(label));
        if (command != null) {
            button.addActionListener(this);
            button.setActionCommand(command);
            buttons.put(command, button);
        }
        
        return button;
    }
    
    private void updateSearchButtons()
    {
        JButton button;
        button = buttons.get("search-next");
        if (button != null) {
            button.setEnabled(documentView.hasNextMatch());
        }
        button = buttons.get("search-previous");
        if (button != null) {
            button.setEnabled(documentView.hasPreviousMatch());
        }
    }
    
    private void syncInterface()
    {
        String taggerClass;
        if (tagger != null) taggerClass = tagger.getClass().getName();
        else                taggerClass = "";
        
        String command;
        if ("org.matracas.historadar.nlp.ner.SimpleRegexp".equals(taggerClass)) {
            command = "ner-engine-simple-regexp";
        }
        else if ("org.matracas.historadar.nlp.ner.OpenNlpNER".equals(taggerClass)) {
            command = "ner-engine-opennlp-maxent";
        }
        else if ("org.matracas.historadar.nlp.ner.StanfordNER".equals(taggerClass)) {
            command = "ner-engine-stanford";
        }
        else {
            System.err.println("Error: unknown tagger class: " + taggerClass);
            command = "";
        }
        
        if (menuItems.containsKey(command)) {
            menuItems.get(command).setSelected(true);
        }
        
        switch (radar.getZoom()) {
        case 1:
            command = "radar-image-zoom-1";
            break;
        case 2:
            command = "radar-image-zoom-2";
            break;
        case 3:
            command = "radar-image-zoom-3";
            break;
        case 4:
            command = "radar-image-zoom-4";
            break;
        case 10:
            command = "radar-image-zoom-10";
            break;
        case 20:
            command = "radar-image-zoom-20";
            break;
        default:
            command = "";
        }
        
        if (menuItems.containsKey(command)) {
            menuItems.get(command).setSelected(true);
        }
        
        if (radar.getFuzzy()) command = "radar-image-fuzzy";
        else                  command = "radar-image-sharp";
        
        if (menuItems.containsKey(command)) {
            menuItems.get(command).setSelected(true);
        }
        
        command = "save-snowball";
        if (menuItems.containsKey(command)) {
            menuItems.get(command).setEnabled(snowballView.getModified());
        }
        if (buttons.containsKey(command)) {
            buttons.get(command).setEnabled(snowballView.getModified());
        }
        
        updateSearchButtons();
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
