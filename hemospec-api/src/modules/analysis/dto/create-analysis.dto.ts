import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsObject, IsString } from 'class-validator';

export class CreateAnalysisDto {
  @ApiProperty({ description: 'Patient ID before pseudonymization' })
  @IsString()
  @IsNotEmpty()
  patientId: string;

  @ApiProperty({ description: 'Raw data from the Hemospec sensor' })
  @IsObject()
  @IsNotEmpty()
  sensorData: Record<string, any>;
}
