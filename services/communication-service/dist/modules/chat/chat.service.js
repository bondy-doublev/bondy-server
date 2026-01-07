"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.ChatService = void 0;
const common_1 = require("@nestjs/common");
const typeorm_1 = require("@nestjs/typeorm");
const chat_room_entity_1 = require("../../entities/chat-room.entity");
const message_entity_1 = require("../../entities/message.entity");
const room_member_entity_1 = require("../../entities/room-member.entity");
const typeorm_2 = require("typeorm");
let ChatService = class ChatService {
    roomRepo;
    memberRepo;
    msgRepo;
    constructor(roomRepo, memberRepo, msgRepo) {
        this.roomRepo = roomRepo;
        this.memberRepo = memberRepo;
        this.msgRepo = msgRepo;
    }
    async createRoom(name, isGroup, memberIds) {
        const room = await this.roomRepo.save({ name, isGroup });
        const members = memberIds.map((id) => this.memberRepo.create({ userId: id, room }));
        await this.memberRepo.save(members);
        return room;
    }
    async getRooms(userId, isGroup) {
        const memberships = await this.memberRepo.find({
            where: { userId },
            relations: ['room', 'room.members', 'room.messages'],
        });
        const rooms = [];
        for (const m of memberships) {
            const room = m.room;
            if (room.isGroup !== isGroup)
                continue;
            const unreadMessages = room.messages
                .filter((msg) => !msg.readBy.includes(String(userId)))
                .sort((a, b) => a.createdAt.getTime() - b.createdAt.getTime());
            const unreadMsg = unreadMessages.length
                ? unreadMessages[0]
                : null;
            const latestMsg = unreadMsg ||
                room.messages.sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime())[0] ||
                null;
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
    async sendMessage(senderId, roomId, dto) {
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
    async editMessage(id, userId, content) {
        const msg = await this.msgRepo.findOne({ where: { id } });
        if (!msg)
            throw new Error('No msg found');
        msg.content = content;
        msg.isEdited = true;
        return await this.msgRepo.save(msg);
    }
    async deleteMessage(id, userId) {
        const msg = await this.msgRepo.findOne({ where: { id } });
        if (!msg)
            throw new Error('No msg found');
        msg.isDeleted = true;
        msg.content = '[Message deleted]';
        return await this.msgRepo.save(msg);
    }
    async markAsRead(messageId, userId) {
        const msg = await this.msgRepo.findOne({ where: { id: messageId } });
        if (!msg)
            return null;
        if (!msg.readBy.includes(userId)) {
            msg.readBy.push(userId);
            await this.msgRepo.save(msg);
        }
        return msg;
    }
    async getRoomMessages(roomId, page = 1, limit = 10) {
        const skip = (page - 1) * limit;
        return await this.msgRepo.find({
            where: { roomId },
            order: { createdAt: 'DESC' },
            take: limit,
            skip: skip,
        });
    }
    async getRoomInformation(roomId) {
        const room = await this.roomRepo.findOne({
            where: { id: roomId },
            relations: ['members'],
        });
        if (!room)
            throw new Error('Room not found');
        return room;
    }
    async getRoomFiles(roomId) {
        const messages = await this.msgRepo.find({
            where: { roomId },
            select: ['attachments', 'fileUrl', 'imageUrl'],
        });
        const fileMap = new Map();
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
        return Array.from(fileMap.values());
    }
    async getUnreadMessageCount(userId, roomId) {
        const qb = this.msgRepo
            .createQueryBuilder('m')
            .where(':userId != ALL(m.readBy)', { userId: userId.toString() })
            .andWhere('m.senderId != :userId', { userId: userId.toString() });
        if (roomId)
            qb.andWhere('m.roomId = :roomId', { roomId });
        return await qb.getCount();
    }
    async markAllAsRead(userId, roomId) {
        const msgs = await this.msgRepo.find({ where: { roomId } });
        const toUpdate = msgs.filter((m) => !m.readBy.includes(userId));
        for (const msg of toUpdate) {
            msg.readBy.push(userId);
            await this.msgRepo.save(msg);
        }
        return toUpdate.length;
    }
    async markAllMessagesAsRead(userId) {
        const msgs = await this.msgRepo.find();
        const toUpdate = msgs.filter((m) => !m.readBy.includes(userId));
        for (const msg of toUpdate) {
            msg.readBy.push(userId);
            await this.msgRepo.save(msg);
        }
        return toUpdate.length;
    }
    async updateGroup(roomId, dto) {
        const room = await this.roomRepo.findOne({ where: { id: roomId } });
        if (!room)
            throw new Error('Room not found');
        if (dto.name !== undefined)
            room.name = dto.name;
        if (dto.avatarUrl !== undefined)
            room.avatar = dto.avatarUrl;
        return await this.roomRepo.save(room);
    }
    async getPersonalRoom(userId1, userId2) {
        if (userId1 === userId2)
            throw new Error('Cannot create personal room with yourself');
        const room = await this.roomRepo
            .createQueryBuilder('room')
            .innerJoin('room.members', 'm1', 'm1.userId = :u1', { u1: userId1 })
            .innerJoin('room.members', 'm2', 'm2.userId = :u2', { u2: userId2 })
            .where('room.isGroup = :isGroup', { isGroup: false })
            .andWhere((qb) => {
            const subQuery = qb
                .subQuery()
                .select('COUNT(mem.id)')
                .from(room_member_entity_1.RoomMember, 'mem')
                .where('mem.roomId = room.id')
                .getQuery();
            return `${subQuery} = 2`;
        })
            .leftJoinAndSelect('room.members', 'members')
            .leftJoinAndSelect('room.messages', 'messages')
            .getOne();
        if (room)
            return room;
        const newRoom = await this.roomRepo.save({ name: '', isGroup: false });
        const members = [userId1, userId2].map((id) => this.memberRepo.create({ userId: id, room: newRoom }));
        await this.memberRepo.save(members);
        return await this.roomRepo.findOne({
            where: { id: newRoom.id },
            relations: ['members', 'messages'],
        });
    }
};
exports.ChatService = ChatService;
exports.ChatService = ChatService = __decorate([
    (0, common_1.Injectable)(),
    __param(0, (0, typeorm_1.InjectRepository)(chat_room_entity_1.ChatRoom)),
    __param(1, (0, typeorm_1.InjectRepository)(room_member_entity_1.RoomMember)),
    __param(2, (0, typeorm_1.InjectRepository)(message_entity_1.Message)),
    __metadata("design:paramtypes", [typeorm_2.Repository,
        typeorm_2.Repository,
        typeorm_2.Repository])
], ChatService);
//# sourceMappingURL=chat.service.js.map