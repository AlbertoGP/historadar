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

package org.matracas.historadar.nlp;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.matracas.historadar.Document;

/**
 * OCR analyzer and corrector.
 */
public class OCR
{
    protected Document.Collection collection;
    
    public OCR(Document.Collection collection)
    {
        this.collection = collection;
        // TODO: as test linguistic analysis of the collection to help identify and correct the OCR errors
    }
    
    /**
     * Correct the OCR errors found in the given document.
     *
     * @param document the one to be corrected
     * @return Count of corrections done
     */
    public int correctDocument(Document document)
    {
        int correctionCount;
        String text, correctedText;
        text = document.getPlainText();
        
        // TODO: correct text according to findings from the linguistic analysis of the document collection
        
        /* Get proper years:
         * get rid of one or more spaces between single digits to get proper year representations (1 9 1 8 --> 1918)
         */
        Pattern pattern; 
        Matcher matcher;
        
        correctedText = text;
        
        // Get rid of spaces within words. Unfortunately also concatenates (some) words --> splitter needed.
        pattern = Pattern.compile("(\\b\\S)\\b\\s\\b");
        matcher = pattern.matcher(text);
        correctedText = matcher.replaceAll("$1");
        
        /*
        // Correct 1 9 1 8 to 1918
        pattern = Pattern.compile("([1-9])[^0-9]?([0-9])[^0-9]?([0-9])[^0-9]?([0-9])");
        matcher = pattern.matcher(text);
        correctedText = matcher.replaceAll("$1$2$3$4");
        
        // Correct "\d \d" to "\d\d"
        pattern = Pattern.compile("(\\d)\\D?(\\d)");
        matcher = pattern.matcher(correctedText);
        correctedText = matcher.replaceAll("$1$2");
        // And once again:
        pattern = Pattern.compile("(\\d)\\D?(\\d)");
        matcher = pattern.matcher(correctedText);
        correctedText = matcher.replaceAll("$1$2");
        */
        
        correctionCount = 0;
        
        document.setPlainText(correctedText);
        return correctionCount;
    }
    
    
	/** Returns the Levenshtein Distance of two strings. Copied from <code>http://www.merriampark.com/ldjava.htm</code>.
	 * @param string1
	 * @param string2
	 * @return Levenshtein distance
	 */
	public static int getLevenshteinDistance(String string1, String string2) {
		if (string1 == null || string2 == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		/*
		 * The difference between this impl. and the previous is that, rather
		 * than creating and retaining a matrix of size s.length()+1 by
		 * t.length()+1, we maintain two single-dimensional arrays of length
		 * s.length()+1. The first, d, is the 'current working' distance array
		 * that maintains the newest distance cost counts as we iterate through
		 * the characters of String s. Each time we increment the index of
		 * String t we are comparing, d is copied to p, the second int[]. Doing
		 * so allows us to retain the previous cost counts as required by the
		 * algorithm (taking the minimum of the cost count to the left, up one,
		 * and diagonally up and to the left of the current cost count being
		 * calculated). (Note that the arrays aren't really copied anymore, just
		 * switched...this is clearly much better than cloning an array or doing
		 * a System.arraycopy() each time through the outer loop.)
		 * 
		 * Effectively, the difference between the two implementations is this
		 * one does not cause an out of memory condition when calculating the LD
		 * over two very large strings.
		 */

		int n = string1.length(); // length of s
		int m = string2.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		int p[] = new int[n + 1]; // 'previous' cost array, horizontally
		int d[] = new int[n + 1]; // cost array, horizontally
		int _d[]; // placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			t_j = string2.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++) {
				cost = string1.charAt(i - 1) == t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left
				// and up +cost
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1]
						+ cost);
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		return p[n];
	}
    
}
