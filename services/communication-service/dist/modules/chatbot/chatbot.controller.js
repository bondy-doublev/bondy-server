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
const swagger_1 = require("@nestjs/swagger");
const chatbot_service_1 = require("./chatbot.service");
const chat_dto_1 = require("./dto/chat.dto");
let ChatController = class ChatController {
    service;
    constructor(service) {
        this.service = service;
    }
    chat(dto) {
        console.log('Dto', dto);
        return this.service.chat(dto.message);
    }
};
exports.ChatController = ChatController;
__decorate([
    (0, common_1.Post)(),
    (0, swagger_1.ApiOperation)({
        summary: 'Chat với FAQ Bot',
        description: 'Chatbot trả lời câu hỏi người dùng dựa hoàn toàn trên dữ liệu FAQ',
    }),
    (0, swagger_1.ApiBody)({
        type: chat_dto_1.ChatDto,
    }),
    (0, swagger_1.ApiResponse)({
        status: 200,
        description: 'Câu trả lời từ chatbot',
        schema: {
            example: {
                answer: 'Bạn bấm nút đăng ký và nhập email, mật khẩu.',
            },
        },
    }),
    __param(0, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [chat_dto_1.ChatDto]),
    __metadata("design:returntype", void 0)
], ChatController.prototype, "chat", null);
exports.ChatController = ChatController = __decorate([
    (0, swagger_1.ApiTags)('Chatbot'),
    (0, common_1.Controller)('chat/chatbot'),
    __metadata("design:paramtypes", [chatbot_service_1.ChatService])
], ChatController);
//# sourceMappingURL=chatbot.controller.js.map