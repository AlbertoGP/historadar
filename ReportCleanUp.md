# Introduction #

The raw text we extracted from the available PDF files contains OCR errors.<br>
Several errors are not important or having influence on the further processing,<br>
and we haven't found a proper way to deal with them anyway.<br>
But some cleaning-up was necessary for the specific data we wanted to be able to access: Date extraction and name extraction.<br>
<br>
For our project we could identify two main issues which were occurring across all the documents:<br>
<ol><li>Spaces within words, dates, and abbreviations<br>
</li><li>Split compound proper nouns when they were aligned in columns in the PDF, but in the OCR-processed text appeared as continuous lines.</li></ol>


<h1>Approaches</h1>
<ol><li>The first issue - split words - could be solved rather easily by the use of several subsequently applied regular expressions. <br>
</li><li>The second issue - split compound proper names - should turn out to be a little trickier: The OCR software would treat everything as a single line, spanning all two columns. As long as all parts of a name (and title, honor) are not separated through a line break, they won't lead to any mistakes, but it is mostly the case that a name entity is longer than a column's width.<br>
It turned out that the OCR actually does detect columns if they are reasonably long, but then again not throughout the whole collection, so the problem remained, but for a smaller subset.</li></ol>


<h1>Implementation</h1>
<h2>1. Split words</h2>
The Regular Expression we used is <code>"(\b\S)\b\s\b"</code>, which gets replaced by the first group (the part in brackets). This simple approach concatenates all single non-space characters (also punctuation) to the following non-whitespace element. <br>
An unwanted side effect is that single characters (like the article "a") get concatenated to the next word, too. A word splitting approach which helps a little here is that we separate words where the second one starts with a capital letter, but we cannot yet separate two not-all-capital-letters words where the second one starts in lowercase. We hoped the OpenNLP tools would provide such an option, but this is not the case.<br>
We remove spaces within abbreviations, too, and separate folling words from them, and we also separate numbers from words. <br>

The resulting treatment of the text looks as follows: <br>
<pre><code>// Get rid of spaces within words. Unfortunately also concatenates (some) words --&gt; splitter needed.<br>
pattern = Pattern.compile("(\\b\\S)\\b\\s\\b");<br>
matcher = pattern.matcher(text);<br>
correctedText = matcher.replaceAll("$1");<br>
correctionCount += matcher.groupCount();<br>
<br>
// Separate words with capital letters in them.<br>
pattern = Pattern.compile("([a-z])([A-Z0-9][a-z0-9]*?)");<br>
matcher = pattern.matcher(correctedText);<br>
correctedText = matcher.replaceAll("$1 $2");<br>
correctionCount += matcher.groupCount();<br>
<br>
// Separate ("prefix"-)numbers from words.<br>
pattern = Pattern.compile("(\\b\\d)([A-Za-z]{2,}?)");<br>
matcher = pattern.matcher(correctedText);<br>
correctedText = matcher.replaceAll("$1 $2");<br>
correctionCount += matcher.groupCount();<br>
<br>
// Spaces within abbreviations<br>
pattern = Pattern.compile("\\b([A-Z])\\s[.]\\s");<br>
matcher = pattern.matcher(correctedText);<br>
correctedText = matcher.replaceAll("$1.");<br>
correctionCount += matcher.groupCount();<br>
<br>
// Separate (compound) abbreviations from following words<br>
pattern = Pattern.compile("[.](\\w)(?![.])");<br>
matcher = pattern.matcher(correctedText);<br>
correctedText = matcher.replaceAll(". $1");<br>
correctionCount += matcher.groupCount();<br>
</code></pre>

<h2>2. Column-crossing</h2>
There were fewer cases occurring than we had expected. <br>
Our idea to get a proper list of all names would be to put all lines of the two columns of all documents into a list, then concatenate the single words cross-wise to get possible compound names and match the resulting compounds with the rest of the list.<br>
<br>
For example, the following picture shows the original text in the PDF file, with part of the OCRed text marked:<br>
<img src='http://www.stud.uni-saarland.de/~s9jobrau/coli/historadar/historadar-ocr-columns.png' />

You can see clearly that the columns have not been detected by the OCR software, because the lines are processed not column-wise.<br>
The resulting text is<br>
<pre><code>The Right Hon. LORD ROBERT CECIL, Vice-Admiral SIR H. F. OLIVER, K.C.B.,<br>
K . C . , M.P., Acting Secretary of State for M.V.O., Deputy Chief of the Naval<br>
Foreign Affairs. Staff. (Minutes 3 to 9.)<br>
The Right Hon. the EARL OP DERBY. K . G . , Major the Hon. WALDORP A.STOR, M.P.<br>
G . C . V . O . , C.B., Secretary of State for (Minute 15.)<br>
War. (Minutes 1 to 9.) The Right Hon. R. MUNRO, K.C., M.P.,<br>
Major-General SIR G. M. W. MACDONOGH, Secretary for Scotland. (Minutes 15<br>
[…]<br>
</code></pre>

After cleaning it, we get<br>
<pre><code>The Right Hon. LORD ROBERT CECIL, Vice-Admiral SIR H. F. OLIVER, K.C.B.,<br>
K.C., M.P., Acting Secretary of State for M.V.O., Deputy Chief of the Naval<br>
Foreign Affairs. Staff. (Minutes 3 to 9.)<br>
The Right Hon. the EARL OP DERBY. K.G., Major the Hon. WALDORP A. STOR, M.P.<br>
G.C.V.O., C.B., Secretary of State for (Minute 15.)<br>
War. (Minutes 1 to 9.) The Right Hon. R. MUNRO, K.C., M.P.,<br>
Major-General SIR G. M. W. MACDONOGH, Secretary for Scotland. (Minutes 15<br>
</code></pre>


Actually it should look more like<br>
<pre><code>The Right Hon. LORD ROBERT CECIL,                Vice-Admiral SIR H. F. OLIVER, K.C.B.,<br>
K.C., M.P., Acting Secretary of State for        M.V.O., Deputy Chief of the Naval<br>
Foreign Affairs.                                 Staff. (Minutes 3 to 9.)<br>
The Right Hon. the EARL OP DERBY. K.G.,          Major the Hon. WALDORP A. STOR, M.P.<br>
G.C.V.O., C.B., Secretary of State for           (Minute 15.)<br>
War. (Minutes 1 to 9.)                           The Right Hon. R. MUNRO, K.C., M.P.,<br>
Major-General SIR G. M. W. MACDONOGH,            Secretary for Scotland. (Minutes 15<br>
[…]<br>
</code></pre>

or, more accurately, like this:<br>
<pre><code>The Right Hon. LORD ROBERT CECIL,         <br>
K.C., M.P., Acting Secretary of State for <br>
Foreign Affairs.                          <br>
The Right Hon. the EARL OP DERBY. K.G.,   <br>
G.C.V.O., C.B., Secretary of State for    <br>
War. (Minutes 1 to 9.)                    <br>
Major-General SIR G. M. W. MACDONOGH,     <br>
[…]<br>
Vice-Admiral SIR H. F. OLIVER, K.C.B.,    <br>
M.V.O., Deputy Chief of the Naval         <br>
Staff. (Minutes 3 to 9.)                  <br>
Major the Hon. WALDORP A. STOR, M.P.      <br>
(Minute 15.)                              <br>
The Right Hon. R. MUNRO, K.C., M.P.,      <br>
Secretary for Scotland. (Minutes 15       <br>
</code></pre>

The plan was to compare this text with documents like the following, which have a different constellation of attendees:<br>
<img src='http://www.stud.uni-saarland.de/~s9jobrau/coli/historadar/historadar-ocr-columns-2.png' />
<pre><code>The Right Hon, the EARL of DERBY, K.G., Admiral SIR J . R. JELLICOE, G.C.B.,<br>
G.C.V.O., C.B., Secretary of State for O.M., G.C.V.O., First Sea Lord of the<br>
War. Admiralty (for Minutes 4 to 9 ) .<br>
Major-General Sir G. M. W. MACDONOGH, The Right Hon. SIR A. STANLEY, M.P.,<br>
K.C.M.G., C.B., Director of Military President of the Board of Trade (for<br>
Intelligence (for Minutes 4 to 9).^ Minutes 1 to 3 ) .<br>
</code></pre>

After cleaning, we have:<br>
<pre><code>The Right Hon, the EARL of DERBY, K.G., Admiral SIR J.R. JELLICOE, G.C.B.,<br>
G.C.V.O., C.B., Secretary of State for O.M., G.C.V.O., First Sea Lord of the<br>
War. Admiralty (for Minutes 4 to 9 ) .<br>
Major-General Sir G. M. W. MACDONOGH, The Right Hon. SIR A. STANLEY, M.P.,<br>
K.C.M.G., C.B., Director of Military President of the Board of Trade (for<br>
Intelligence (for Minutes 4 to 9).^ Minutes 1 to 3 ) .<br>
</code></pre>

Should be:<br>
<pre><code>The Right Hon, the EARL of DERBY, K.G.,          Admiral SIR J.R. JELLICOE, G.C.B.,<br>
G.C.V.O., C.B., Secretary of State for           O.M., G.C.V.O., First Sea Lord of the<br>
War.                                             Admiralty (for Minutes 4 to 9 ) .<br>
Major-General Sir G. M. W. MACDONOGH,            The Right Hon. SIR A. STANLEY, M.P.,<br>
K.C.M.G., C.B., Director of Military             President of the Board of Trade (for<br>
Intelligence (for Minutes 4 to 9).^              Minutes 1 to 3 ) .<br>
[…]<br>
</code></pre>
… respectively:<br>
<pre><code>The Right Hon, the EARL of DERBY, K.G.,<br>
G.C.V.O., C.B., Secretary of State for <br>
War.                                   <br>
Major-General Sir G. M. W. MACDONOGH,  <br>
K.C.M.G., C.B., Director of Military   <br>
Intelligence (for Minutes 4 to 9).^    <br>
[…]<br>
Admiral SIR J.R. JELLICOE, G.C.B.,    <br>
O.M., G.C.V.O., First Sea Lord of the <br>
Admiralty (for Minutes 4 to 9 ) .     <br>
The Right Hon. SIR A. STANLEY, M.P.,  <br>
President of the Board of Trade (for  <br>
Minutes 1 to 3 ) .                    <br>
</code></pre>

You might see that when we want to get the name `The Right Hon, the EARL of DERBY, K.G.,<br>
G.C.V.O., C.B., Secretary of State for War.`, we face the problem that it is separated by<br>
<code>Major the Hon. WALDORP A.STOR, M.P. (Minute 15.)</code> and by <code>Admiral SIR J . R. JELLICOE, G.C.B., O.M., G.C.V.O., First Sea Lord of the</code> in the second section.<br>
<br>
So, if we compare a sufficient amount of documents, we might be able to tell that the two parts ("lines") always occur together and therefore belong to the same name/entity.<br>
<br>
<br>
<h1>Open Issues</h1>
<ul><li>Split words: Separate concatenated words, where the beginning of the next word is not indicated by a capital letter or another sign (digit etc.)</li></ul>

<h1>References</h1>

The library we used is the Java built-in Regular Expression tool set.<br>
For testing purposes we found the <a href='http://myregexp.com/'>MyRegexp.com</a> site very useful.<br>
<br>
<h1>Team members responsible for this</h1>

<ul><li>Johannes Braunias<br>
</li><li>Souhail Bouricha