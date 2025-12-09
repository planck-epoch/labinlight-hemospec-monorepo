import { Module, Global } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { DatabaseModule } from './database/database.module';
import { registerAs } from '@nestjs/config';

const appConfig = registerAs('app', () => ({
  port: parseInt(process.env.PORT || '3000', 10) || 3000,
  jwtSecret: process.env.JWT_SECRET || 'defaultSecretKey',
  jwtExpiresIn: process.env.JWT_EXPIRES_IN || '60m',
}));

@Global()
@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      load: [appConfig],
      envFilePath: '.env',
    }),
    DatabaseModule,
  ],
  exports: [ConfigModule, DatabaseModule],
})
export class CoreModule {}
