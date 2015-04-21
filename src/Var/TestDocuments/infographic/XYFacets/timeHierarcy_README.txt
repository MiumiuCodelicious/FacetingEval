{\rtf1\ansi\ansicpg1252\cocoartf1344\cocoasubrtf720
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx720\tx1440\tx2160\tx2880\tx3600\tx4320\tx5040\tx5760\tx6480\tx7200\tx7920\tx8640\pardirnatural

\f0\fs24 \cf0 1.for some files , in the allXExp filed , it has terms such as season,quarter ,month ,week, day, date, hour and age. Those terms have equavalent and high termfreq. However, in the allX , it only has years. (Example: set1_54-1_exp,set1_61_exp\
\
2.Some files does not contain allXExp values. Therefore ,their X facet values are currently none(for example set_5_22\
\
3.Period, decades,quarters,seasons are intuitively calculated according to the date in allX. Once a file has period,decades,quarters or seasons in its allXExp field, those time durations will be calculated.\
\
4. Once the allXExp has \'91time\'92 in it, all the date information in allX will be shown. And the terms \'92year\'92 or \'91month\'92 will not be used in facet_x any more\
\
5. Once the allXExp does not contain \'91time\'92 in it, according to which allXExp has, certain date format will be returned. For example, year, then the specific year that in allX will be return. }