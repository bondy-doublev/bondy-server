import { Controller, Get, Req } from '@nestjs/common';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';

@ApiTags('App')
@ApiBearerAuth('Bearer') // ⚡ bắt Swagger gắn token Bearer
@ApiSecurity('API Key') // ⚡ bắt Swagger gắn API key
@Controller('chat')
export class AppController {
  @Get('hello')
  getHello(@Req() req: Request) {
    return {
      message: 'Hello from Communication Service 👋',
      headers: req.headers,
    };
  }
}
