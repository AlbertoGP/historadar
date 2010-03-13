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

package org.matracas.historadar.nlp.ner;


import org.matracas.historadar.Document;
import org.matracas.historadar.nlp.NER;

import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.util.StringUtils;

/**
 * Named entities extracted from a document.
 *
 */
public class StanfordNER extends NER
{
    protected Document.PatternTable patterns;

    public StanfordNER(/*Document.Collection collection*/)
    {
    }

    /**
     * Get the entities from a given document,
     * based on the linguistic analysis of the document collection.
     *
     * @param document the document where we look for entities
     */
    public Document.SegmentList getEntities(Document document)
    {
        Document.SegmentList segments = new Document.SegmentList();
        String plainText = document.getPlainText();

        // TODO: extract entities form plain text
        String serializedClassifier = "lib/StanfordNER/classifiers/ner-eng-ie.crf-3-all2008.ser.gz";



      AbstractSequenceClassifier classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);

 
        String result = classifier.classifyWithInlineXML(plainText);



        
        return segments;
    }

}
