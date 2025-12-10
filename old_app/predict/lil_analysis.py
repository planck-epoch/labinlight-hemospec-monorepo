import numpy as np
from glob import glob
import pandas as pd
import logging

B_outliers = np.genfromtxt('models/outlier_detect.txt')
B0_outliers = 0.94849648

B0_values_capillary = {
    '6100023': {'RBC': 2.75, 'Hemoglobin': 10.72, 'Hematocrit': 33.64, 'RDW': 23.91, 'Creatinina': 0.36, 'PCR': -14.42},
    '850R019': {'RBC': 2.58, 'Hemoglobin': 10.07, 'Hematocrit': 32.05, 'RDW': 23.84, 'Creatinina': 0.29, 'PCR': -13.74},
    '850R058': {'RBC': 2.48, 'Hemoglobin': 9.37, 'Hematocrit': 32.43, 'RDW': 23.69, 'Creatinina': 0.37, 'PCR': -14.16},
    '015R014': {'RBC': 2.50, 'Hemoglobin': 10.28, 'Hematocrit': 32.45, 'RDW': 23.26, 'Creatinina': 0.28, 'PCR': -15.02},
    '015R012': {'RBC': 2.28, 'Hemoglobin': 9.27, 'Hematocrit': 31.57, 'RDW': 24.57, 'Creatinina': 0.26, 'PCR': -14.89},
    'C49R049': {'RBC': 2.21, 'Hemoglobin': 10.26, 'Hematocrit': 32.69, 'RDW': 23.81, 'Creatinina': -0.166, 'PCR': -22.05},
    '015R013': {'RBC': 2.5678, 'Hemoglobin': 8.6052, 'Hematocrit': 30.2086, 'RDW': 23.6401, 'Creatinina': 0.0973, 'PCR': -16.0176},
    'C49R051': {'RBC': 2.14, 'Hemoglobin': 8.57, 'Hematocrit': 31.79, 'RDW': 24.62, 'Creatinina': 0.1296, 'PCR': -11.29},
    'C49R048': {'RBC': 2.14, 'Hemoglobin': 8.57, 'Hematocrit': 31.79, 'RDW': 24.62, 'Creatinina': 0.1296, 'PCR': -11.29},
    'C49R053': {'RBC': 2.91, 'Hemoglobin': 10.45, 'Hematocrit': 36.06, 'RDW': 25.47, 'Creatinina': 0.88, 'PCR': -15.38},
    'C49R045': {'RBC': 2.64, 'Hemoglobin': 10.26, 'Hematocrit': 32.69, 'RDW': 23.81, 'Creatinina': 0.2744, 'PCR': -13.2116},
    'C49R038': {'RBC': 2.38, 'Hemoglobin': 10.50, 'Hematocrit': 34.69, 'RDW': 22.96, 'Creatinina': -0.04, 'PCR': -14.92},
}

RBC_B=np.genfromtxt('models/RBC.txt')

Hemoglobin_B=np.genfromtxt('models/Hemoglobin.txt')

Hematocrit_B=np.genfromtxt('models/Hematocrit.txt')

RDW_B=np.genfromtxt('models/RDW.txt') 

Creatinina_B=np.genfromtxt('models/Crea.txt')

PCR_B=np.genfromtxt('models/PCR.txt')


def analyze(samples, serial_number):
    nrSamples = len(samples)
    Abs = {}
    RBC={}
    Hemoglobin={}
    Hematocrit={}
    RDW={}
    Creatinina={}
    PCR={}
    for i in range (nrSamples):
        Abs[i] = samples[i]['Absorbance'][13:210]

        outlier_calc = sum(Abs[i] * B_outliers) + B0_outliers
        if outlier_calc >= 0.85:
            logging.info(f'{serial_number}, sample {i} no blood type detected. {outlier_calc}')
            return 1

        B0_c = B0_values_capillary.get(serial_number)

        RBC[i] = sum(Abs[i]*RBC_B) + B0_c['RBC']
        Hemoglobin[i] = sum(Abs[i]*Hemoglobin_B) + B0_c['Hemoglobin']
        Hematocrit[i] = sum(Abs[i]*Hematocrit_B) + B0_c['Hematocrit']
        RDW[i] = sum(Abs[i]*RDW_B) + B0_c['RDW']
        Creatinina[i] = 2**(sum(Abs[i]*Creatinina_B) + B0_c['Creatinina'])
        PCR[i] = 2**(sum(Abs[i]*PCR_B) + B0_c['PCR'])

        if not (1 <= RBC[i] <= 7 and 1 <= Hemoglobin[i] <= 23 and 2 <= Hematocrit[i] <= 62 and 9 <= RDW[i] <= 27):
            return 2
     
    df2 = pd.DataFrame({'Eritrocitos': RBC})
    df3 = pd.DataFrame({'Hemoglobina': Hemoglobin})
    df4 = pd.DataFrame({'Hematocrito': Hematocrit})
    df9 = pd.DataFrame({'RDW': RDW})
    df10 = pd.DataFrame({'Creatinina': Creatinina})
    df17 = pd.DataFrame({'PCR': PCR})

    df21 = pd.concat([df2, df3, df4, df9, df10, df17], axis=1)
    df21.apply(pd.to_numeric, errors='coerce')
    df100 = df21.agg({'Eritrocitos':np.mean,'Hemoglobina':np.mean,
    'Hematocrito':np.mean,'RDW':np.mean,'Creatinina':np.mean,'PCR':np.mean})
 
    df100['Eritrocitos'] = df100['Eritrocitos'].round(2)
    df100['Hemoglobina'] = df100['Hemoglobina'].round(1)
    df100['Hematocrito'] = df100['Hematocrito'].round(1)
    df100['RDW'] = df100['RDW'].round(1)
    df100['Creatinina'] = df100['Creatinina'].round(1)
    df100['PCR'] = df100['PCR'].round(1)

    return (df100)

