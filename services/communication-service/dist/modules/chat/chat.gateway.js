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
exports.ChatGateway = void 0;
const websockets_1 = require("@nestjs/websockets");
const socket_io_1 = require("socket.io");
const chat_service_1 = require("./chat.service");
let ChatGateway = class ChatGateway {
    chatService;
    server;
    constructor(chatService) {
        this.chatService = chatService;
    }
    async handleConnection(socket) {
        const userId = socket.handshake.auth['X-User-Id'];
        if (!userId) {
            console.log('❌ No userId in auth');
            return;
        }
        console.log(`✅ User ${userId} connected, socket:  ${socket.id}`);
        socket.join(userId.toString());
        socket.data.userId = userId;
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
        }
        catch (err) {
            console.error(`❌ Failed to auto-join rooms for user ${userId}:`, err);
        }
    }
    handleJoinRoom({ roomId, userId }, socket) {
        socket.join(roomId);
        socket.data.userId = userId;
        console.log(`User ${userId} manually joined room ${roomId}`);
    }
    async handleSend(data) {
        const msg = await this.chatService.sendMessage(data.senderId, data.roomId, data);
        this.server.to(data.roomId).emit('newMessage', msg);
        console.log('📨 New message sent to room', data.roomId);
        await this.chatService.markAllAsRead(data.senderId.toString(), data.roomId);
        const roomMembers = await this.chatService.memberRepo.find({
            where: { room: { id: data.roomId } },
            relations: ['room'],
        });
        roomMembers.forEach(async (m) => {
            if (m.userId === data.senderId) {
                this.server
                    .to(m.userId.toString())
                    .emit('updateUnreadBadge', { roomId: data.roomId, count: 0 });
                return;
            }
            const count = await this.chatService.getUnreadMessageCount(m.userId, data.roomId);
            this.server
                .to(m.userId.toString())
                .emit('updateUnreadBadge', { roomId: data.roomId, count });
            console.log(`🔔 Badge updated for user ${m.userId}, count: ${count}`);
        });
    }
    async handleEdit(data) {
        const msg = await this.chatService.editMessage(data.id, data.userId, data.content);
        this.server.to(msg.roomId).emit('messageEdited', msg);
        const roomMembers = await this.chatService.memberRepo.find({
            where: { room: { id: msg.roomId } },
            relations: ['room'],
        });
        roomMembers.forEach(async (m) => {
            if (!msg.readBy.includes(m.userId.toString())) {
                const count = await this.chatService.getUnreadMessageCount(m.userId, msg.roomId);
                this.server
                    .to(m.userId.toString())
                    .emit('updateUnreadBadge', { roomId: msg.roomId, count });
            }
        });
    }
    async handleDelete(data) {
        const msg = await this.chatService.deleteMessage(data.id, data.userId);
        this.server.to(msg.roomId).emit('messageDeleted', msg);
    }
    async handleRead(data) {
        const msg = await this.chatService.markAsRead(data.messageId, data.userId);
        if (msg) {
            this.server.to(msg.roomId).emit('messageRead', msg);
            const count = await this.chatService.getUnreadMessageCount(data.userId, msg.roomId);
            this.server
                .to(data.userId.toString())
                .emit('updateUnreadBadge', { roomId: msg.roomId, count });
            console.log(`✅ Message ${msg.id} marked as read by user ${data.userId}, new badge: ${count}`);
        }
    }
    async handleMarkAllAsRead({ userId, roomId }) {
        await this.chatService.markAllAsRead(userId, roomId);
        const totalCount = await this.chatService.getUnreadMessageCount(userId);
        this.server
            .to(userId.toString())
            .emit('updateUnreadBadge', { roomId, count: 0 });
        console.log(`✅ All messages in room ${roomId} marked as read by user ${userId}`);
    }
    async handleOpenRoom({ userId, roomId }, socket) {
        await this.chatService.markAllAsRead(userId.toString(), roomId);
        const totalCount = await this.chatService.getUnreadMessageCount(userId);
        this.server
            .to(userId.toString())
            .emit('updateUnreadBadge', { roomId, count: 0 });
        console.log(`✅ User ${userId} opened room ${roomId}, badge reset to 0`);
    }
};
exports.ChatGateway = ChatGateway;
__decorate([
    (0, websockets_1.WebSocketServer)(),
    __metadata("design:type", socket_io_1.Server)
], ChatGateway.prototype, "server", void 0);
__decorate([
    (0, websockets_1.SubscribeMessage)('joinRoom'),
    __param(0, (0, websockets_1.MessageBody)()),
    __param(1, (0, websockets_1.ConnectedSocket)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, socket_io_1.Socket]),
    __metadata("design:returntype", void 0)
], ChatGateway.prototype, "handleJoinRoom", null);
__decorate([
    (0, websockets_1.SubscribeMessage)('sendMessage'),
    __param(0, (0, websockets_1.MessageBody)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], ChatGateway.prototype, "handleSend", null);
__decorate([
    (0, websockets_1.SubscribeMessage)('editMessage'),
    __param(0, (0, websockets_1.MessageBody)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], ChatGateway.prototype, "handleEdit", null);
__decorate([
    (0, websockets_1.SubscribeMessage)('deleteMessage'),
    __param(0, (0, websockets_1.MessageBody)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], ChatGateway.prototype, "handleDelete", null);
__decorate([
    (0, websockets_1.SubscribeMessage)('readMessage'),
    __param(0, (0, websockets_1.MessageBody)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], ChatGateway.prototype, "handleRead", null);
__decorate([
    (0, websockets_1.SubscribeMessage)('markAllAsRead'),
    __param(0, (0, websockets_1.MessageBody)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], ChatGateway.prototype, "handleMarkAllAsRead", null);
__decorate([
    (0, websockets_1.SubscribeMessage)('openRoom'),
    __param(0, (0, websockets_1.MessageBody)()),
    __param(1, (0, websockets_1.ConnectedSocket)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, socket_io_1.Socket]),
    __metadata("design:returntype", Promise)
], ChatGateway.prototype, "handleOpenRoom", null);
exports.ChatGateway = ChatGateway = __decorate([
    (0, websockets_1.WebSocketGateway)({ cors: { origin: '*' } }),
    __metadata("design:paramtypes", [chat_service_1.ChatService])
], ChatGateway);
//# sourceMappingURL=chat.gateway.js.map