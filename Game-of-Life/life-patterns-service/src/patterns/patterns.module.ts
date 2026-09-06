import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';

import { PatternsController } from './controllers/patterns.controller';
import { PatternsService } from './services/patterns.service';
import { Pattern, PatternSchema } from './schemas/pattern.schema';
import { IsValidGridConstraint } from './validators/is-valid-grid.constraint';

@Module({
  imports: [
    MongooseModule.forFeature([{ name: Pattern.name, schema: PatternSchema }]),
  ],
  controllers: [PatternsController],
  providers: [PatternsService, IsValidGridConstraint],
  exports: [PatternsService],
})
export class PatternsModule {}
