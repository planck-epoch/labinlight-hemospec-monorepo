import { Controller, Post, Get, Body, Param, UseGuards } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation } from '@nestjs/swagger';
import { AnalysisService } from './analysis.service';
import { CreateAnalysisDto } from './dto/create-analysis.dto';
import { AnalyzeDto } from './dto/analyze.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';

@ApiTags('Analysis & History')
@Controller()
export class AnalysisController {
  constructor(private readonly analysisService: AnalysisService) {}

  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @Post('analyses')
  @ApiOperation({ summary: 'Submit sensor data for analysis (Legacy)' })
  create(@Body() createAnalysisDto: CreateAnalysisDto) {
    return this.analysisService.create(createAnalysisDto);
  }

  // New Endpoint: POST /analyze
  // Explicitly not using @UseGuards(JwtAuthGuard) for this endpoint as requested for now.
  @Post('analyze')
  @ApiOperation({ summary: 'Analyze blood sample (Mobile App Integration)' })
  async analyze(@Body() analyzeDto: AnalyzeDto) {
    return this.analysisService.analyze(analyzeDto);
  }

  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @Get('history/patient/:patientId')
  @ApiOperation({ summary: 'Retrieve analysis history for a patient' })
  findHistory(@Param('patientId') patientId: string) {
    return this.analysisService.findHistoryForPatient(patientId);
  }
}
