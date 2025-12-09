// Placeholder for the Analysis entity.
export class Analysis {
  id: number;
  patientId: string; // The pseudonymized ID or the PatientId from the payload
  sensorData: any; // Can store the full AnalyzeDto payload here
  predictionResult: any; // Stores the result (Eritrocitos, Hemoglobina, etc.)
  createdAt: Date;
}
