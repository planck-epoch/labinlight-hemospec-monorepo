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
    // 1. Check for Simulation Mode via Env or DTO (optional)
    const isSimulationMode = process.env.SIMULATION_MODE === 'true';

    let result;
    if (isSimulationMode) {
      this.logger.log('Running in SIMULATION MODE');
      result = this.generateSimulationValues();
    } else {
      // 2. Call External Service
      // Note: The python service is likely at http://old_app_predict:8080 if running in docker-compose,
      // or we assume the environment provides the correct URL.
      // Default to localhost for dev if not set, but 'old_app_predict' is the service name in typical docker setups.
      const externalUrl = process.env.PREDICTION_SERVICE_URL || 'http://localhost:8080/analyze';

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
           const text = await response.text();
           this.logger.error(`Response body: ${text}`);
           throw new Error(`External service error: ${response.statusText} - ${text}`);
        }

        result = await response.json();
      } catch (error) {
        this.logger.error('Error calling external service', error);
        // Fallback to simulation for demo robustness if external service is unreachable
        // (Only if explicitly desired, but standard behavior is to fail)
        // Given this is a demo environment, failing fast helps debugging.
        throw error;
      }
    }

    // 3. Store in History
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
    const eritrocitos = this.randomInRange(3.5, 5.8);
    const hemoglobina = this.randomInRange(10.0, 17.0);
    const hematocrito = this.randomInRange(30.0, 52.0);
    const rdw = this.randomInRange(11.0, 16.0);
    const creatinina = this.randomInRange(0.5, 1.5);
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
