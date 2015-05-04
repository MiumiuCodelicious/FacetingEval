import re

entity_read = open("../../Entity/New_XYEntities.txt")
attribute_read = open("../../Entity/New_XYAttributes.txt")

entities = entity_read.readlines()


'''
   ------ below is to read in human labeled XY axis class value --------
'''
def XYlabel():
    instance_counter = 0
    for line in attribute_read.readlines():
        if line != "\n" and not re.match("@", line):
    #         print instance_counter, entities [instance_counter ]
    #         print line
            XYclassmatcher = re.search('\([XYN]\)', entities[instance_counter])
            classlabel= entities[instance_counter][XYclassmatcher.start()+1: XYclassmatcher.end()-1]
            print line[:-1] + ","+classlabel
            instance_counter += 1
            
        else:
            print line[:-1]
            
        
        
'''
   ------- below is to read in human labeled FX entity value  ---------
'''
def FXlabel():
    instance_counter = 0
    entity_number = entityNumber("/Users/divinityelle/Desktop/Dropbox/Entity/New_Query_Entity_Number.txt")
    queryIM = allQueryIM("/Users/divinityelle/Desktop/Dropbox/Entity/AnnotatedIgAttr_new.dat")
    message_array = []
    for query in entity_number.keys():
        message_array += [queryIM[query-1].replace("\n", "")] * entity_number[query]
    for line in attribute_read.readlines() :
        if len(line) >1 and not re.match("@", line):
    #         print instance_counter, entities [instance_counter ]
    #         print line
            FXclassmatcher = re.search('\(F\)', entities[instance_counter])
            XYclassmatcher = re.search('\([XYN]\)', entities[instance_counter])
            XYclasslabel= entities[instance_counter][XYclassmatcher.start()+1: XYclassmatcher.end()-1]
            if FXclassmatcher:
                classlabel = entities[instance_counter][FXclassmatcher.start()+1: FXclassmatcher.end()-1]
            else:
                classlabel = 'NF'
            print line[:-1] + ","+XYclasslabel + "," + message_array[instance_counter] + "," + classlabel
            instance_counter += 1
            
        else:
            print line[:-1]
            

def allQueryIM(queryfile):
    '''
      This function reads in the hand labeled query IM (by Matt) for all 324 queries collected by Matt.
      In file "/Users/divinityelle/Desktop/Dropbox/Entity/AnnotatedIgAttr_new.dat"
    '''
    fullQIM = {}
    qid = 0
    qhandler = open(queryfile, 'r')
    for line in qhandler.readlines():
        fullQIM[qid] = line.split(",")[-1]
        qid += 1
    return fullQIM

def entityNumber(query_entity_number_file):
    '''
        /Users/divinityelle/Desktop/Dropbox/Entity/New_Query_Entity_Number.txt
    '''
    entityNumber = {}
    fhandler = open(query_entity_number_file)
    for line in fhandler.readlines():
        entityNumber[int(line.split(",")[0])] = int(line.split(",")[-1])
    return entityNumber
        
FXlabel()
