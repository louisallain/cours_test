# -*- coding: utf-8 -*-
"""
Created on Wed Sep 18 13:27:17 2019

Exos de manipulation d'array

@author: e1602246
"""

import numpy as np
import matplotlib.pyplot as plt
from numpy import random
import math

# Shape et dimensions de matrices
print('1.')
Ma = np.ones(shape=(4, 2))
Mb = np.ones(shape=(2, 3))

print(np.shape(Ma))
print(np.shape(Mb))

# Assign matrice
print('2.')
M = np.array([[1,2,5], [2,3,4]])
print(M)
print(M[0,1])
print(M[1,0])
print('3.')
print(M[0:1:1])