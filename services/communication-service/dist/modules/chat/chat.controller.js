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
exports.ChatController = void 0;
const common_1 = require("@nestjs/common");
const chat_service_1 = require("./chat.service");
const swagger_1 = require("@nestjs/swagger");
const create_room_dto_1 = require("./dto/create-room.dto");
const send_message_dto_1 = require("./dto/send-message.dto");
const edit_message_dto_1 = require("./dto/edit-message.dto");
const delete_message_dto_1 = require("./dto/delete-message.dto");
const update_group_dto_1 = require("./dto/update-group.dto");
let ChatController = class ChatController {
    chatService;
    constructor(chatService) {
        this.chatService = chatService;
    }
    getMessages(roomId, page, limit) {
        const pageNum = parseInt(page) || 1;
        const limitNum = parseInt(limit) || 10;
        return this.chatService.getRoomMessages(roomId, pageNum, limitNum);
    }
    getPrivateRooms(userId) {
        return this.chatService.getRooms(+userId, false);
    }
    getPublicRooms(userId) {
        return this.chatService.getRooms(+userId, true);
    }
    createRoom(body) {
        return this.chatService.createRoom(body.name, body.isGroup, body.memberIds);
    }
    sendMessage(body) {
        console.log('Body', body);
        return this.chatService.sendMessage(body.senderId, body.roomId, body);
    }
    editMessage(id, body) {
        return this.chatService.editMessage(id, +body.userId, body.content);
    }
    deleteMessage(id, body) {
        return this.chatService.deleteMessage(id, +body.userId);
    }
    async getTotalUnread(userId) {
        const total = await this.chatService.getUnreadMessageCount(userId);
        return { total };
    }
    async getRoomMembers(roomId) {
        return await this.chatService.getRoomInformation(roomId);
    }
    async getRoomFiles(roomId) {
        const files = await this.chatService.getRoomFiles(roomId);
        return { files };
    }
    async updateGroup(roomId, body) {
        return await this.chatService.updateGroup(roomId, body);
    }
    getPersonalRoom(userId1, userId2) {
        return this.chatService.getPersonalRoom(+userId1, +userId2);
    }
};
exports.ChatController = ChatController;
__decorate([
    (0, common_1.Get)(':roomId/messages'),
    __param(0, (0, common_1.Param)('roomId')),
    __param(1, (0, common_1.Query)('page')),
    __param(2, (0, common_1.Query)('limit')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, String, String]),
    __metadata("design:returntype", void 0)
], ChatController.prototype, "getMessages", null);
__decorate([
    (0, common_1.Get)('private-rooms/:userId'),
    __param(0, (0, common_1.Param)('userId')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", void 0)
], ChatController.prototype, "getPrivateRooms", null);
__decorate([
    (0, common_1.Get)('public-rooms/:userId'),
    __param(0, (0, common_1.Param)('userId')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", void 0)
], ChatController.prototype, "getPublicRooms", null);
__decorate([
    (0, common_1.Post)('create-room'),
    __param(0, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [create_room_dto_1.CreateRoomDto]),
    __metadata("design:returntype", void 0)
], ChatController.prototype, "createRoom", null);
__decorate([
    (0, common_1.Post)('messages'),
    __param(0, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [send_message_dto_1.SendMessageDto]),
    __metadata("design:returntype", void 0)
], ChatController.prototype, "sendMessage", null);
__decorate([
    (0, common_1.Put)('messages/:id'),
    __param(0, (0, common_1.Param)('id')),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, edit_message_dto_1.EditMessageDto]),
    __metadata("design:returntype", void 0)
], ChatController.prototype, "editMessage", null);
__decorate([
    (0, common_1.Delete)('messages/:id'),
    __param(0, (0, common_1.Param)('id')),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, delete_message_dto_1.DeleteMessageDto]),
    __metadata("design:returntype", void 0)
], ChatController.prototype, "deleteMessage", null);
__decorate([
    (0, common_1.Get)('unread-count/:userId'),
    __param(0, (0, common_1.Param)('userId')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Number]),
    __metadata("design:returntype", Promise)
], ChatController.prototype, "getTotalUnread", null);
__decorate([
    (0, common_1.Get)('rooms/:roomId/members'),
    __param(0, (0, common_1.Param)('roomId')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", Promise)
], ChatController.prototype, "getRoomMembers", null);
__decorate([
    (0, common_1.Get)('rooms/:roomId/files'),
    __param(0, (0, common_1.Param)('roomId')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String]),
    __metadata("design:returntype", Promise)
], ChatController.prototype, "getRoomFiles", null);
__decorate([
    (0, common_1.Put)('rooms/:roomId'),
    __param(0, (0, common_1.Param)('roomId')),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, update_group_dto_1.UpdateGroupDto]),
    __metadata("design:returntype", Promise)
], ChatController.prototype, "updateGroup", null);
__decorate([
    (0, common_1.Get)('personal-room/:userId1/:userId2'),
    __param(0, (0, common_1.Param)('userId1')),
    __param(1, (0, common_1.Param)('userId2')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, String]),
    __metadata("design:returntype", void 0)
], ChatController.prototype, "getPersonalRoom", null);
exports.ChatController = ChatController = __decorate([
    (0, swagger_1.ApiTags)('Chat'),
    (0, swagger_1.ApiBearerAuth)('Bearer'),
    (0, swagger_1.ApiSecurity)('API Key'),
    (0, common_1.Controller)('chat'),
    __metadata("design:paramtypes", [chat_service_1.ChatService])
], ChatController);
//# sourceMappingURL=chat.controller.js.map