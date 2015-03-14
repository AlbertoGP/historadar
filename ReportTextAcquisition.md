# Source text acquisition #

The source collection we chose was the [British Cabinet Papers](http://www.nationalarchives.gov.uk/cabinetpapers/).

Those files are in PDF format, including both the scanned image and their own automatic transcription using OCR.
We extracted the text transcription and worked with the resulting text files.

It is interesting to note that the oldest documents dating from 1910 were typeset and printed for the British Government so they had good quality, while more recent documents were often typewritten occasionally with mistakes crossed-over and hand-written annotations and corrections which affected the OCR negatively, with the result of older documents being actually easier to work with than recent ones contrary to our expectations.

Most files contained several actual documents, so we needed to split them.

To find the first line of each collated document, we took the notice they have in their first pages:

`This Document is the Property of His Britannic Majesty Government`


Due to the high incidence of OCR errors, we took a probabilistic approach for matching that line of text.
Many OCR errors are due to local damage to the document, such as faded ink, stains, or interference from annotations or stamps. Therefore we assume that the error rate is not uniform in the notice, so some parts of it will match literally.

One example of an actual line with errors is the following:

`*iTfois Document is the Property ­of Eis Britannic Majesty^Goyernm^tX`

We build several patterns that match different parts of the notice, words in this case.

```
patterns = [
  re.compile(r"\bthis\b.*\bdocument\b.*\bproperty\b", re.I),
  re.compile(r"\bdocument\b.*\bproperty\b.*\bhis\b +\bbritannic\b", re.I),
  re.compile(r"\bproperty\b.*\bbritannic\b +\bmajesty\b", re.I),
  re.compile(r"\bdocument\b.*\bproperty\b.*\bmajesty\b", re.I),
  re.compile(r"\bthis\b +\bdocument\b.*\bgovernment\b", re.I),
  re.compile(r"\bproperty\b +\bof\b.*\bgovernment\b", re.I),
]
```

We compute the match score by counting the patterns that match, and dividing by the total number of patterns to normalize the result to the interval `[0, 1]`, and if the normalized score surpasses our threshold, we consider it a match.
In this case a threshold of `1/n` (triggered when the score is greater but not equal to it) seemed to work fine.
From 178 files we separated 2395, which works out to an average of around 16 documents per file but there was a high variance with some files only yielding one or two documents, and others up to 50.

# Team members responsible for this #

  * Alberto González Palomo