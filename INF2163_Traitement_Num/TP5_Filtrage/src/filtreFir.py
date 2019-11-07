#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Oct 15 17:06:56 2018

@author: sylviegibet
"""

import numpy as np
from scipy import signal
import matplotlib.pyplot as plt

    
#######
# I) Filtre FIR
#######

#construction d'un filtre passe-bas, avec freq. de coupure 0.1 ;
numtaps = 41 #nombre de coef du filtre (impair)
#cutoff: liste de fréquences de coupure (une seule ici) ; 
#Ces freq. sont relatives à la freq d'échantillonnage

b1=signal.firwin(numtaps,cutoff=[0.1, 0.2], pass_zero=False,window='hann',nyq=0.5)

# Tracer la réponse impulsionnelle :
plt.figure(figsize=(10,5))
plt.stem(b1)
plt.xlabel("n")
plt.xlabel("b")
plt.grid()

#Réponse fréquentielle
w,H=signal.freqz(b1)

plt.figure()
plt.subplot(211)
plt.plot(w/(2*np.pi),20*np.log10(np.absolute(H)))
plt.xlabel("f/fe")
plt.ylabel("GdB")
plt.grid()
plt.subplot(212)      
plt.plot(w/(2*np.pi),np.unwrap(np.angle(H)))
plt.xlabel("f/fe")
plt.ylabel("phase")
plt.grid()


#######
#II) Filtre IIR
#######

b3,a3 = signal.iirfilter(N=2,Wn=[0.1*2],btype="lowpass",ftype="butter")
                  
print(a3)
print(b3)

#Réponse fréquentielle

w,H=signal.freqz(b3,a3)

plt.figure()
plt.subplot(211)
plt.plot(w/(2*np.pi),20*np.log10(np.absolute(H)))
plt.xlabel("f/fe")
plt.ylabel("GdB")
plt.grid()
plt.subplot(212)
plt.plot(w/(2*np.pi),np.unwrap(np.angle(H)))
plt.xlabel("f/fe")
plt.ylabel("phase")
plt.grid()

(zeros,poles,gain) = signal.tf2zpk(b3,a3) 
print(np.absolute(poles))

########
# Réalisation du filtrage
########


### Génération d'un signal bruité
t = np.linspace(-1, 1, 201)
x = (np.sin(2*np.pi*0.75*t*(1-t) + 2.1) + 0.1*np.sin(2*np.pi*1.25*t + 1) + 0.18*np.cos(2*np.pi*3.85*t))
xn = x + np.random.randn(len(t)) * 0.08

# Affichage
plt.figure()
plt.plot(t, x, 'g')
plt.plot(t, xn, 'b')

### Application du filtre FIR b1 à xn
# Convolution centrée
yn = signal.convolve(xn,b1,mode = 'same')
plt.plot(t, yn, 'r')

### Création d'un filtre IIR butter
b, a = signal.butter(3, 0.2)

### Application d'un filtre IIR à xn : 3 versions différentes

#lfilter_zi: to choose the initial condition of the filter:
zi = signal.lfilter_zi(b, a)
z, _ = signal.lfilter(b, a, xn, zi=zi*xn[0])

#Apply the filter again, to have a result filtered at an order the same as filtfilt:
z2, _ = signal.lfilter(b, a, z, zi=zi*z[0])

#Use filtfilt to apply the filter:
y = signal.filtfilt(b, a, xn)

#Plot the original signal and the various filtered versions:
plt.figure()
plt.plot(t, xn, 'b', alpha=0.75)
plt.plot(t, z, 'r--', t, z2, 'r', t, y, 'k')
plt.legend(('noisy signal', 'lfilter, once', 'lfilter, twice','filtfilt'), loc='best')
plt.grid(True)
plt.show()



