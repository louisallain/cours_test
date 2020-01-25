#!/bin/bash
file="infos" # fichier donné par secrétaire
i=1001
while IFS=: read -r f1 # f1 : nom d'utilisateur
do
    i=$((i+1))
    echo "${f1}:x:${i}:65534::/home/${f1}:/bin/bash" >> "/etc/passwd" # 65534 : groupe utilisateurs par défaut
    mkdir "/home/${f1}"
    cp -a "/etc/skel/." "/home/${f1}"
    chown -R "${f1}" "/home/${f1}"
    tmpPasswd="$(openssl rand -base64 8)"
    clef="$(openssl passwd -6 -salt xyz ${tmpPasswd})"
    dateCourante="$(($(date +%s) / 86400))"
    echo "${f1}:${clef}:${dateCourante}:0:99999:7:::" >> "/etc/shadow"
    echo "${f1}:${tmpPasswd}" >> "passwords"
done < "${file}"