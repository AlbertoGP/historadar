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
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.text.html.HTMLDocument;
import java.io.File;
import java.util.Iterator;

import org.matracas.historadar.Document;
import org.matracas.historadar.ui.HeatMap;
import org.matracas.historadar.nlp.OCR;
import org.matracas.historadar.nlp.Metadata;
import org.matracas.historadar.nlp.NER;
import org.matracas.historadar.nlp.ner.SimpleRegexp;

/**
 * Main class of HistoRadar, with the GUI application.
 */
public class View implements ActionListener
{
    private JFrame window;
    private JTextPane documentView;
    private JTextPane documentSourceView;
    private HeatMap heatMap;
    private JPanel metadataPane;
    
    protected Document.Collection documents;
    protected Document document;
    Document.SegmentList segments;
    
    public View(String[] args)
    {
        JFrame.setDefaultLookAndFeelDecorated(false);
        window = new JFrame("HistoRadar View");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800,600);
        buildGUI(window);
        window.setVisible(true);
        
        document = null;
        
        if (args.length == 1) {
            loadCollection(new File(args[0]));
            if (documents != null) {
                document = documents.getDocument(null);
            }
            showDocument();
        }
        else {
            documents = null;
        }
    }
    
    protected boolean loadCollection(File directory)
    {
        System.err.println("1");
        documents = new Document.Collection(directory);
        
        return true;
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
                String lastCollectionLoaded = System.getProperty("historadar.defaultCollection");
                if (null == lastCollectionLoaded) lastCollectionLoaded = ".";
                JFileChooser fileChooser = new JFileChooser(lastCollectionLoaded);
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = fileChooser.showOpenDialog(window);
                if (JFileChooser.APPROVE_OPTION == returnValue) {
                    File directory = fileChooser.getSelectedFile();
                        System.setProperty("historadar.defaultCollection", directory.getAbsolutePath());
                    loadCollection(directory);
                }
                else {
                    System.err.println("Collection loading cancelled");
                }
            }
            
            if (documents != null) showDocument();
        }
        else {
            System.err.println("Error: Unexpected command: '" + command + "'");
            System.exit(1);
        }
    }
    
    protected void annotateDocument()
    {
        if (null == document) {
            System.err.println("annotateDocument(): document is null");
            return;
        }
        
        // OCR correction
        System.err.println("Correcting OCR...");
        OCR ocr = new OCR(documents);
        int corrections = ocr.correctDocument(document);
        System.err.println("Corrected " + corrections + " errors from the OCR text");
        
        // Metadata extraction
        System.err.println("Extracting metadata...");
        Metadata metadata = new Metadata(documents);
        Metadata.Entries entries = metadata.getMetadata(document);
        Metadata.Values values;
        metadataPane.removeAll();
        values = entries.get(Metadata.title);
        if (values != null) {
            Iterator valueIterator = values.iterator();
            while (valueIterator.hasNext()) {
                metadataPane.add(new JLabel("Title: " + valueIterator.next()));
            }
        }
        values = entries.get(Metadata.date);
        if (values != null) {
            Iterator valueIterator = values.iterator();
            while (valueIterator.hasNext()) {
                metadataPane.add(new JLabel("Date: " + valueIterator.next()));
            }
        }
        metadataPane.add(new JLabel("Identifier: " + document.getIdentifier()));
        
        System.err.println("Tagging text...");
        NER tagger = new SimpleRegexp(documents);
        segments = tagger.getEntities(document);
    }
    
    protected void showDocument()
    {
        if (document == null) return;
        
        annotateDocument();
        
        String content;
        
        content = document.getXMLString(segments, document.getXMLVocabulary("html"));
        documentView.setText(content);
        ((HTMLDocument) documentView.getDocument()).getStyleSheet()
            .addRule("span { background:#FFAAAA; }");
        
        content = document.getXMLString(segments);
        documentSourceView.setText(content);
        
        System.err.println("Text set");
        
        window.validate();
    }
    
    private void buildGUI(JFrame frame)
    {
        documentView = new JTextPane();
        documentView.setEditable(false);
        documentView.setContentType("text/html");
        
        documentSourceView = new JTextPane();
        documentSourceView.setEditable(false);
        documentSourceView.setContentType("text/plain");
        
        heatMap = new HeatMap();
        heatMap.addActionListener(this);
        heatMap.setActionCommand("heat-map-click");
        
        metadataPane = new JPanel();
        JPanel documentPane;
        JScrollPane heatMapPane;
        documentPane = new JPanel();
        heatMapPane = new JScrollPane(heatMap);
        
        documentPane.setLayout(new BoxLayout(documentPane, BoxLayout.PAGE_AXIS));
        metadataPane.setLayout(new BoxLayout(metadataPane, BoxLayout.PAGE_AXIS));
        documentPane.add(metadataPane);
        
        JTabbedPane tabbedDocumentView = new JTabbedPane();
        tabbedDocumentView.addTab("Source", null, new JScrollPane(documentSourceView), "XML source of the annotated document");
        tabbedDocumentView.addTab("Document", null, new JScrollPane(documentView), "The annotated document");
        documentPane.add(tabbedDocumentView);
        
        JSplitPane view = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, documentPane, heatMapPane);
        view.setOneTouchExpandable(true);
        view.setResizeWeight(0.8);
        
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
