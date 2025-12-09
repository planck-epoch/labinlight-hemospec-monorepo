import { Module } from '@nestjs/common';
import { CoreModule } from './core/core.module';
import { AuthModule } from './modules/auth/auth.module';
import { UsersModule } from './modules/users/users.module';
import { AnalysisModule } from './modules/analysis/analysis.module';
import { DevicesModule } from './modules/devices/devices.module';

@Module({
  imports: [CoreModule, AuthModule, UsersModule, AnalysisModule, DevicesModule],
  controllers: [],
  providers: [],
})
export class AppModule {}
