'''
Created on Mar 26, 2012

@author: fifille
'''
'''
This class is to model a template for What N ... ? questions
eg 1. What countries have GDP per capita less than $30,000 in 2001 ?
eg 2. what states have the highest percentage of southern baptists in the US ?

Template Main Idea:
    What (S[]/ (S[]\NP) ) / N
    The N is expecting entity on X axis, this N should be the right child of the parent node of Which
    "What" indicates countable entity
'''
from TempWhichN import *
from EntityExtract import *

class TempWhatN(TempWhichN):
    '''
    Taken a parsed tree together with POS tags of a Which-N question
    '''     

if __name__ == '__main__':
    wha = TempWhatN('wha.txt')
    print wha.identifySent() 
    regex = r'W|what'
    wha.getIndieVar(regex)