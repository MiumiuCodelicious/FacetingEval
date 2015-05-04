'''
Created on Jan 21, 2013

@author: ivanka li

This module take in the complete list of query-entity pairs. 

And get attributes to identify whether a query-entity pair is focused.

Use all entity XY attributes, plus the classification result of Axes and Intended Message

'''


from AxesDecisionResults import AxesDecisionResults
from IgAttributes import Annotations
import os, re

class FXAttributes():
    '''
        batch use: 
            * read in from Axes attribute file
            * for every query-entity pair, add attribute whether it's on X or Y axis
            * for every query, add attribute the type of its intended message 
        single query: 
            * read in Axes attribute matrix
            * read in Axes classification result array
            * read in Intended Message classification result array
    '''
    
    
    def __init__(self, axes_attr_path, XYDecision, IM_annotation_path):
        self.axesDecision = AxesDecisionResults(XYDecision)
        self.axesAttr(axes_attr_path)
        self.annotation = Annotations(IM_annotation_path)
        
        
        
    def axesAttr(self, axes_attr_path):
        if os.path.isfile(axes_attr_path):
            fhandler = open(axes_attr_path, 'r')
            lno = 1
            for line in fhandler.readlines():
                if not re.match("@", line) and len(line) > 1: 
                    self.attrList.append( map( lambda x: int(x), line.split(',')[:-1] ) )
                lno = lno +1
            fhandler.close()
        else:
            self.attrPath.append(axes_attr_path)
            
            
        
        
        
        
        