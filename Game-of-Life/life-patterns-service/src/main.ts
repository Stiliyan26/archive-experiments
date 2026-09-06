import { Module, ValidationPipe } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { NestFactory } from '@nestjs/core';
import { MongooseModule } from '@nestjs/mongoose';

import configuration, { AppConfig } from './config/configuration';
import { validationSchema } from './config/validation';
import { PatternsModule } from './patterns/patterns.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      load: [configuration],
      validationSchema,
    }),
    MongooseModule.forRootAsync({
      useFactory: (configService: ConfigService<AppConfig>) => {
        const databaseConfig =
          configService.getOrThrow<AppConfig['database']>('database');

        return {
          uri: databaseConfig.uri,
        };
      },
      inject: [ConfigService],
    }),
    PatternsModule,
  ],
})
class RootModule {}

async function bootstrap(): Promise<void> {
  const app = await NestFactory.create(RootModule, { cors: false });
  const configService = app.get<ConfigService<AppConfig>>(ConfigService);

  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true, // Strips props that aren't in the DTO
      forbidUnknownValues: true, 
      transform: true, // JSON -> DTO
      transformOptions: { 
        enableImplicitConversion: true,
      },
    }),
  );

  const appConfig = configService.getOrThrow<AppConfig['app']>('app');

  const origins = appConfig.corsOrigins ?? [];

  if (origins.length > 0) {
    app.enableCors({
      origin: origins,
      credentials: true,
    });
  } else {
    app.enableCors();
  }

  const port = appConfig.port ?? 3001;

  await app.listen(port);
  console.log(`Life Patterns API running on port ${port}`);
}
void bootstrap();
