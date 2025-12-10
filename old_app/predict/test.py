
import json
import lil_analysis

with open('./sample-inputs/payload.json') as file: 
    jsonData = json.load(file)

result = lil_analysis.analyze(jsonData['Samples'], jsonData['SerialNumber'])
print("---------------------------------------------")
print(result)
print("---------------------------------------------")

err = False



try:
    assert result["RDW"] >= 11 and result["RDW"] <= 26 , "RDW error"  
except Exception as e: 
    print(e)
    err = True

try:
    assert result["Eritrocitos"] >= 0.5 and result["Eritrocitos"] <= 6, "Eritrocitos error"  
except Exception as e: 
    print(e)
    err = True

try:
    assert result["Hemoglobina"] >= 0.8 and result["Hemoglobina"] <= 21, "Hemoglobina error"  
except Exception as e: 
    print(e)
    err = True


try:
    assert result["PCR"] >= 0 and result["PCR"] <= 341, "PCR error"  
except Exception as e: 
    print(e)
    err = True
    
try:
    assert result["Hematocrito"] >= 2 and result["Hematocrito"] <= 59, "Hematocrito error"   
except Exception as e: 
    print(e)
    err = True


try:
    assert result["Creatinina"] >= 0.2 and result["Creatinina"] <= 4.5, "Creatinina error"  
except Exception as e: 
    print(e)
    err = True



if err:
    print(" !! Failure !!") 
    raise Exception("There are test failures")

print(" !! Success !!")