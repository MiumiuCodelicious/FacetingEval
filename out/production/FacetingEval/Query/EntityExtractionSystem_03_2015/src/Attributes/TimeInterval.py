'''
Created on May 17, 2012

@author: fifille
'''
import re
from Attribute import Attribute

class TimeInterval(Attribute):
    
    def init(self):
        self.specific_year = re.compile('(BC|AD|FY)?(1[0-9][0-9][0-9])|(20[0-9][0-9])|((^|[^$])[5-9][0-9]$)|((^|[^$])0[0-9]$)')
        self.time_units = re.compile('(minute)|(hour)|(day)|(week)|(month)|(season)|(year)|'
                                     '(century)|(spring)|(summer)|(fall)|(autumn)|(winter)|'
                                     '([Mm]illenium)|(quarter)|(decade)')
        self.holidays = re.compile('(holiday$)|(vacation$)|([Cc]hristmas$)|([Tt]hanks [Gg]iving)|'
                                   '([Nn]ew [Yy]ear)')
        self.months = re.compile('([Jj]anuary)|([Ff]ebruary)|([Mm]arch)|([Aa]pril)|([Mm]ay)|([Jj]une)|'
                                 '([Jj]uly)|([Aa]ugust)|([Ss]eptember)|([Oo]ctober)|([Nn]ovember)|'
                                 '([Dd]ecember)|([Jj]an)|([Ff]eb)|([Mm]ar)|([Aa]pr)|([Jj]un)|'
                                 '([Jj]ul)|([Aa]ug)|([Ss]ept?)|([Oo]ct)|([Nn]ov)|([Dd]ec)')
        self.week_days = re.compile ('([Mm]onday)|([Tt]uesday)|([Ww]ednesday)|([Tt]hursday)|([Ff]riday)|'
                                     '([Ss]aturday)|([Ss]unday)')
        self.days = re.compile('[1-3]?[0-9](st|nd|th)$')
        self.abstract = re.compile('(time)|([W|w]hen)|(recent)|(period)|(span)')
        self.timePeriodIN = re.compile('([Ff]rom)|([Ss]ince)|([Bb]efore)|([Aa]fter)|([Bb]etween)|'
                                 '([Dd]uring)|([Aa]round)|([Oo]ver)|([Pp]ast)|([Ff]uture)|([Pp]eriods?)|([Yy]ears)|'
                                 '([Bb]efore)|([Aa]fter)|([Ll]ast)|([Pp]revious)|(century)|(millennium)|(decade)|'
                                 '([Tt]ime)|([Rr]ecent)|([Ss]pan)|([Ii]nterval)|-')
        self.timePointIn = re.compile('([Ii]n)|([Aa]t)')
        self.decades = re.compile('^([1-9]0s)|(2[0-9]{3}s)$')
        self.timeList, self.typeList, self.timeEntity = [], [], []
        self.timeType = []    
    
    def __basicMatch(self):
#        query = ' '.join([self.posdict[i][0] for i in self.posdict])
#        print query, '-------'
        
        basiclist, timelist = [], []
        
        for i in self.posdict:    
            specific_year = re.search(self.specific_year, self.posdict[i][0])
            unit = re.search(self.time_units, self.posdict[i][0])
            holiday = re.search(self.holidays, self.posdict[i][0])
            week = re.search(self.week_days, self.posdict[i][0])
            month = re.search(self.months, self.posdict[i][0])
            #days = re.search(self.days, self.posdict[i][0])
            abs = re.search(self.abstract, self.posdict[i][0])
            decades = re.search(self.decades, self.posdict[i][0])
            
            if specific_year != None or unit != None or holiday != None or week != None or \
                abs != None or decades != None or month != None:
                basiclist.append(i)
                
        for b in basiclist[:-1]:
            check = 0
            for j in range(b, basiclist[basiclist.index(b)+1]):
                if not re.match(r'CD|DT|NN|N|NNS|NP|IN|TO|JJ|CC|-', self.posdict[j][2] ):
                    check = check + 1
            if check == 0:
                timelist.append([b, basiclist[basiclist.index(b)+1]])
            else:
                timelist.append([b, b])
        if len(basiclist)>0:    
            timelist.append([basiclist[-1], basiclist[-1]])
            
#            if specific_year != None:    print 'year: ', self.posdict[i][0]
#            if unit != None:    print 'unit: ', self.posdict[i][0]
#            if holiday != None: print 'holiday: ', self.posdict[i][0]
#            if week != None:    print 'week: ', self.posdict[i][0] 
#            #if days != None:    print 'days: ', self.posdict[i][0]
#            if abs != None:     print 'abs: ',  self.posdict[i][0] 
        return timelist
    
    
    def __biggestTime(self):
        timelist = self.__basicMatch()
        TimeList = []
        combine = [-1, -1]
        for i in range(1, len(timelist)):
            tindex = len(timelist) -i
            if timelist[tindex][0] > timelist[tindex-1][1]:
                if timelist[tindex][0] != combine[0]:
                    TimeList.append(timelist[tindex])
                else:
                    TimeList.append(combine)
                    combine = [-1, -1]
            elif timelist[tindex][0] == timelist[tindex-1][1]:
                if combine[0] == -1 and combine[1] == -1:
                    combine = [ timelist[tindex-1][0], timelist[tindex][1] ] 
                else:
                    combine = [timelist[tindex-1][0], combine[1]]
        if combine not in self.timeList and combine[0]!=-1 and combine[1]!=-1:    TimeList.append(combine)
        if len(timelist) > 1 and timelist[0][0] != combine[0]:
            TimeList.append(timelist[0])
        return TimeList
                
    def timeChunk(self):
        # brute force
        TimeList = self.__biggestTime()
        
        if len(TimeList) == 0:
            return [], []
        
        for tindex in range(len(TimeList)):
            low , hi = TimeList[tindex]
            if low != -1 and hi != -1:
                for i in range (1, min(6, low)):
                    if re.match('IN|TO|DT|JJ|CD|CC|-', self.posdict[low-i][1]):
                        TimeList[tindex][0] = low -i
                    else:
                        break
        # detele overlapped time entities
        TimeList = self.deleteOverlap(TimeList)
          
        return TimeList
        

    def timeAttr(self):
        TimeList = self.timeChunk()  
        TypeList = [2]*len(TimeList)
        TimeType = [0]*len(self.EntityChunks) + TypeList
    
        for index, en in enumerate(self.EntityChunks):
            chunkword = " ".join( w[1][0] for w in en )
            if (re.search(self.specific_year, chunkword) or re.search(self.time_units, chunkword) or re.search(self.holidays, chunkword) \
                or re.search(self.months, chunkword) or re.search(self.week_days, chunkword) or re.search(self.days, chunkword) ):
                TimeType[index] = 2
                if re.search(self.timePeriodIN, chunkword): TimeType[index] = 1

        if len(TimeList[0]) == 0:
            return  TimeType
    
        else:       
            # now delete time entities in the original generated entity list
            for low, high in TimeList:
                self.deleteTimeEntities(low, high)
                entity = []
                for i in range(low, high+1):
                    entity.append([i, self.posdict[i]])
                self.timeEntity.append(entity)
                
        for tindex in range(len(TimeList)):
            low, hi = TimeList[tindex]
            if re.search(self.timePeriodIN, " ".join( [self.posdict[w][0] for w in range(low, hi+1)] )) \
                or re.search(self.timePeriodIN, self.posdict[max(low-1, 0)][0]):  
                TypeList[tindex] = 1 # 1 for time period 
                    
        self.EntityChunks = self.EntityChunks + self.timeEntity
#        print len(self.timeType), len(self.EntityChunks), len(self.timeEntity)
        return TimeType
        
            
    def deleteOverlap(self, givenlist):
        '''
        Give any list, this function will delete the entities that are overlapped or included in other entities
        '''
        return_list = []
        for en in givenlist:
            for resten in givenlist[givenlist.index(en):]:
                if en[0] <= resten[0] and en[1] >= resten[1]:
                    # include
                    resten[0] = en[0]
                    resten[1] = en[1]
                    if en not in return_list: return_list.append(en)
                elif en[0] == resten[1]:
                    # conjunct
                    resten[1] = en[1]
                    en[0] = resten[0]
                    if en not in return_list: return_list.append(en)
                else:
                    if en not in return_list: return_list.append(en)
        return return_list
    

        
    def deleteTimeEntities(self, low, high):
        rmvenlist = []
        rmvrootlist = []
        tnklist = []
        for en in self.EntityChunks:
            if en[0][0]>=low and en[-1][0]<=high:
                rmvenlist.append(en)
                rmvrootlist.append(self.EntityRoots[self.EntityChunks.index(en)])
            elif en[0][0]<=low and en[-1][0]>=high:
                rmvenlist.append(en)
                rmvrootlist.append(self.EntityRoots[self.EntityChunks.index(en)])
            elif en[-1][0]<low and en[-1][0] <high:
                en = en[:-(en[-1][0]-low+1)]
            elif  en[0][0]>high and en[0][0]>low:
                en = en[(high-en[0][0]+1):]
        for rmv in rmvenlist:
            self.EntityChunks.remove(rmv)
        for rmv in rmvrootlist:
            self.EntityRoots.remove(rmv)
        
                
    
if __name__ == '__main__':
    
    # ------------------------------------------------------------------------------------------------------------------------
    from TreeStructure.ReadTree import *
    filepath = '../../../../test.txt'     
    single_tree = open(filepath, 'r') 
    readtree = ReadTree(single_tree)

    from EntityExtract import EntityExtract
    entity = EntityExtract(readtree.ptree, readtree.posdict, readtree.verbdict, readtree.noundict)
    entity.EntityChunker(entity.ptree.root)
    entity.onlyNP()
    entity.truncate()
    entity.keepMinimal()
    # ------------------------------------------------------------------------------------------------------------------------

    time = TimeInterval(entity.ptree, entity.posdict, entity.EntityChunks, entity.EntityRoots)
    TimeType = time.timeAttr()
    for e in time.EntityChunks:    print e
    for t in TimeType: print t