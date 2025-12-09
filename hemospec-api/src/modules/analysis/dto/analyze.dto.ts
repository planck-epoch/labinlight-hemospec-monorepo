import { ApiProperty } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsArray, IsNumber, IsObject, IsOptional, IsString, ValidateNested } from 'class-validator';

class ScanConfigSectionDto {
  @ApiProperty()
  @IsNumber()
  width_px: number;

  @ApiProperty()
  @IsNumber()
  num_patterns: number;

  @ApiProperty()
  @IsNumber()
  exposure_time: number;

  @ApiProperty()
  @IsNumber()
  section_scan_type: number;

  @ApiProperty()
  @IsNumber()
  wavelength_end_nm: number;

  @ApiProperty()
  @IsNumber()
  wavelength_start_nm: number;
}

class ScanConfigHeadDto {
  @ApiProperty()
  @IsNumber()
  scan_type: number;

  @ApiProperty()
  @IsString()
  config_name: string;

  @ApiProperty()
  @IsNumber()
  num_repeats: number;

  @ApiProperty()
  @IsNumber()
  num_sections: number;

  @ApiProperty()
  @IsNumber()
  scanConfigIndex: number;

  @ApiProperty()
  @IsString()
  ScanConfig_serial_number: string;
}

class ReferenceScanConfigDataDto {
  @ApiProperty()
  @ValidateNested()
  @Type(() => ScanConfigHeadDto)
  Head: ScanConfigHeadDto;

  @ApiProperty({ type: [ScanConfigSectionDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => ScanConfigSectionDto)
  Section: ScanConfigSectionDto[];
}

class SampleDto {
  @ApiProperty()
  @IsString()
  UUID: string;

  @ApiProperty()
  @IsNumber()
  LampPD: number;

  @ApiProperty()
  @IsString()
  Method: string;

  @ApiProperty()
  @IsNumber()
  NumRep: number;

  @ApiProperty()
  @IsNumber()
  PGAgain: number;

  @ApiProperty()
  @IsNumber()
  RefTemp: number;

  @ApiProperty()
  @IsNumber()
  Exposure: number;

  @ApiProperty()
  @IsNumber()
  Humidity: number;

  @ApiProperty({ type: [Number] })
  @IsArray()
  @IsNumber({}, { each: true })
  Intensity: number[];

  @ApiProperty()
  @IsNumber()
  RefLampPD: number;

  @ApiProperty({ type: [Number] })
  @IsArray()
  @IsNumber({}, { each: true })
  Absorbance: number[];

  @ApiProperty()
  @IsNumber()
  NumSection: number;

  @ApiProperty()
  @IsNumber()
  SystemTemp: number;

  @ApiProperty({ type: [Number] })
  @IsArray()
  @IsNumber({}, { each: true })
  WaveLength: number[];

  @ApiProperty()
  @IsString()
  RefDateTime: string;

  @ApiProperty()
  @IsNumber()
  RefHumidity: number;

  @ApiProperty()
  @IsNumber()
  DetectorTemp: number;

  @ApiProperty()
  @IsString()
  HostDateTime: string;

  @ApiProperty()
  @IsNumber()
  ReferencePGA: number;

  @ApiProperty()
  @IsNumber()
  EndWavelength: number;

  @ApiProperty()
  @IsNumber()
  HeaderVersion: number;

  @ApiProperty()
  @IsNumber()
  TotalTimeScan: number;

  @ApiProperty()
  @IsString()
  ScanConfigName: string;

  @ApiProperty()
  @IsNumber()
  ScanConfigType: number;

  @ApiProperty()
  @IsNumber()
  RefDetectorTemp: number;

  @ApiProperty()
  @IsString()
  RefSerialNumber: string;

  @ApiProperty()
  @IsNumber()
  StartWavelength: number;

  @ApiProperty()
  @IsNumber()
  DigitalResolution: number;

  @ApiProperty()
  @IsNumber()
  PatternPixelWidth: number;

  @ApiProperty()
  @IsNumber()
  RefScanConfigType: number;

  @ApiProperty({ type: [Number] })
  @IsArray()
  @IsNumber({}, { each: true })
  ReferenceIntensity: number[];

  @ApiProperty()
  @ValidateNested()
  @Type(() => ReferenceScanConfigDataDto)
  ReferenceScanConfigData: ReferenceScanConfigDataDto;

  @ApiProperty({ type: [Number] })
  @IsArray()
  @IsNumber({}, { each: true })
  ShiftVectorCoefficients: number[];

  @ApiProperty()
  @IsNumber()
  ReferenceScanDataVersion: number;

  @ApiProperty({ type: [Number] })
  @IsArray()
  @IsNumber({}, { each: true })
  PixelWavelengtCoefficients: number[];

  @ApiProperty({ type: [Number] })
  @IsArray()
  @IsNumber({}, { each: true })
  Wavelength: number[];
}

export class AnalyzeDto {
  @ApiProperty()
  @IsNumber()
  Age: number;

  @ApiProperty()
  @IsString()
  Sex: string;

  @ApiProperty({ type: [SampleDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => SampleDto)
  Samples: SampleDto[];

  @ApiProperty({ required: false })
  @IsOptional()
  @IsString()
  ModelName: string;

  @ApiProperty({ required: false })
  @IsOptional()
  @IsString()
  PatientId: string;

  @ApiProperty()
  @IsString()
  DateReading: string;

  @ApiProperty()
  @IsNumber()
  BirthdayYear: number;

  @ApiProperty()
  @IsString()
  SerialNumber: string;

  @ApiProperty()
  @IsString()
  ProcessNumber: string;

  @ApiProperty()
  @IsString()
  AnalysisBundle: string;

  @ApiProperty()
  @IsString()
  PatientIdCountry: string;

  @ApiProperty({ required: false })
  @IsOptional()
  @IsString()
  PatientContactNumber: string;
}
