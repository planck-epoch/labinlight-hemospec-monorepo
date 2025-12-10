import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ConfigModule, ConfigService } from '@nestjs/config';

@Module({
  imports: [
    TypeOrmModule.forRootAsync({
      imports: [ConfigModule],
      useFactory: (configService: ConfigService) => ({
        type: 'postgres',
        host: process.env.DATABASE_HOST || process.env.POSTGRES_HOST,
        port: parseInt(process.env.DATABASE_PORT || process.env.POSTGRES_PORT || '5432', 10),
        username: process.env.DATABASE_USER || process.env.POSTGRES_USER,
        password: process.env.DATABASE_PASSWORD || process.env.POSTGRES_PASSWORD,
        database: process.env.DATABASE_NAME || process.env.POSTGRES_DB,
        autoLoadEntities: true,
        synchronize: true,
      }),
      inject: [ConfigService],
    }),
  ],
})
export class DatabaseModule {}
