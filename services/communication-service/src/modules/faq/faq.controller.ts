import { Controller, Post, Body, Get, Delete, Param } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiParam } from '@nestjs/swagger';
import { FaqService } from './faq.service';
import { CreateFaqDto } from './dto/create-faq.dto';

@ApiTags('FAQs')
@Controller('chat/faqs')
export class FaqController {
  constructor(private service: FaqService) {}

  @Post()
  @ApiOperation({
    summary: 'Tạo FAQ mới',
    description: 'Tạo một câu hỏi – trả lời FAQ và tự động sinh embedding',
  })
  @ApiResponse({
    status: 201,
    description: 'FAQ được tạo thành công',
  })
  create(@Body() dto: CreateFaqDto) {
    return this.service.create(dto);
  }

  @Get()
  @ApiOperation({
    summary: 'Danh sách FAQ',
    description: 'Lấy toàn bộ FAQ trong hệ thống',
  })
  @ApiResponse({
    status: 200,
    description: 'Danh sách FAQ',
  })
  findAll() {
    return this.service.findAll();
  }

  @Delete(':id')
  @ApiOperation({
    summary: 'Xoá FAQ',
    description: 'Xoá FAQ theo ID',
  })
  @ApiParam({
    name: 'id',
    description: 'ID của FAQ',
    example: '550e8400-e29b-41d4-a716-446655440000',
  })
  @ApiResponse({
    status: 200,
    description: 'FAQ đã bị xoá',
  })
  remove(@Param('id') id: string) {
    return this.service.delete(id);
  }
}
