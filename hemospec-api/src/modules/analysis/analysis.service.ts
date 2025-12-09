import { Injectable, Logger } from '@nestjs/common';
import { createHmac } from 'crypto';
import { CreateAnalysisDto } from './dto/create-analysis.dto';
import { AnalyzeDto } from './dto/analyze.dto';
import { Analysis } from './entities/analysis.entity';

@Injectable()
export class AnalysisService {
  private readonly logger = new Logger(AnalysisService.name);
  // Mock data store
  private readonly analyses: Analysis[] = [];

  /**
   * Creates a new analysis using the legacy/internal format.
   */
  async create(createAnalysisDto: CreateAnalysisDto): Promise<Analysis> {
    const secret = process.env.PATIENT_ID_HASH_SECRET || 'default-hash-secret';
    const hashedPatientId = createHmac('sha256', secret)
      .update(createAnalysisDto.patientId)
      .digest('hex');

    const predictionResult = { prediction: 'mock_prediction_value' };

    const newAnalysis: Analysis = {
      id: this.analyses.length + 1,
      patientId: hashedPatientId,
      sensorData: createAnalysisDto.sensorData,
      predictionResult: predictionResult,
      createdAt: new Date(),
    };

    this.analyses.push(newAnalysis);
    return newAnalysis;
  }

  /**
   * Processes the external analysis request from the mobile app.
   */
  async analyze(analyzeDto: AnalyzeDto): Promise<any> {
    // 1. Check for Simulation Mode
    const isSimulationMode = process.env.SIMULATION_MODE === 'true';

    let result;
    if (isSimulationMode) {
      this.logger.log('Running in SIMULATION MODE');
      result = this.generateSimulationValues();
    } else {
      // 2. Call External Service
      const externalUrl = process.env.PREDICTION_SERVICE_URL || 'https://8e1579505cf6.ngrok-free.app/analyze';
      try {
        this.logger.log(`Calling external prediction service: ${externalUrl}`);
        const response = await fetch(externalUrl, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(analyzeDto),
        });

        if (!response.ok) {
           this.logger.error(`External service failed with status ${response.status}`);
           // Fallback to simulation if external service fails? The prompt said "create a simulation mode like a parameter that when added do not reach the endpoint".
           // However, if the service is down, maybe we should just throw or return error.
           // For now, I will throw an error to be safe unless simulation is explicitly requested.
           throw new Error(`External service error: ${response.statusText}`);
        }

        result = await response.json();
      } catch (error) {
        this.logger.error('Error calling external service', error);
        throw error;
      }
    }

    // 3. Store in History
    // We try to use the PatientId from the payload if available, otherwise just generate a hash or 'unknown'
    const patientIdRaw = analyzeDto.PatientId || 'unknown';
    const secret = process.env.PATIENT_ID_HASH_SECRET || 'default-hash-secret';
    const hashedPatientId = createHmac('sha256', secret)
      .update(patientIdRaw)
      .digest('hex');

    const analysisRecord: Analysis = {
      id: this.analyses.length + 1,
      patientId: hashedPatientId,
      sensorData: analyzeDto,
      predictionResult: result,
      createdAt: new Date(),
    };
    this.analyses.push(analysisRecord);

    return result;
  }

  private generateSimulationValues() {
    // Generates realistic, ISO-compliant blood analysis values.
    // Ranges are illustrative of healthy adults.

    // Eritrocitos (RBC): 4.0 - 5.5 (x10^6/uL)
    const eritrocitos = this.randomInRange(3.5, 5.8);

    // Hemoglobina (Hgb): 12.0 - 16.0 (g/dL)
    const hemoglobina = this.randomInRange(10.0, 17.0);

    // Hematocrito (Hct): 36 - 50 (%)
    const hematocrito = this.randomInRange(30.0, 52.0);

    // RDW: 11.5 - 14.5 (%)
    const rdw = this.randomInRange(11.0, 16.0);

    // Creatinina: 0.6 - 1.2 (mg/dL)
    const creatinina = this.randomInRange(0.5, 1.5);

    // PCR (C-Reactive Protein): < 10 (mg/L) usually, but can go high with infection.
    // Let's vary it between 0 and 50 to show range.
    const pcr = this.randomInRange(0.1, 40.0);

    return {
      Eritrocitos: parseFloat(eritrocitos.toFixed(2)),
      Hemoglobina: parseFloat(hemoglobina.toFixed(1)),
      Hematocrito: parseFloat(hematocrito.toFixed(1)),
      RDW: parseFloat(rdw.toFixed(1)),
      Creatinina: parseFloat(creatinina.toFixed(2)),
      PCR: parseFloat(pcr.toFixed(1)),
    };
  }

  private randomInRange(min: number, max: number): number {
    return Math.random() * (max - min) + min;
  }

  /**
   * Retrieves the analysis history for a given patient ID.
   */
  async findHistoryForPatient(patientId: string): Promise<Analysis[]> {
    const secret = process.env.PATIENT_ID_HASH_SECRET || 'default-hash-secret';
    const hashedPatientId = createHmac('sha256', secret)
      .update(patientId)
      .digest('hex');

    return this.analyses.filter((a) => a.patientId === hashedPatientId);
  }
}
