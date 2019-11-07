# -*- coding: utf-8 -*-
"""
Created on Wed Apr 19 14:31:07 2017

@author: sylviegibet

"""

# Saisies 
import numpy as np
import matplotlib.pyplot as plt
from scipy.io.wavfile import read
from scipy.fftpack import fft
from scipy import signal

(fs,x) = read('../DATA/Sons/Oiseaux/corneillenoire.wav')


print('taille du fichier : ', x.size)
print('fréquence d\'échantillonnage : ', fs)
print('durée du signal : ', x.size/fs, 's')

Nf=4096
offset=256   


sf = np.zeros(Nf)
sf[:]=x[offset:offset+Nf]
X=fft(sf)/Nf # tfd
F = np.linspace(0,fs,Nf)	# échelle des fréquences

powerSpectra = 10 * np.log10(abs(X)) # dB

plt.plot(x)
"""
Amplitude du spectre
"""
mX = 2*abs(X) # amplitude

fig1 = plt.figure()
plt.figure(figsize=(10, 4))
plt.xlabel('Frequency [Hz]')
plt.ylabel('Magnitude')
#mX1=powerSpectra[100:256]
plt.xlim(2500, 2500+512)
plt.ylim(0, 10)
plt.plot(F,mX)

"""
Phase du spectre
"""
pX=np.angle(X) # phase

fig2 = plt.figure()
plt.figure(figsize=(10, 4))
plt.xlabel('Frequency [Hz]')
plt.ylabel('Phase')
plt.xlim(100, 100+128)
plt.plot(F,pX)


"""
Spectrogramme
"""
fig3 = plt.figure()
plt.figure(figsize=(10, 4))
f, t, Sxx = signal.spectrogram(x, fs)
plt.pcolormesh(t, f, np.log(Sxx))
plt.ylabel('Frequency [Hz]')
plt.xlabel('Time [sec]')

"""
Calcul du spectre...
"""
def calculerSpectre(echantillons, fs, dB=False):
    
    N = echantillons.size
    sf = np.zeros(N)
    sf[:] = echantillons[0:N]
    X = fft(sf)/N # tfd
    F = np.linspace(0, fs, N)
    
    if(dB): 
        X = 10 * np.log10(abs(X)) # dB
    
    return (F, X)

(fsC, xC) = read('../DATA/Sons/Oiseaux/corneillenoire.wav')


[freq,spectre] = calculerSpectre(xC, fsC, True)
fig4 = plt.figure(figsize=(10,4))
plt.plot(freq, spectre)
plt.xlabel('f')
plt.ylabel('A')

"""
movingFFT ... 
"""
def movingFFT(filename, winsize, offset, wintype):
    (fs, x) = read(filename)
    (freq, spectre) = calculerSpectre(x, fs, False)
    N = x.size
    plt.figure(figsize=(10,4))
    plt.xlim(offset, winsize+offset)
    plt.ylim(-0.5, 0.5)
    spectre = spectre*signal.get_window(wintype,N)
    plt.plot(freq, spectre)
    
    
movingFFT('../DATA/Sons/Oiseaux/piebavarde.wav', 256, 2600, "hamming")   
    
    
    
    
    
    
    
    
    
    
    
    
    
    