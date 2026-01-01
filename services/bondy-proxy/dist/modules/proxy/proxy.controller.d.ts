import { ProxyService } from './proxy.service';
import type { Request, Response } from 'express';
export declare class ProxyController {
    private readonly proxyService;
    constructor(proxyService: ProxyService);
    handleProxy(apiName: string, req: Request, res: Response): Promise<any>;
}
