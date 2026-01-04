import {
  Controller,
  Get,
  Param,
  Post,
  Body,
  Put,
  Delete,
  Query,
} from '@nestjs/common';
import { ChatService } from './chat.service';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { CreateRoomDto } from './dto/create-room.dto';
import { SendMessageDto } from './dto/send-message.dto';
import { EditMessageDto } from './dto/edit-message.dto';
import { DeleteMessageDto } from './dto/delete-message.dto';
import { UpdateGroupDto } from './dto/update-group.dto';

@ApiTags('Chat')
@ApiBearerAuth('Bearer')
@ApiSecurity('API Key')
@Controller('chat')
export class ChatController {
  constructor(private chatService: ChatService) {}

  @Get(':roomId/messages')
  getMessages(
    @Param('roomId') roomId: string,
    @Query('page') page: string, // page số
    @Query('limit') limit: string, // số tin mỗi page
  ) {
    const pageNum = parseInt(page) || 1;
    const limitNum = parseInt(limit) || 10;
    return this.chatService.getRoomMessages(roomId, pageNum, limitNum);
  }

  @Get('private-rooms/:userId')
  getPrivateRooms(@Param('userId') userId: string) {
    return this.chatService.getRooms(+userId, false);
  }

  @Get('public-rooms/:userId')
  getPublicRooms(@Param('userId') userId: string) {
    return this.chatService.getRooms(+userId, true);
  }
  @Post('create-room')
  createRoom(@Body() body: CreateRoomDto) {
    return this.chatService.createRoom(body.name, body.isGroup, body.memberIds);
  }

  @Post('messages')
  sendMessage(@Body() body: SendMessageDto) {
    console.log('Body', body);
    return this.chatService.sendMessage(body.senderId, body.roomId, body);
  }

  @Put('messages/:id')
  editMessage(@Param('id') id: string, @Body() body: EditMessageDto) {
    return this.chatService.editMessage(id, +body.userId, body.content);
  }

  @Delete('messages/:id')
  deleteMessage(@Param('id') id: string, @Body() body: DeleteMessageDto) {
    return this.chatService.deleteMessage(id, +body.userId);
  }

  @Get('unread-count/:userId')
  async getTotalUnread(@Param('userId') userId: number) {
    const total = await this.chatService.getUnreadMessageCount(userId);
    return { total };
  }

  @Get('rooms/:roomId/members')
  async getRoomMembers(@Param('roomId') roomId: string) {
    return await this.chatService.getRoomInformation(roomId);
  }

  @Get('rooms/:roomId/files')
  async getRoomFiles(@Param('roomId') roomId: string) {
    const files = await this.chatService.getRoomFiles(roomId);
    return { files };
  }

  @Put('rooms/:roomId')
  async updateGroup(
    @Param('roomId') roomId: string,
    @Body() body: UpdateGroupDto,
  ) {
    return await this.chatService.updateGroup(roomId, body);
  }

  @Get('personal-room/:userId1/:userId2')
  getPersonalRoom(
    @Param('userId1') userId1: string,
    @Param('userId2') userId2: string,
  ) {
    return this.chatService.getPersonalRoom(+userId1, +userId2);
  }
}
