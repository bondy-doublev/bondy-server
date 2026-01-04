import {
  Controller,
  Post,
  Get,
  Body,
  Req,
  Param,
  Patch,
  Query,
  ParseIntPipe,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBody,
  ApiParam,
} from '@nestjs/swagger';
import { AdvertService } from './advert.service';
import { CreateAdvertRequestDto } from './dto/create-advert-request.dto';
import { UpdateAdvertRequestStatusDto } from './dto/update-advert-request-status';
import { AdvertRequest } from 'src/entities/advert_request.entity';
import { FilterAdvertDto } from './dto/filter-advert.dto';
import { GetMyRequestsQuery } from './dto/get-my-request.dto';

@ApiTags('Advert')
@Controller('advert')
export class AdvertController {
  constructor(private readonly advertService: AdvertService) {}

  // ----------------- USER: tạo request -----------------
  @Post()
  @ApiOperation({ summary: 'User tạo request quảng cáo' })
  @ApiBody({ type: CreateAdvertRequestDto })
  @ApiResponse({
    status: 201,
    description: 'Advert request được tạo thành công',
    type: AdvertRequest,
  })
  create(@Req() req, @Body() dto: CreateAdvertRequestDto) {
    return this.advertService.create(dto.userId, dto);
  }

  @Get('admin')
  @ApiOperation({ summary: 'Admin lấy danh sách advert (filter + pagination)' })
  @ApiResponse({ status: 200 })
  getAllForAdmin(@Query() query: FilterAdvertDto) {
    return this.advertService.findAllForAdmin(query);
  }

  @Get('active')
  @ApiOperation({ summary: 'Lấy danh sách quảng cáo đang chạy ngoài web' })
  @ApiResponse({ status: 200, type: [AdvertRequest] })
  getActiveAdverts() {
    return this.advertService.findActiveAdverts();
  }

  // ----------------- USER: xem request của mình -----------------
  @Get('me')
  @ApiOperation({
    summary:
      'Lấy danh sách request của user hiện tại hoặc theo userId query param',
  })
  @ApiResponse({
    status: 200,
    description: 'Danh sách request',
    type: [AdvertRequest],
  })
  async getMyRequests(@Query() query: GetMyRequestsQuery) {
    console.log('userId', query.userId);
    return this.advertService.findMyRequests(query.userId ?? 0);
  }

  // ----------------- ADMIN: xem tất cả request -----------------
  @Get()
  @ApiOperation({ summary: 'Admin lấy tất cả request quảng cáo' })
  @ApiResponse({
    status: 200,
    description: 'Danh sách tất cả request quảng cáo',
    type: [AdvertRequest],
  })
  getAll() {
    return this.advertService.findAllRequests();
  }

  // ----------------- ADMIN: cập nhật status -----------------
  @Patch(':id/status')
  @ApiOperation({ summary: 'Admin cập nhật trạng thái request quảng cáo' })
  @ApiParam({
    name: 'id',
    type: Number,
    description: 'ID của advert request',
  })
  @ApiBody({ type: UpdateAdvertRequestStatusDto })
  @ApiResponse({
    status: 200,
    description: 'Advert request đã được cập nhật status',
    type: AdvertRequest,
  })
  updateStatus(
    @Param('id') id: string,
    @Body() dto: UpdateAdvertRequestStatusDto,
  ) {
    return this.advertService.updateStatus(+id, dto.status);
  }

  // ================= GET BY ID (NEW) =================
  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết advert request theo ID' })
  @ApiParam({
    name: 'id',
    type: Number,
    description: 'ID của advert request',
  })
  @ApiResponse({
    status: 200,
    description: 'Chi tiết advert request',
    type: AdvertRequest,
  })
  getById(@Param('id', ParseIntPipe) id: number) {
    return this.advertService.findById(id);
  }
}
