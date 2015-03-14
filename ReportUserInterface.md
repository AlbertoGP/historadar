# Introduction #

We intended this application to be actually usable by historians.

This requires a user interface robust enough for people not interested in the technical details, and tailored to the typical historian's workflow.

In our discussions during the seminar with the historians we identified the following basic workflow when working with a document collection:

  * Read documents in the collection
  * Collect interesting topics
  * **Snowball** method:
    * Read again, collecting notes about selected topics
    * Add findings to "snowball"
    * Follow leads
    * Iterate

# Workflow #

A document collection is built by putting the plain text version of the documents to be studied into a directory/folder. It can then be loaded with the menu option "File→Load collection", selecting that directory. All files with names ending in ".txt" will be loaded by the application.

The interface is split in two main parts: left is the document area, and right is the radar area.

The radar shows a high-level view of the whole document collection, and is the method used for navigating it.

![http://wiki.historadar.googlecode.com/hg/images/screenshot-historadar-sarrail-radar-click.png](http://wiki.historadar.googlecode.com/hg/images/screenshot-historadar-sarrail-radar-click.png)

The "radar screen" represents all entities found in the documents using [Named Entity Recognition](ReportNER.md). The horizontal axis represents time, with the timeline above the radar screen. The vertical axis represents the entities. Therefore, vertical lines are synchronic slices, and horizontal ones diachronic.

Clicking on the radar screen loads the corresponding document, and copies the entity at that point to the search box above the document, with the effect of the document scrolling to the first appearance. The arrow buttons next to the search box select the next and previous appearance respectively.

There are two documents in tabs. One is the current source document being explored, and another is the "snowball" where the working historian writes down annotations about his findings.

![http://wiki.historadar.googlecode.com/hg/images/screenshot-historadar-sarrail-add-to-snowball-cropped.png](http://wiki.historadar.googlecode.com/hg/images/screenshot-historadar-sarrail-add-to-snowball-cropped.png)

Clicking on the button labelled with a plus sign "+" adds an entry to the snowball.

Each entry contains first the document identifier as a web link to the file, some document metadata, some indications of the current location in the document (search query and result index, character position, percentage of total characters and page), a citation around the current position for context, and an empty field for additional annotations.

![http://wiki.historadar.googlecode.com/hg/images/screenshot-historadar-sarrail-snowball-tab.png](http://wiki.historadar.googlecode.com/hg/images/screenshot-historadar-sarrail-snowball-tab.png)

The snowball is an HTML document that can be edited at any time, then saved using the "Save" button or menu option, and viewed in a web browser, imported into a word processor, etc.

The snowball file can be loaded to continue working on it later, even after modifying it with external tools.

# Options #

The Named Entity Recognition engine to be used can be selected in the "NER" menu.

![http://wiki.historadar.googlecode.com/hg/images/screenshot-historadar-ner-menu-stanford-ner-cropped.png](http://wiki.historadar.googlecode.com/hg/images/screenshot-historadar-ner-menu-stanford-ner-cropped.png)

The initial one is the built-in "simple regexp" one we implemented with regular expressions. It just searches for occurrences of some words like a few country names and some job titles we found in the British Cabinet papers.

The result of the Named Entity Recognition is a list of annotated segments (http://wiki.historadar.googlecode.com/hg/javadoc/org/matracas/historadar/Document.Segment.html). Other NER engines use similar data structures that we translate to our own so that all of them present a common interface to our application.

In the screenshot we have selected the Stanford Natural Language Processing Group's [Conditional Random Field NER](http://nlp.stanford.edu/software/CRF-NER.shtml) engine. The other one we have integrated so far is OpenNLP's [Maximum Entropy classifier](http://maxent.sourceforge.net/). Details about this can be found in the [NER section](ReportNER.md).

# Team members responsible for this #

  * Alberto González Palomo