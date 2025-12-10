import { HttpAdapterHost, NestFactory } from '@nestjs/core';
import { NestExpressApplication } from '@nestjs/platform-express';
import { SwaggerModule, DocumentBuilder } from '@nestjs/swagger';
import { ValidationPipe } from '@nestjs/common';
import { AppModule } from './app.module';
import { urlencoded, json } from 'express';
// CHANGE: Use default imports instead of 'import * as'
import compression from 'compression';
import cookieParser from 'cookie-parser';
import { HttpExceptionFilter } from './shared/utils/http-exception.filter';

async function bootstrap() {
  const app = await NestFactory.create<NestExpressApplication>(AppModule);

  // Global Configuration
  app.setGlobalPrefix('api');
  app.use(compression());
  app.use(cookieParser());
  
  // Body parsing limits
  app.use(json({ limit: '50mb' }));
  app.use(urlencoded({ extended: true, limit: '50mb' }));

  // CORS Configuration
  app.enableCors({ 
    credentials: true, 
    origin: true,
    methods: 'GET,HEAD,PUT,PATCH,POST,DELETE', 
    allowedHeaders: 'Content-Type, Accept, Authorization, X-Requested-With, X-HTTP-Method-Override, Origin, X-Frame-Options, X-XSRF-TOKEN, Referer, X-Client-Loading',
    exposedHeaders: 'Content-Type, Accept, Authorization, X-Requested-With, X-HTTP-Method-Override, Origin, X-Frame-Options, X-XSRF-TOKEN, Referer, X-Client-Loading', 
    preflightContinue: false 
  });

  // Global Exception Filter
  const { httpAdapter } = app.get(HttpAdapterHost);
  app.useGlobalFilters(new HttpExceptionFilter());

  // Global Validation Pipe
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
    }),
  );

  // Swagger Documentation
  const config = new DocumentBuilder()
    .setTitle('Hemospec API')
    .setDescription('API for the Hemospec Sensor System MVP')
    .setVersion('1.0')
    .addBearerAuth()
    .addTag('Authentication', 'Endpoints related with authentication process.')
    .addTag('Users', 'Endpoints related with all users.')
    .addTag('Analysis', 'Endpoints related with sample analysis.')
    .addTag('Devices', 'Endpoints related with device management.')
    .build();
    
  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('docs', app, document);

  // Start Server
  const port = process.env.PORT || 3000;
  // Listen on 0.0.0.0 to accept connections from Docker/Caddy
  await app.listen(port, '0.0.0.0'); 
  console.log(`Application is running on: ${await app.getUrl()}`);
}
bootstrap();