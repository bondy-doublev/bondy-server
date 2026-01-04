import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { ChatRoom } from 'src/entities/chat-room.entity';
import { Message } from 'src/entities/message.entity';
import { RoomMember } from 'src/entities/room-member.entity';
import { Repository } from 'typeorm';

@Injectable()
export class ChatService {
  constructor(
    @InjectRepository(ChatRoom) private roomRepo: Repository<ChatRoom>,
    @InjectRepository(RoomMember) public memberRepo: Repository<RoomMember>,
    @InjectRepository(Message) private msgRepo: Repository<Message>,
  ) {}

  // Tạo phòng chat
  async createRoom(name: string, isGroup: boolean, memberIds: number[]) {
    const room = await this.roomRepo.save({ name, isGroup });
    const members = memberIds.map((id) =>
      this.memberRepo.create({ userId: id, room }),
    );
    await this.memberRepo.save(members);
    return room;
  }

  async getRooms(userId: number, isGroup: boolean) {
    const memberships = await this.memberRepo.find({
      where: { userId },
      relations: ['room', 'room.members', 'room.messages'],
    });

    const rooms: {
      id: string;
      name: string;
      isGroup: boolean;
      createdAt: Date;
      members: { id: number }[];
      latestUnreadMessage: {
        id: string;
        content: string;
        senderId: string;
        createdAt: Date;
        isUnread: boolean;
      } | null;
    }[] = [];

    for (const m of memberships) {
      const room = m.room;
      if (room.isGroup !== isGroup) continue;

      // Tin chưa đọc cuối cùng (cũ nhất trong số chưa đọc)
      const unreadMessages = room.messages
        .filter((msg) => !msg.readBy.includes(String(userId)))
        .sort((a, b) => a.createdAt.getTime() - b.createdAt.getTime()); // ↑ cũ trước, mới sau

      const unreadMsg = unreadMessages.length
        ? unreadMessages[0] // lấy tin chưa đọc đầu tiên (tức là cũ nhất)
        : null;

      // Nếu đọc hết thì lấy tin mới nhất
      const latestMsg =
        unreadMsg ||
        room.messages.sort(
          (a, b) => b.createdAt.getTime() - a.createdAt.getTime(),
        )[0] ||
        null;

      // Lấy danh sách members
      const members = isGroup
        ? room.members.map((mem) => ({ id: mem.userId }))
        : room.members
            .filter((mem) => mem.userId !== userId)
            .map((mem) => ({ id: mem.userId }));

      rooms.push({
        ...room,
        members,
        latestUnreadMessage: latestMsg
          ? {
              id: latestMsg.id,
              content: latestMsg.isDeleted
                ? '<Đã xóa>'
                : latestMsg.content || '[Đính kèm]',
              senderId: latestMsg.senderId,
              createdAt: latestMsg.createdAt,
              isUnread: !latestMsg.readBy.includes(String(userId)),
            }
          : null,
      });
    }

    return rooms;
  }

  // Nhắn tin
  async sendMessage(senderId: string, roomId: string, dto: any) {
    const msg = this.msgRepo.create({
      senderId,
      roomId,
      content: dto.content,
      fileUrl: dto.fileUrl,
      imageUrl: dto.imageUrl,
      replyToMessageId: dto.replyToMessageId,
      attachments: dto.attachments,
      sharedPost: dto.sharedPost,
    });
    return await this.msgRepo.save(msg);
  }

  // Sửa tin nhắn
  async editMessage(id: string, userId: number, content: string) {
    const msg = await this.msgRepo.findOne({ where: { id } });
    if (!msg) throw new Error('No msg found');
    msg.content = content;
    msg.isEdited = true;
    return await this.msgRepo.save(msg);
  }

  // Xóa tin nhắn
  async deleteMessage(id: string, userId: number) {
    const msg = await this.msgRepo.findOne({ where: { id } });
    if (!msg) throw new Error('No msg found');
    msg.isDeleted = true;
    msg.content = '[Message deleted]';
    return await this.msgRepo.save(msg);
  }

  // Đánh dấu đã đọc
  async markAsRead(messageId: string, userId: string) {
    const msg = await this.msgRepo.findOne({ where: { id: messageId } });
    if (!msg) return null;
    if (!msg.readBy.includes(userId)) {
      msg.readBy.push(userId);
      await this.msgRepo.save(msg);
    }
    return msg;
  }

  async getRoomMessages(roomId: string, page = 1, limit = 10) {
    const skip = (page - 1) * limit;
    return await this.msgRepo.find({
      where: { roomId },
      order: { createdAt: 'DESC' },
      take: limit,
      skip: skip,
    });
  }

  async getRoomInformation(roomId: string) {
    const room = await this.roomRepo.findOne({
      where: { id: roomId },
      relations: ['members'],
    });
    if (!room) throw new Error('Room not found');

    return room;
  }

  async getRoomFiles(roomId: string) {
    const messages = await this.msgRepo.find({
      where: { roomId },
      select: ['attachments', 'fileUrl', 'imageUrl'],
    });

    const fileMap = new Map<
      string,
      { url: string; type: 'image' | 'file'; fileName?: string }
    >();

    for (const msg of messages) {
      if (msg.attachments?.length) {
        for (const att of msg.attachments) {
          if (!fileMap.has(att.url)) {
            fileMap.set(att.url, att);
          }
        }
      }

      if (msg.imageUrl && !fileMap.has(msg.imageUrl)) {
        fileMap.set(msg.imageUrl, { url: msg.imageUrl, type: 'image' });
      }

      if (msg.fileUrl && !fileMap.has(msg.fileUrl)) {
        fileMap.set(msg.fileUrl, { url: msg.fileUrl, type: 'file' });
      }
    }

    // Trả về mảng
    return Array.from(fileMap.values());
  }

  async getUnreadMessageCount(userId: number, roomId?: string) {
    const qb = this.msgRepo
      .createQueryBuilder('m')
      .where(':userId != ALL(m.readBy)', { userId: userId.toString() })
      .andWhere('m.senderId != :userId', { userId: userId.toString() });

    if (roomId) qb.andWhere('m.roomId = :roomId', { roomId });

    return await qb.getCount();
  }

  // --- Đánh dấu tất cả tin nhắn trong room là đã đọc
  async markAllAsRead(userId: string, roomId: string) {
    const msgs = await this.msgRepo.find({ where: { roomId } });
    const toUpdate = msgs.filter((m) => !m.readBy.includes(userId));
    for (const msg of toUpdate) {
      msg.readBy.push(userId);
      await this.msgRepo.save(msg);
    }
    return toUpdate.length; // số tin nhắn đã đọc
  }

  // --- Đánh dấu tất cả tin nhắn của user là đã đọc (không phân room)
  async markAllMessagesAsRead(userId: string) {
    const msgs = await this.msgRepo.find();
    const toUpdate = msgs.filter((m) => !m.readBy.includes(userId));
    for (const msg of toUpdate) {
      msg.readBy.push(userId);
      await this.msgRepo.save(msg);
    }
    return toUpdate.length;
  }

  async updateGroup(
    roomId: string,
    dto: { name?: string; avatarUrl?: string },
  ) {
    const room = await this.roomRepo.findOne({ where: { id: roomId } });
    if (!room) throw new Error('Room not found');

    if (dto.name !== undefined) room.name = dto.name;
    if (dto.avatarUrl !== undefined) room.avatar = dto.avatarUrl;

    return await this.roomRepo.save(room);
  }

  // Lấy (hoặc tạo) phòng chat cá nhân giữa 2 user
  async getPersonalRoom(userId1: number, userId2: number) {
    if (userId1 === userId2)
      throw new Error('Cannot create personal room with yourself');

    // 1) Tìm room isGroup=false mà có đủ 2 member (userId1, userId2)
    // và tổng member của room = 2
    const room = await this.roomRepo
      .createQueryBuilder('room')
      .innerJoin('room.members', 'm1', 'm1.userId = :u1', { u1: userId1 })
      .innerJoin('room.members', 'm2', 'm2.userId = :u2', { u2: userId2 })
      .where('room.isGroup = :isGroup', { isGroup: false })
      // đảm bảo room chỉ có đúng 2 member
      .andWhere((qb) => {
        const subQuery = qb
          .subQuery()
          .select('COUNT(mem.id)')
          .from(RoomMember, 'mem')
          .where('mem.roomId = room.id')
          .getQuery();
        return `${subQuery} = 2`;
      })
      .leftJoinAndSelect('room.members', 'members')
      .leftJoinAndSelect('room.messages', 'messages')
      .getOne();

    if (room) return room;

    // 2) Không có thì tạo mới
    // name có thể để "" hoặc set theo logic của bạn
    const newRoom = await this.roomRepo.save({ name: '', isGroup: false });

    const members = [userId1, userId2].map((id) =>
      this.memberRepo.create({ userId: id, room: newRoom }),
    );
    await this.memberRepo.save(members);

    // trả về room đầy đủ relations nếu cần
    return await this.roomRepo.findOne({
      where: { id: newRoom.id },
      relations: ['members', 'messages'],
    });
  }
}
