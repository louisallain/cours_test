# -*- coding: utf-8 -*-
"""
Created on Tue Sep 17 08:51:04 2019

Compare les listes et les arrays.

@author: e1602246
"""

import numpy as np
from random import random
from operator import add
import time

n = int(input("Entrez un entier : "))
l1 = [random() for i in range(n)]
l2 = [random() for i in range(n)]

start = time.clock()
l3 = map(add, l1, l2)
end = time.clock()
print( end - start)

A1 = np.array(l1)
A2 = np.array(l2)
11
start = time.clock()    
A3 = A1 + A2
end = time.clock()
print( end - start)