import { Test, TestingModule } from '@nestjs/testing';
import { AnalysisController } from './analysis.controller';
import { AnalysisService } from './analysis.service';
import { CreateAnalysisDto } from './dto/create-analysis.dto';
import { AnalyzeDto } from './dto/analyze.dto';

describe('AnalysisController', () => {
  let controller: AnalysisController;
  let service: AnalysisService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [AnalysisController],
      providers: [AnalysisService],
    }).compile();

    controller = module.get<AnalysisController>(AnalysisController);
    service = module.get<AnalysisService>(AnalysisService);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });

  describe('analyze', () => {
    it('should call analysisService.analyze with the DTO', async () => {
      const dto = new AnalyzeDto();
      dto.PatientId = 'test-patient';

      const result = { Eritrocitos: 4.5 };
      jest.spyOn(service, 'analyze').mockResolvedValue(result);

      expect(await controller.analyze(dto)).toBe(result);
      expect(service.analyze).toHaveBeenCalledWith(dto);
    });
  });
});
