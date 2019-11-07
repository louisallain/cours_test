# -*- coding: utf-8 -*-
"""
Created on Tue Oct 15 13:44:25 2019

@author: e1602246
"""

import numpy as np
from scipy import signal
import matplotlib.pyplot as plt
from scipy.io.wavfile import read
from scipy.io import wavfile

# Fréquence d'échantillonnage
fe = 8000

#################
## Filtres FIR ##
#################

ordreFIR = 100

# Filtre passe-bande 0-500 Hz
f1 = 0.1
f2 = 500
pb1_fir = signal.firwin(ordreFIR+1, [f1, f2], pass_zero=False, fs=fe)

# Filtre passe-bande 500-2000 Hz
f1 = 500
f2 = 2000
pb2_fir = signal.firwin(ordreFIR+1, [f1, f2], pass_zero=False, fs=fe)

# Filtre passe-bande 2000-4000 Hz
f1 = 2000
f2 = 4000-1
pb3_fir = signal.firwin(ordreFIR+1, [f1, f2], pass_zero=False, fs=fe)

###################
## Visualisation ##
###################

# Visualise le filtre
filtre_fir = pb3_fir

# Réponse impulsionnelle
fig1 = plt.figure(figsize=(10,4))
plt.title("Réponse impulsionnelle")
plt.stem(filtre_fir)
plt.xlabel("n")
plt.xlabel("b")
plt.grid()

#Réponse fréquentielle
w,H = signal.freqz(filtre_fir)

# Amplitude
fig2= plt.figure(figsize=(10,4))
plt.plot(w/(2*np.pi),20*np.log10(np.absolute(H)))
plt.title("Réponse fréquentielle FIR amplitude")
plt.xlabel("f/fe")
plt.ylabel("GdB")
plt.grid()

# Phase
fig3 = plt.figure(figsize=(10,4))    
plt.plot(w/(2*np.pi),np.unwrap(np.angle(H)))
plt.title("Réponse fréquentielle FIR phase")
plt.xlabel("f/fe")
plt.ylabel("phase")
plt.grid()

#################
## Filtres IIR ##
#################

ordreIIR = 2

# Filtre passe-bande 0-500 Hz
f2 = 500
a1, b1 = signal.iirfilter(ordreIIR, f2/fe, btype="lowpass")

# Filtre passe-bande 500-2000 Hz
f1 = 500
f2 = 2000
a2, b2 = signal.iirfilter(ordreIIR, [f1/fe, f2/fe], btype="bandpass")

# Filtre passe-bande 2000-4000 Hz
f1 = 2000
f2 = 4000-1
a3, b3 = signal.iirfilter(ordreIIR, [f1/fe, f2/fe], btype="bandpass")

###################
## Visualisation ##
###################

a = a3
b = b3

# Amplitude
fig4 = plt.figure(figsize=(10,4))
w, H = signal.freqz(b, a)
plt.plot(w/(2*np.pi),20*np.log10(np.absolute(H)))
plt.title("Réponse fréquentielle IIR amplitude")
plt.xlabel("f/fe")
plt.ylabel("GdB")
plt.grid()

# Phase
fig5 = plt.figure(figsize=(10,4))
w, H = signal.freqz(b, a)
plt.plot(w/(2*np.pi),np.unwrap(np.angle(H)))
plt.title("Réponse fréquentielle IIR phase")
plt.xlabel("f/fe")
plt.ylabel("phase")
plt.grid()


#######################
## Comb. les filtres ##
#######################

# Visualiser la réponse fréquentielle de
# l'assemblage parallèle des trois filtres

# FIR
w1, H1 = signal.freqz(pb1_fir)
w2, H2 = signal.freqz(pb2_fir)
w3, H3 = signal.freqz(pb3_fir)

fig6 = plt.figure(figsize=(10,4))    
plt.plot(w1/(2*np.pi),20*np.log10(np.absolute(H1)), 'g',
         w2/(2*np.pi),20*np.log10(np.absolute(H2)), 'b',
         w3/(2*np.pi),20*np.log10(np.absolute(H3)), 'r')
plt.title("Réponse fréquentielle de l'assemblage parallèle des trois filtres FIR")
plt.xlabel("f/fe")
plt.ylabel("GdB")
plt.grid()


# IIR
w1, H1 = signal.freqz(b1, a1)
w2, H2 = signal.freqz(b2, a2)
w3, H3 = signal.freqz(b3, a3)

fig7 = plt.figure(figsize=(10,4))    
plt.plot(w1/(2*np.pi),20*np.log10(np.absolute(H1)), 'g',
         w2/(2*np.pi),20*np.log10(np.absolute(H2)), 'b',
         w3/(2*np.pi),20*np.log10(np.absolute(H3)), 'r')
plt.title("Réponse fréquentielle de l'assemblage parallèle des trois filtres IIR")
plt.xlabel("f/fe")
plt.ylabel("GdB")
plt.grid()

###################
## Application   ##
###################

(fsC, xC) = read('../data/Sons/Oiseaux/corneillenoire.wav')

# FIR
y1_fir = signal.lfilter(pb1_fir, [1.0], xC)
wavfile.write("../ret/y1_fir.wav", fsC, y1_fir)

y2_fir = signal.lfilter(pb2_fir, [1.0], xC)
wavfile.write("../ret/y2_fir.wav", fsC, y2_fir)

y3_fir = signal.lfilter(pb3_fir, [1.0], xC)
wavfile.write("../ret/y3_fir.wav", fsC, y3_fir)




# IIR
y1_iir = signal.lfilter(b1, a1, xC)
wavfile.write("../ret/y1_iir.wav", fsC, y1_iir)

y2_iir = signal.lfilter(b2, a2, xC)
wavfile.write("../ret/y2_iir.wav", fsC, y2_iir)

y3_iir = signal.lfilter(b3, a3, xC)
wavfile.write("../ret/y3_iir.wav", fsC, y3_iir)















