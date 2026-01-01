import { ConfigService } from '@nestjs/config';
import { Request, Response } from 'express';
export declare class ProxyService {
    private configService;
    constructor(configService: ConfigService);
    private getApiConfig;
    forwardStreamOrJson(apiName: string, path: string, method: string, req: Request, res: Response): Promise<any>;
}
