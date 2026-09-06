import { Stock } from '@live-stock-prices/api-interfaces';
import { Controller, Get, Sse } from '@nestjs/common';
import { StocksService } from '../services/stocks.service';
import { Observable, map } from 'rxjs';

@Controller('stocks')
export class StocksController {

    constructor(private readonly stockService: StocksService) { }

    @Get('simple-poll')
    getData(): Stock[] {
        return this.stockService.getStocks();
    }

    @Get('long-poll')
    async getLongPollingData(): Promise<Stock[]> {
        return await this.stockService.waitForUpdate();
    }

    @Sse('sse')
    sse(): Observable<MessageEvent> {
        return this.stockService.getStream().pipe(
            map((stocks) => ({
                data: stocks,
            } as MessageEvent))
        );
    }
}
