import { Module } from '@nestjs/common';
import { StocksController } from './controllers/stocks.controller';
import { StocksService } from './services/stocks.service';
import { StocksGateway } from './gateways/stocks.gateway';

@Module({
  controllers: [StocksController],
  providers: [StocksService, StocksGateway],
})
export class StocksModule {}
