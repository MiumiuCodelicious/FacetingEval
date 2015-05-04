'''
Created on May 8, 2012

@author: fifille
'''
import re
import threading

class Attribute(threading.Thread):
    
    def __init__(self, ptree, posdict, EntityChunks, EntityRoots):
        threading.Thread.__init__(self)
        self.ptree, self.posdict, self.EntityChunks, self.EntityRoots = ptree, posdict, EntityChunks, EntityRoots
        self.init()
    
    def init(self):
        return 

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
                if len(paren_stack) > 0:    paren_stack.pop()
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


    def verbModifier(self, word):
        if re.match(r'VBG', word[1][1]):
            terms = self.getTerms(word[1][2])
            if len(terms) == 3 and terms[1] == '/' and re.match(r'(^NP$)|(^NP\[nb\]$)|(^N$)', terms[-1]):
                return 1;   
            else:   return 0
        elif re.match(r'VB(D|N)', word[1][1]):
            terms = self.getTerms(word[1][2])
            if len(terms) == 3 and terms[1] == '\\' and re.match(r'(^NP$)|(^NP\[nb\]$)|(^N$)', terms[-1]):
                return 1
            else:   return 0
        else:   return 0   
        
        
    def run(self):
        return