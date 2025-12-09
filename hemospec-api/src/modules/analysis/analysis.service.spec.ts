import { Test, TestingModule } from '@nestjs/testing';
import { AnalysisService } from './analysis.service';
import { AnalyzeDto } from './dto/analyze.dto';

describe('AnalysisService', () => {
  let service: AnalysisService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [AnalysisService],
    }).compile();

    service = module.get<AnalysisService>(AnalysisService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  describe('analyze', () => {
    it('should use simulation mode when SIMULATION_MODE is true', async () => {
      process.env.SIMULATION_MODE = 'true';
      const dto = new AnalyzeDto();
      dto.PatientId = 'test-patient';

      const result = await service.analyze(dto);
      expect(result).toHaveProperty('Eritrocitos');
      expect(result.Eritrocitos).toBeGreaterThanOrEqual(3.5);
    });

    it('should call external service when SIMULATION_MODE is false', async () => {
      process.env.SIMULATION_MODE = 'false';
      process.env.PREDICTION_SERVICE_URL = 'http://example.com/api/predict';

      const dto = new AnalyzeDto();
      dto.PatientId = 'test-patient';

      const mockResponse = { Eritrocitos: 5.0 };

      // Mock global fetch
      global.fetch = jest.fn().mockResolvedValue({
        ok: true,
        json: async () => mockResponse,
      } as Response);

      const result = await service.analyze(dto);
      expect(result).toEqual(mockResponse);
      expect(global.fetch).toHaveBeenCalledWith('http://example.com/api/predict', expect.anything());
    });

    it('should store analysis in history', async () => {
       process.env.SIMULATION_MODE = 'true';
       const dto = new AnalyzeDto();
       dto.PatientId = 'patient-123';

       await service.analyze(dto);

       const history = await service.findHistoryForPatient('patient-123');
       expect(history.length).toBeGreaterThan(0);
       expect(history[0].sensorData).toBe(dto);
    });
  });
});
