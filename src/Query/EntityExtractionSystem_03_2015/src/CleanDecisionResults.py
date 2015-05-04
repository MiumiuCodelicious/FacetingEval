'''
Created on Jan 9, 2013

@author: ivanka li

This module takes the printed out result of Leave-1-Query-Out cross validation from Decision Tree learning,

and store the results into a list of 190 elements corresponding to 190 queries, 

each element contains the list of entity-query judgement.

'''
import re

class CleanDecision():
    
#    def __init__(self, indeResultPath, depResultPath):
    def __init__(self, singleTreePath):
        self.resultList = []
        self.rangeList = []
        # have to call initInde first and read in X judgments
#        self.initInde(indeResultPath)
#        self.initDep(depResultPath)
        self.singleTreeResults(singleTreePath)
        
    def initInde(self, indeResultPath):
        # Read in X judgement 
        self.indeJudge = []
        fhandler = open(indeResultPath, 'r')
        for line in fhandler.readlines():
            self.entityRange(line)
            if re.match(re.compile('RUN NUMBER'), line):
                if len(self.indeJudge) >0:   self.resultList.append(self.indeJudge)
                self.indeJudge = []
            if re.match(re.compile('Instance:'), line):
                pred = re.search(re.compile('Prediction:'), line).end()
                gold = re.search(re.compile('Normal:'), line).end()
                if int( float( line[pred:pred+3] ) ) == 1:  self.indeJudge.append('x')
                else:   self.indeJudge.append('n')
        self.resultList.append(self.indeJudge)
        fhandler.close()
        
    def initDep(self, depResultPath):
        self.depJudge = []
        fhandler = open(depResultPath, 'r')
        for line in fhandler.readlines():
            if re.match(re.compile('RUN NUMBER'), line):
                qno = int(line[11:])
                if len(self.depJudge) >0 and qno <= 190: 
#                    print qno-1, '.\t',  self.depJudge
#                    print '--\t', self.resultList[qno-2]
                    for j in range( len(self.depJudge) ):
                        self.decideXY(self.resultList[qno-2][j], self.depJudge[j], qno, j)
                    self.depJudge = []
            if re.match(re.compile('Instance:'), line):
                pred = re.search(re.compile('Pred.:'), line).end()
                gold = re.search(re.compile('Gold:'), line).end()
                if int( float( line[pred:pred+3] ) ) == 1:  self.depJudge.append('y')
                else:   self.depJudge.append('n')   
#        print 190, '.\t',  self.depJudge
#        print '--\t', self.resultList[189]
        for j in range( len(self.resultList[189]) ):
            self.decideXY(self.resultList[189][j], self.depJudge[j], 191, j)
#        print "dep result", self.resultList
        fhandler.close()
        
     
    def decideXY(self, xj, yj, qno, j):
        if yj == 'y' and xj == 'n':
            self.resultList[qno-2][j] = 'y'    
        elif yj == 'y' and xj == 'x':
            if self.bestY(qno) > -1:
                if self.bestY(qno) == j:
                    self.resultList[qno-2][j] = 'y'
                else:
                    self.resultList[qno-2][j] = 'x'
            else:
                # I am making all x-y judgements as x, since s is majority. ------This might need to be fixed
                self.resultList[qno-2][j] = 'x'
                
        
    def singleTreeResults(self, singleTreePath):
        # Read in X and Y judgements from a single tree results 
        self.singleTJudge = []
        fhandler = open(singleTreePath, 'r')
        lo, hi = -1, -1
        for line in fhandler.readlines():
            if re.match(re.compile('RUN NUMBER'), line):
                if len(self.singleTJudge) >0:   self.resultList.append(self.singleTJudge)
                if lo != -1 and hi != -1: self.rangeList.append([lo, hi])
                lo, hi = -1, -1
                self.singleTJudge = []
            if re.match(re.compile('Instance:'), line):
                inst = re.search(re.compile('Instance:'), line).end()
                entityLo = re.search(re.compile('[0-9]+'), line[inst:]).start() + inst
                entityHi = re.search(re.compile('[0-9]+'), line[inst:]).end() + inst
#                print 'entityNo = ', line[ entityLo : entityHi ]
                if lo == -1: lo= int(  line[ entityLo : entityHi] ) -1
                hi = int( line[ entityLo : entityHi] ) -1
                pred = re.search(re.compile('Prediction:'), line).end()
                gold = re.search(re.compile('Normal:'), line).end()
                if int( float( line[pred:pred+3] ) ) == 1:  self.singleTJudge.append('x')
                elif int( float( line[pred:pred+3] ) ) == 0:   self.singleTJudge.append('n')
                elif int( float( line[pred:pred+3] ) ) == 2: self.singleTJudge.append('y')
        if len(self.singleTJudge) >0: self.resultList.append(self.singleTJudge)
        if lo != -1 and hi != -1: self.rangeList.append([lo, hi])
        fhandler.close()
    
    def singleTreeResults2(self, singleTreePath):
        # Read in X and Y judgements from a single tree results 
        self.singleTJudge = []
        fhandler = open(singleTreePath, 'r')
        lo, hi = -1, -1
        for line in fhandler.readlines():
            if re.match(re.compile('Query'), line):
                if len(self.singleTJudge) >0:   self.resultList.append(self.singleTJudge)
                if lo != -1 and hi != -1: self.rangeList.append([lo, hi])
                lo, hi = -1, -1
                self.singleTJudge = []
            if re.match(re.compile('1|0'), line):
                inst = re.search(re.compile('Instance:'), line).end()
                entityLo = re.search(re.compile('[0-9]+'), line[inst:]).start() + inst
                entityHi = re.search(re.compile('[0-9]+'), line[inst:]).end() + inst
#                print 'entityNo = ', line[ entityLo : entityHi ]
                if lo == -1: lo= int(  line[ entityLo : entityHi] ) -1
                hi = int( line[ entityLo : entityHi] ) -1
                pred = re.search(re.compile('Prediction:'), line).end()
                gold = re.search(re.compile('Normal:'), line).end()
                if int( float( line[pred:pred+3] ) ) == 1:  self.singleTJudge.append('x')
                elif int( float( line[pred:pred+3] ) ) == 0:   self.singleTJudge.append('n')
                elif int( float( line[pred:pred+3] ) ) == 2: self.singleTJudge.append('y')
                
    def bestY(self, qno):
        bestY = -1
        for xj in self.resultList[qno-1]:
            for yj in self.depJudge:
                if xj == 'n' and yj == 'y':
                    bestY = self.depJudge.index(yj)
        return bestY
    
    
    def entityRange(self, line):
        if re.match(re.compile('Instance'), line):
            self.rangeList.append( map( lambda x: int(x), re.findall(re.compile('[0-9]+'), line) ) )
        

if __name__ == "__main__":
    import sys
    
#    clean = CleanDecision('weka_result/Independent_Results.txt', 'weka_result/Dependent_Results.txt')
    clean = CleanDecision('../../Entity/AllAxisResults.txt')
    for i in range(len( clean.rangeList)):
        print i+1, clean.rangeList[i][0], clean.rangeList[i][1]
    