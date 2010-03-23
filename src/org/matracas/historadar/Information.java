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

import java.util.Map;
import java.util.Hashtable;

/**
 * Predicates about objects.
 *
 */
public class Information
{
    public class Object
    {
        protected String identifier;
    }
    
    public class Predicate
    {
        protected Object a, b;
        protected String identifier;
    }
    
    Map<String, Object> objects;
    Map<String, Predicate> predicates;
    
    public Information()
    {
        objects    = new Hashtable<String, Object>();
        predicates = new Hashtable<String, Predicate>();
    }
    
}
