# Introduction #

The central idea of HistoRadar is to show the places where there are changes in the information gathered from the document collection. For this, the first step is to extract information about [named entities](ReportNER.md) from the text.

# Implementation #

There was time left only for the most basic information extraction required by the radar screen.

# Algorithm sketch #

  * Planned
    1. Pick relevant entities, e.g. by clustering according to correlation
    1. Find relations among them, e.g. distance in the text

  * Implemented
    1. Collect all entities mentioned in the document collection
    1. Sort them alphabetically
    1. Count how many times they appear in each document
    1. Normalize by dividing each entity count by the maximum count
    1. Map normalized count logarithmically to color intensity in the radar screen: a single mention of an entity is a very different event from no mention, but the difference between, say, 30 and 40 mentions of a given entity is much smaller.

# References #

(libraries used, related papers, ...)

# Team members responsible for this #

  * Alberto González Palomo