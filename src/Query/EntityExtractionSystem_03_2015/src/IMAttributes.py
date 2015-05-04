'''
Created on Nov 25, 2014

@author: Zhuo (Jewel) li

This module take in the complete list of query-entity pairs, and their attribute matrix.

And match each query-entity pair with their decision tree classification.

Then produce a single vector of attribute about the entire query, for its Intended Message:
Does the query want a Rank of a particular (or more) entity? Or a Rank of all entities? Or a trend? Etc. 

'''

import sys, re

class IntendedMessageAttributes():
    
    def __init__(self, queryEntityPairs, entityXYAttributes):
        # This class needs XY axes attributes to be extracted first. 
        