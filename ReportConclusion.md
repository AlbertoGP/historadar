# Evaluation and conclusion #

The linguistic analysis of the text yielded surprisingly poor results. Readily available Named-Entity Recognition libraries do not work as well as we expected.

The information we extract so far is just a count of mentions of each found named entity in the collection, for each document. While it does help exploring the collection for those entities, so many others are missing that we expect historians not to trust it: in this application, recall is more important than precision because the historian does not want to miss any potentially interesting event but having some false positives is tolerable as long as they are few relative to the number of true negatives.

Given the unreliability of automated linguistic analysis, we suspect that building the radar image from full text search results would be more useful in practice. The linguistic analysis could still be used for query expansion, where its shortcomings would have a lesser impact.

The best results of this application are in the user interface area, where we successfully automated some of the historian's workflow.
The automatic citation into the snowball document seems like a superior alternative to manual copy and paste.

## Future work ##

### Historical entity evolution tracking ###

Some entities change names with time, others split into pieces or combine with others. This is particularly obvious with countries.
We would like to track those changes to provide long-term evolution visualizations to the user.