import { Module } from '@nestjs/common';
import { AnalysisService } from './analysis.service';
import { AnalysisController } from './analysis.controller';
// import { HttpModule } from '@nestjs/axios'; // Uncomment if you need to make HTTP requests

@Module({
  // imports: [HttpModule], // For calling the Python Prediction Service
  controllers: [AnalysisController],
  providers: [AnalysisService],
})
export class AnalysisModule {}
