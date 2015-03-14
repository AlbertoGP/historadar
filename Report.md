# Introduction #

The first problem when studying a collection of documents is where to start.
[HistoRadar](http://code.google.com/p/historadar/) intends to give an interactive map of a given text document collection, showing information extracted from it using Natural Language Processing techniques.

The highlight is on changes in the information: we assume that the most interesting points to start studying are those where people or other entities appeared or stopped being mentioned, where their stated opinions on a given subject changed, etc.

We work with the plain text transcription of the documents.
We chose the [British Cabinet Papers](http://www.nationalarchives.gov.uk/cabinetpapers/) collection, and some aspects of our implementation like the date extraction are somewhat optimized for those documents. However the program can work with any plain text documents and is easily adaptable to them. For instance, the date extraction assumes dates in the English language, but simply replacing the regular expressions would detect other date formats, and the Named Entity Recognition libraries we use can load models for other languages apart from the English ones we use in this case.

We designed the program to support the historian's "snowball" method for working with document collections as described in the user interface section.

# Content #

  * [Acquisition](ReportTextAcquisition.md) of the plain text transcription.
  * [Cleaning-up](ReportCleanUp.md) the provided plain text transcription from some typos and OCR errors.
  * [Extracting metadata](ReportMetadata.md), most importantly the document date in a normalized format suitable for machine processing.
  * [Extracting the list of attendants](ReportAttendantList.md) in the British Cabinet meeting minutes.
  * [Named Entity Recognition](ReportNER.md), specifically persons and geographical locations mentioned in the minutes.
  * [Information Extraction](ReportInformationExtraction.md), specifically presence/absence of known persons in the government, statements by members of the cabinet.
  * [User interface](ReportUserInterface.md) designed to support the historian's workflow.
  * [Evaluation and conclusion](ReportConclusion.md), what we achieved and ideas for future work.
  * [Division of work](ReportWorkDivision.md) among the team members.