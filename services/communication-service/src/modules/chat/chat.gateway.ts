import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  MessageBody,
  ConnectedSocket,
  OnGatewayConnection,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { ChatService } from './chat.service';

@WebSocketGateway({ cors: { origin: '*' } })
export class ChatGateway implements OnGatewayConnection {
  @WebSocketServer() server: Server;

  constructor(private chatService: ChatService) {}

  // ✅ Auto join all rooms when user connects
  async handleConnection(socket: Socket) {
    const userId = socket.handshake.auth['X-User-Id'];

    if (!userId) {
      console.log('❌ No userId in auth');
      return;
    }

    console.log(`✅ User ${userId} connected, socket:  ${socket.id}`);

    // Join personal room for badges
    socket.join(userId.toString());
    socket.data.userId = userId;

    // ✅ Auto-join all rooms user is a member of
    try {
      const userRooms = await this.chatService.memberRepo.find({
        where: { userId: parseInt(userId) },
        relations: ['room'],
      });

      userRooms.forEach((member) => {
        const roomId = member.room.id;
        socket.join(roomId);
        console.log(`🔗 Auto-joined user ${userId} to room ${roomId}`);
      });

      console.log(`✅ User ${userId} joined ${userRooms.length} rooms`);
    } catch (err) {
      console.error(`❌ Failed to auto-join rooms for user ${userId}:`, err);
    }
  }

  @SubscribeMessage('joinRoom')
  handleJoinRoom(
    @MessageBody() { roomId, userId }: any,
    @ConnectedSocket() socket: Socket,
  ) {
    socket.join(roomId);
    socket.data.userId = userId;
    console.log(`User ${userId} manually joined room ${roomId}`);
  }

  @SubscribeMessage('sendMessage')
  async handleSend(@MessageBody() data: any) {
    const msg = await this.chatService.sendMessage(
      data.senderId,
      data.roomId,
      data,
    );

    // ✅ Emit to room (all members will receive)
    this.server.to(data.roomId).emit('newMessage', msg);
    console.log('📨 New message sent to room', data.roomId);

    await this.chatService.markAllAsRead(data.senderId.toString(), data.roomId);

    // Update unread badge
    const roomMembers = await this.chatService.memberRepo.find({
      where: { room: { id: data.roomId } },
      relations: ['room'],
    });

    roomMembers.forEach(async (m) => {
      // Skip sender
      if (m.userId === data.senderId) return;

      const count = await this.chatService.getUnreadMessageCount(
        m.userId,
        data.roomId,
      );

      // Emit to personal room
      this.server
        .to(m.userId.toString())
        .emit('updateUnreadBadge', { roomId: data.roomId, count });

      console.log(`🔔 Badge updated for user ${m.userId}, count: ${count}`);
    });
  }

  @SubscribeMessage('editMessage')
  async handleEdit(@MessageBody() data: any) {
    const msg = await this.chatService.editMessage(
      data.id,
      data.userId,
      data.content,
    );
    this.server.to(msg.roomId).emit('messageEdited', msg);

    const roomMembers = await this.chatService.memberRepo.find({
      where: { room: { id: msg.roomId } },
      relations: ['room'],
    });

    roomMembers.forEach(async (m) => {
      if (!msg.readBy.includes(m.userId.toString())) {
        const count = await this.chatService.getUnreadMessageCount(
          m.userId,
          msg.roomId,
        );
        this.server
          .to(m.userId.toString())
          .emit('updateUnreadBadge', { roomId: msg.roomId, count });
      }
    });
  }

  @SubscribeMessage('deleteMessage')
  async handleDelete(@MessageBody() data: any) {
    const msg = await this.chatService.deleteMessage(data.id, data.userId);
    this.server.to(msg.roomId).emit('messageDeleted', msg);
  }

  @SubscribeMessage('openRoom')
  async handleOpenRoom(
    @MessageBody() { userId, roomId }: any,
    @ConnectedSocket() socket: Socket,
  ) {
    await this.chatService.markAllAsRead(userId.toString(), roomId);

    const count = await this.chatService.getUnreadMessageCount(userId);

    this.server.to(userId.toString()).emit('updateUnreadBadge', {
      roomId,
      count,
    });

    console.log(`✅ User ${userId} opened room ${roomId}, badge reset`);
  }
}
