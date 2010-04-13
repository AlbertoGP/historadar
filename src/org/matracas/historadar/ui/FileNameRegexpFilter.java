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

package org.matracas.historadar.ui;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FileNameRegexpFilter extends javax.swing.filechooser.FileFilter
{
    public static final Pattern FILENAME_EXTENSION = Pattern.compile("([^.]*)$");
    protected String description;
    protected java.util.Set<String> values;
    protected Pattern pattern;
    
    public FileNameRegexpFilter(String patternString, String description, String... values)
    {
        this(Pattern.compile(patternString), description, values);
    }
    
    public FileNameRegexpFilter(Pattern pattern, String description, String... values)
    {
        this.pattern = pattern;
        this.description = description;
        this.values = new java.util.TreeSet<String>();
        for (int i = 0; i < values.length; ++i) this.values.add(values[i]);
    }
    
    public boolean accept(java.io.File file)
    {
        if (file.isDirectory()) return true;
        
        Matcher matcher = pattern.matcher(file.getAbsolutePath());
        if (matcher.find()) {
            String match = matcher.group(1);
            if (match != null) return values.contains(match);
            else return false;
        }
        else return false;
    }
    
    public String getDescription()
    {
        return description;
    }
    
}
