import { Injectable, HttpException, HttpStatus } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import axios from 'axios';
import { Request, Response } from 'express';

@Injectable()
export class ProxyService {
  constructor(private configService: ConfigService) {}

  private getApiConfig(apiName: string) {
    const url = this.configService.get<string>(
      `${apiName.toUpperCase()}_SERVER_URL`,
    );
    const key = this.configService.get<string>(`${apiName.toUpperCase()}_KEY`);
    if (!url || !key) {
      throw new HttpException(
        `API config not found for ${apiName}`,
        HttpStatus.BAD_REQUEST,
      );
    }
    return { url, key };
  }

  async forwardStreamOrJson(
    apiName: string,
    path: string,
    method: string,
    req: Request,
    res: Response,
  ) {
    const { url, key } = this.getApiConfig(apiName);
    const fullUrl = `${url}/${path}`;
    const contentType = req.headers['content-type'] || '';

    console.log('==============================');
    console.log('🚀 Proxy Forward');
    console.log('API Name:', apiName);
    console.log('Target URL:', fullUrl);
    console.log('Using API KEY:', key); // ⚡ LOG ENV
    console.log('Method:', method);
    console.log('Content-Type:', contentType);
    console.log('Headers:', req.headers);

    // ⚡ LOG BODY nếu là JSON
    if (!contentType.includes('multipart/form-data')) {
      console.log('Body:', JSON.stringify(req.body, null, 2));
    }

    try {
      if (contentType.includes('multipart/form-data')) {
        const forwardHeaders = {
          ...req.headers,
          'x-api-key': key,
          cookie: req.headers.cookie,
        };

        console.log('--- Axios Final Headers ---', forwardHeaders);

        const response = await axios({
          method,
          url: fullUrl,
          headers: forwardHeaders,
          data: req,
          responseType: 'stream' as any,
          maxContentLength: Infinity,
          maxBodyLength: Infinity,
        } as any);

        console.log('📥 Response Headers:', response.headers);
        console.log('📥 Response Status:', response.status);

        res.status(response.status);
        Object.entries(response.headers).forEach(([k, v]) => {
          if (k.toLowerCase() !== 'transfer-encoding') {
            res.setHeader(k, v);
          }
        });
        return response.data.pipe(res);
      }

      const response = await axios({
        method,
        url: fullUrl,
        headers: {
          ...req.headers,
          'x-api-key': key,
        },
        data: req.body,
      });

      console.log('📥 Response Status:', response.status);
      console.log('📥 Response Data:', response.data);

      res.status(response.status).json(response.data);
    } catch (err: any) {
      const status = err.response?.status || 500;

      console.error('❌ Proxy Error');
      console.error('Message:', err.message);
      console.error('Status:', status);
      console.error('Error Response Headers:', err.response?.headers);
      console.error('Error Response Data:', err.response?.data);

      return res.status(status).json(
        err.response?.data || {
          success: false,
          code: status,
          data: { type: 'PROXY_ERROR', message: err.message },
        },
      );
    }
  }
}
