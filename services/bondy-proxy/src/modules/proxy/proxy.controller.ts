import { Controller, Req, Res, Param, All } from '@nestjs/common';
import { ProxyService } from './proxy.service';
import type { Request, Response } from 'express';

@Controller('proxy')
export class ProxyController {
  constructor(private readonly proxyService: ProxyService) {}

  @All(':apiName/*')
  handleProxy(
    @Param('apiName') apiName: string,
    @Req() req: Request,
    @Res() res: Response,
  ) {
    const method = req.method;
    const path = req.originalUrl.replace(`/proxy/${apiName}/`, '');

    return this.proxyService.forwardStreamOrJson(
      apiName,
      path,
      method,
      req,
      res,
    );
  }
}
