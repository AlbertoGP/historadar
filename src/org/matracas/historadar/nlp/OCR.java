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
        Pattern pattern = Pattern.compile("([1-9]).([1-9]).([1-9]).([1-9])");
        Matcher matcher = pattern.matcher(text);
        correctedText = matcher.replaceAll("$1$2$3$4");
        correctionCount = 0;
        
        document.setPlainText(correctedText);
        return correctionCount;
    }
    
}
