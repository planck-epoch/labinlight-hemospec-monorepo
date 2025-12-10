password = "changeme"

Admin.create(email: "admin@app.local", 
             password: password,
             password_confirmation: password)

User.create(email: "user@app.local",
            password: password,
            password_confirmation: password)

org1 = Organization.create(name: "Organization Test 1")
org2 = Organization.create(name: "Organization Test 2")

Device.create(serial_number: "6100023",
              organization: org1)
Device.create(serial_number: "015R012",
              organization: org2)

erythrocytes = AnalysisTest.create(name: 'Eritrócitos',
                         code: "Eritrocitos",
                         unit: "1E6/uL",
                         reference_male: "4.5 - 5.9",
                         reference_female: "4.0 - 5.2")

hemoglobin = AnalysisTest.create(name: 'Hemoglobina',
                         code: "Hemoglobina",
                         unit: "g/dl",
                         reference_male: "13.5 - 17.0",
                         reference_female: "11.9 - 15.6")

hematocrit = AnalysisTest.create(name: 'Hematócrito',
                         code: "Hematocrito",
                         unit: "%",
                         reference_male: "40.0 - 49.5",
                         reference_female: "36.6 - 45.0")

rdw = AnalysisTest.create(name: 'RDW',
                         code: "RDW",
                         unit: "%",
                         reference_male: "11.6 - 14.0",
                         reference_female: "11.7 - 14.4")

AnalysisBundle.create(name: "Global",
                    code: "global",
                    enabled: true,
                    analysis_tests: [erythrocytes, hemoglobin, hematocrit, rdw])

AnalysisBundle.create(name: "Eritrócitos",
                    code: "erythrocytes",
                    enabled: true,
                    analysis_tests: [erythrocytes])

AnalysisBundle.create(name: "Hemoglobina",
                    code: "hemoglobin",
                    enabled: true,
                    analysis_tests: [hemoglobin])

AnalysisBundle.create(name: "Hematócrito",
                    code: "hematocrit",
                    enabled: true,
                    analysis_tests: [hematocrit])

AnalysisBundle.create(name: "RDW",
                    code: "rdw",
                    enabled: true,
                    analysis_tests: [rdw])
