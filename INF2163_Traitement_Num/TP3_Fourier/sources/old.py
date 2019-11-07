# -*- coding: utf-8 -*-
"""
Created on Mon Sep 30 12:08:58 2019

@author: e1602246
"""

import math,cmath
import numpy as np
import matplotlib.pyplot as plt
from scipy import signal

t = np.linspace(0, 10, 500)
plt.plot(t, signal.square(1 * t))

def serieFourier (ordre, N) :
         
    R = np.zeros(N)  
    t = np.linspace(0,((N-1)/N)*2*(np.pi),N)     
    
    for i in range(N):
        x = t[i]           
        y = 0 
        
        for k in range(1,ordre+1):             
            terme = (2*(1-(-1)**k)*np.sin(k*x))/(k*(np.pi))             
            y = y + terme
            
        R[i]=y   
    return R 

#for i in range(1, 100, 3):
    #plt.plot(serieFourier(i, 100))