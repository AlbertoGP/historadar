# Introduction #

The metadata included in the original PDF file refers to the file creation (time of scanning), not to the date of when the paper was written.<br>
Therefore we have to extract it from the <a href='ReportCleanUp.md'>OCR text</a> content.<br>
Our approach is to match the date included in the phrase like <br>
<code>"held in London at 10, Downing Street, S.W., on Tuesday, April 24, 1917, at 11-30 A.M"</code>, <br>
which is used in the same style in many documents, and convert it into a typed representation (year, month, day, time). This is an example for the desired date part:<br>
<img src='http://www.stud.uni-saarland.de/~s9jobrau/coli/historadar/historadar-ocr-columns-5.png' />

<h1>Implementation</h1>

We first match the whole date part, and from that we extract the date details.<br>
The weekday is mentioned, and we could use it as a means to verify the recognition of the day, month, and year part of the date.<br>
One issue was the different formats used for the day of month:<br>
<i>1st January</i> and <i>January 1</i> are the two occurring variants. Some other variants we have to deal with:<br>
<pre><code>Tuesday, 7th August, 1 9 4 5 , at 5 - 0 p.m. Street, S.W. 1<br>
Thursday, lst. November, 1 9 4 5 , at 1 1 a.m.<br>
Tuesday, lst January, 1 9 4 6 , at 1 1 a.m.<br>
December 9, 1916, at 11-30 A.M.<br>
March 1 , 1 9 1 7 , at 1 1 - 3 0 A . M .<br>
Tuesday, June 5, 1917, at 11*30 A.M.<br>
Tuesday, January 1 , 1 9 1 S , at 1 1 * 3 0 A.M.<br>
Monday, April .1, 1913, at 1130 A.M.<br>
Monday, July 1, 1018, at 12 noun.<br>
Octdber 1, 1913, at 1T30 A.M.<br>
Thursday,3 , 1 9 1 9 , at 12<br>
Tuesday, July 1 , 1 9 1 9 , at 1 1 * 3 0 A.M.<br>
Friday, June 8, 1917, at IF..30 a.m.<br>
Friday, August 15, 1919, at 1 1 3 0 A.M<br>
Friday, June 8, 1917, at IF..30 a.m.<br>
Tuesday, January 2, 1940, at 11 A . M<br>
WEDNESDAY, 21st JUNE, 1939, at 10030 a,m<br>
MONDAY, 24th APRIL, 1939 at 5.6 p.m<br>
WEDNESDAY, 15th MARCH, 1939, at 11.0 a.m<br>
WEDNESDAY, 22nd MARCH, 1959, at 10.0 a.m<br>
</code></pre>

After applying the OCR corrections, we get <br>
<pre><code>Tuesday, 7 th August, 1945 , at 5 - 0p.m. Street, S.W. 1<br>
Thursday, lst. November, 1945 , at 11a.m.<br>
Tuesday, lst January, 1946 , at 11a.m.<br>
December 9, 1916, at 11-30 A.M.<br>
March 1 , 1917 , at 11 - 30A . M.<br>
Tuesday, June 5, 1917, at 11*30 A.M.<br>
Tuesday, January 1 , 191S , at 11 * 30A.M.<br>
Monday, April . 1, 1913, at 1130 A.M.<br>
Monday, July 1, 1018, at 12 noun.<br>
Octdber 1, 1913, at 1T30 A.M.<br>
Thursday,3 , 1919 , at 12<br>
Tuesday, July 1 , 1919 , at 11 * 30A.M.<br>
Friday, June 8, 1917, at IF.. 30 a.m.<br>
Friday, August 15, 1919, at 1130A. M<br>
Friday, June 8, 1917, at IF.. 30 a.m.<br>
Tuesday, January 2, 1940, at 11 A. M<br>
WEDNESDAY, 21st JUNE, 1939, at 10030 a,m<br>
MONDAY, 24th APRIL, 1939 at 5. 6p. m<br>
WEDNESDAY, 15th MARCH, 1939, at 11. 0a. m<br>
WEDNESDAY, 22nd MARCH, 1959, at 10. 0a. m<br>
</code></pre>

The quite well working set of regular expressions we found is (note: we use the options <code>Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.CANON_EQ</code>. For a reference to the regular expressions please refer to the corresponding Java documentation on <a href='http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html'>regular expressions</a> and to the an online testing <a href='http://www.myregexp.com/'>tool</a>):<br>
For the whole date (we need to make sure that we don't match a date occuring later in the text, which could happen if the date has OCR errors and wouldn't match in the first place):<br>
<code>held.{1,60}?((\w+?day|\w+?uary|march|april|may|june|july|august|\w+?mber|\w+?ober)\b.+?(?:[.,] ?m[.,]?|oon))</code>

For the day of week:<br>
<code>\w+day</code>

For the month:<br>
<code>\w+ry|\w+ber|\w+rch|\w+ril|may|\wune|\w+ly|\w+ust</code>

For the year:<br>
<code>(?:\w+ry|\w+ber|\w+rch|\w+ril|may|\wune|\w+ly|\w+ust).+([12]\d{3})</code>

For the day of month:<br>
<code>(\d\d?).{1,5}(?:\w+ry|\w+ber|\w+rch|\w+ril|may|\wune|\w+ly|\w+ust)|(?:\w+ry|\w+ber|\w+rch|\w+ril|may|\wune|\w+ly|\w+ust)\s{1,2}(\d\d?)</code>

For the hour and minute: <br>
<code>at\s+(\d{1,2})[- *o\\.]{0,4}(\d*) ?(a|p|n)?</code>

For the time of day (before or after noon):<br>
<code>(a\..*|p\..*|noon.*)</code>

The month names got converted to numeric values afterwards.<br>
<br>
As you can see from the examples above, there are cases where simple OCR correction cannot improve the results dramatically, and what is left over are for examples time stamps like <br>
<pre><code>1T30 A.M.<br>
IE.30 a.m.<br>
10o30<br>
</code></pre>

What we do in our project is to just analyse the data as best we can and leave the empty data fields (i.e. hour and minute) empty.<br>
<br>
<h1>References</h1>

We also had considered another option, namely to use a date tagger. <br>
Examples with <a href='http://platypus.ics.mq.edu.au:8180/~mpawel/demos/dante/dante.jsp'>Dante</a> yielded poor results for the continuous text, and has not been longer available after giving it a long input string.<br>
<a href='http://www.free-ocr.com/'>Online OCR processing</a>

<h1>Team members responsible for this</h1>

<ul><li>Johannes Braunias