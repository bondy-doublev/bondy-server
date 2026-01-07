import { Controller, Post, Body } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBody } from '@nestjs/swagger';
import { ChatService } from './chatbot.service';
import { ChatDto } from './dto/chat.dto';

@ApiTags('Chatbot')
@Controller('chat/chatbot')
export class ChatController {
  constructor(private service: ChatService) {}

  @Post()
  @ApiOperation({
    summary: 'Chat với FAQ Bot',
    description:
      'Chatbot trả lời câu hỏi người dùng dựa hoàn toàn trên dữ liệu FAQ',
  })
  @ApiBody({
    type: ChatDto,
  })
  @ApiResponse({
    status: 200,
    description: 'Câu trả lời từ chatbot',
    schema: {
      example: {
        answer: 'Bạn bấm nút đăng ký và nhập email, mật khẩu.',
      },
    },
  })
  chat(@Body() dto: ChatDto) {
    console.log('Dto', dto);
    return this.service.chat(dto.message);
  }
}
