# Introduction #

We need to extract named-entities to be able to derive facts about them, at the very least whether they are present, and how many times in a document.

# Implementation #

Tried the Stanford NER System, didn't work because of missing libraries (might have been a classpath issue, see below). Spent a lot of time searching for and reading documentation.

Tried the OpenNlp NER System, didn't compile at first because of misconfigured classpath in the build.xml and missing libraries. It took some time to realize that the classpath caused the problem. After that, finding the missing libraries wasn't a big deal.

Once the OpenNlp classes were made available from within our source code, I tried to figure out how to use them correctly. Again spent a lot of time searching and reading documentation. Eventually came to the conclusion that the examples for the use of the Tokenizer, SentenceFinder and NameFinder classes I found were all wrong (The classes' constructors' argument types didn't match what was specified in the source code). Maybe the examples were just outdated. In general, I didn't find a lot of documentation for the OpenNlp toolkit. In the end I finally found the information I needed in the tools' API and was able to run the NameFinder over a document and find some names with it.

Providing the right segments for marking the names in the text is still a problem. The NameFinder returns the position of a name as two integers representing the first and last token of a name. Whitespaces are lost when the text is tokenized, so it is not possible to just count the letters in the tokens and thus get the first and last letter of a name in the text of a document.
There are ways to solve this problem though, for example moving around the segment in the original text a bit until the name exactly matches the part of text in the segment.

# Algorithm sketch #

> We just use a NE tagger (currently the OpenNlp one) and let it do whatever it is that it  does. Then we can count how often each NE appears in the processed document.

# References #

OpenNlp Toolkit: http://opennlp.sourceforge.net/

Stanford NamedEntityRecognizer: http://nlp.stanford.edu/index.shtml

# Team members responsible for this #

  * Uwe Boltz