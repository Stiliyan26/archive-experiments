import { Injectable, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { Stock } from '@live-stock-prices/api-interfaces';
import { Subject, Observable, take } from 'rxjs';

@Injectable()
export class StocksService implements OnModuleInit, OnModuleDestroy {

    private intervalId: NodeJS.Timeout;

    private stocks: Stock[] = [
        { symbol: 'AAPL', price: 150.0, timestamp: new Date() },
        { symbol: 'GOOGL', price: 2800.0, timestamp: new Date() },
        { symbol: 'MSFT', price: 300.0, timestamp: new Date() },
        { symbol: 'AMZN', price: 3400.0, timestamp: new Date() },
        { symbol: 'TSLA', price: 700.0, timestamp: new Date() },
    ];

    private stockUpdate$ = new Subject<Stock[]>();

    onModuleInit() {
        this.startSimulation();
    }

    onModuleDestroy() {
        if (this.intervalId) {
            clearInterval(this.intervalId);
        }
        //Singal completion to all subscribers (SSE, WebSockets)
        this.stockUpdate$.complete();
    }

    getStocks(): Stock[] {
        return this.stocks;
    }

    getStream(): Observable<Stock[]> {
        return this.stockUpdate$.asObservable();
    }

    waitForUpdate(): Promise<Stock[]> {
        return new Promise((resolve) => {
            this.stockUpdate$
                .pipe(take(1))
                .subscribe((stocks) => resolve(stocks))
        });
    }

    startSimulation(): void {
        this.intervalId = setInterval(() => {
            this.updatePrices();
        }, 1000);
    }

    updatePrices(): void {
        this.stocks = this.stocks.map((stock) => ({
            ...stock,
            price: Number((stock.price * (1 + (Math.random() * 0.02 - 0.01))).toFixed(2)),
            timestamp: new Date()
        }));

        this.stockUpdate$.next(this.stocks);
    }
}


