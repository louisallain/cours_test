# -*- coding: utf-8 -*-
"""
Created on Mon Sep 30 12:08:58 2019

@author: e1602246
"""

import math,cmath
import numpy as np
import matplotlib.pyplot as plt
from scipy import signal
from scipy.fftpack import fft

# partie 1

"""
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
    
for i in range(1, 100, 3):
    plt.plot(serieFourier(i, 100))
"""
# partie 2
def genSin(N, f, fs):
    t = np.linspace(0, (N-1)/fs, N)
    x = np.sin(2*np.pi*f*t)
    return x

# q2.1 création sinusoide
fe = 200
N = 256
T = float(N-1)/fe
echantillons = np.zeros(N)
echantillons = genSin(N, 128, fe)
n = np.arange(N)
#plt.plot(n[0:50], echantillons[0:50])

# q2.3 calcul spectre
TFD = fft(echantillons) # spectre
A = np.absolute(TFD/N) # amplitude
An = A/A.max() # ampmlitude normalisée
P = np.angle(TFD/N) # phase normalisée

# q2.4 afficher le spectre
F = np.linspace(0, fe, N)
plt.plot(F, abs(TFD))


# q2.5 observation

# q2.6 modification de f et de fs puis observation

# q2.7 création signal
fe = 200
N = 256
t = np.linspace(0, (N-1)/fe, N)
pi = np.pi
x = 3*math.cos(50*pi*t) + 10*math.sin(300*pi*t) - math.sin(100*pi*t)
#X = fft(x)
#plt.plot(X)
"""
#q2.3
TFD = fft((t,s))

A = np.abs(TFD/256)

#q2.4
fs = 256
N = 256
F =np.linspace(0,fs,N)
plt.plot(F, np.abs(TFD)) 

plt.plot(t, s)
"""