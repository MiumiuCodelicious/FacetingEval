'''
Created on Mar 22, 2012

@author: fifille
'''

from TreeStructure.ReadFile import *

class Temp():
    '''
    Taken a parsed tree together with POS tags of a Which-N question
    '''
    def __init__(self, filepath):
        self.init(filepath)
        
    def init(self, filepath):
        # from ReadFile ------------------------------------------------------------
        self.input = ReadFile(filepath)
        self.ptree, self.posdict = self.input.ptree, self.input.posdict
        self.input.buildDict()
        self.verbdict, self.noundict = self.input.verbdict, self.input.noundict
        # from ReadFile ------------------------------------------------------------
    
    def getUltHead(self, tag):
        '''
         given a tag, what is the ultimate super tag casted to if all requirements satisfied  
         eg: (S[wq]/(S[dcl]\NP))/N
         ultimate head is S[wq] if first given N and then given (S[dcl]\NP)
        '''
        rec = self.getTerms(tag)
        while len(rec) > 1:
            rec = self.getTerms(rec[0]) 
        return rec
        
        
    def getTerms(self, tag):
        # get only the first level of terms. need to use recursively to get all terms
        paren_stack = []
        terms = []
        term = ''
        # parse tag at highest level, seperate into 2 parts
        for t in tag:
            if t == '(':
                if len(paren_stack) > 0 :
                    term = term + t   # don't want the outter layer parenthesis     
                paren_stack.append(t) 
            elif t == ')':
                paren_stack.pop()
                if len(paren_stack) > 0:
                    term = term + t         # don't want the outter layer parenthesis
            elif (t == '/' or t == '\\') and len(paren_stack) == 0:
                terms.append(term)
                terms.append(t)
                term = ''
            else:
                term = term + t
        # push the last  term gotten from last loop
        terms.append(term)
        return terms
                
    
            
if __name__ == '__main__':
    tmp= Temp('super.txt')
    print 'first level terms: ', tmp.getTerms('(S[wq]/(S[dcl]\NP))/N')
    print 'ultimate head: ', tmp.getUltHead('(S[wq]/(S[dcl]\NP))/N')