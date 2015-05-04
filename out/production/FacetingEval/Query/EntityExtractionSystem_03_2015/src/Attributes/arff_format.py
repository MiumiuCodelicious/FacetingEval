



# file = open("../../../Entity/AllQueryEntities_Kelly.txt", 'r')
# for line in file.readlines():
#     if len(line) > 10:
#         print line[:-1]
#         
        
        
import re
specific_year = re.compile('(BC|AD|FY)?(1[0-9][0-9][0-9])|(20[0-9][0-9])|((^|[^$])[6-9][0-9]$)|(^|[^$])0[0-9]$)')

testq1 = "How has Men\s Warehouse percentage of same-store sales changed from BC2003 to BC2005% ?"
word1 = "BC1930"
word2 = "FY2007"
word3 = "8/06"
word4 = "98%"
print re.search(specific_year, word3)