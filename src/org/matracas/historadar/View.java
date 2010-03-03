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

import org.matracas.historadar.Document;
import org.matracas.historadar.nlp.OCR;
import org.matracas.historadar.nlp.Metadata;

/**
 * Main class of HistoRadar, with the GUI application.
 */
public class View implements ActionListener
{
    private JFrame window;
    private JTextPane documentView;
    private HeatMap heatMap;
    private JPanel metadataPane;
    
    public View()
    {
        JFrame.setDefaultLookAndFeelDecorated(false);
        window = new JFrame("HistoRadar View");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800,600);
        buildGUI(window);
        window.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e)
    {
        String command = e.getActionCommand();
        if ("quit".equals(command)) {
            System.exit(0);
        }
        else if ("load-collection".equals(command)) {
            String lastCollectionLoaded = System.getProperty("historadar.defaultCollection");
            if (null == lastCollectionLoaded) lastCollectionLoaded = ".";
            JFileChooser fileChooser = new JFileChooser(lastCollectionLoaded);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fileChooser.showOpenDialog(window);
            if (JFileChooser.APPROVE_OPTION == returnValue) {
                File directory = fileChooser.getSelectedFile();
                System.setProperty("historadar.defaultCollection", directory.getAbsolutePath());
                Document.Collection documents = new Document.Collection(directory);
                Document document = documents.getDocumentIterator().next();
                
                // OCR correction
                OCR ocr = new OCR(documents);
                int corrections = ocr.correctDocument(document);
                System.err.println("Corrected " + corrections + " errors from the OCR text");
                documentView.setText(document.getPlainText());
                
                // Metadata extraction
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
                window.validate();
            }
            else {
                System.err.println("Collection loading cancelled");
            }
        }
        else {
            System.err.println("Error: Unexpected command: '" + command + "'");
            System.exit(1);
        }
    }
    
    private void buildGUI(JFrame frame)
    {
        documentView = new JTextPane();
        documentView.setEditable(false);
        documentView.setContentType("text/plain");
        
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
        documentPane.add(new JScrollPane(documentView));
        
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
    
    public static void main(String[] args)
    {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() 
                {
                    View view = new View();
                }
            });
    }
}
