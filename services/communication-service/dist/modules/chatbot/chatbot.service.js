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
Object.defineProperty(exports, "__esModule", { value: true });
exports.ChatService = void 0;
const common_1 = require("@nestjs/common");
const openai_service_1 = require("../../common/openai/openai.service");
const supabase_service_1 = require("../../common/supabase/supabase.service");
let ChatService = class ChatService {
    openai;
    supabase;
    constructor(openai, supabase) {
        this.openai = openai;
        this.supabase = supabase;
    }
    async chat(message) {
        const embedding = await this.openai.embed(message);
        const { data: faqs } = await this.supabase.client.rpc('match_faqs', {
            query_embedding: embedding.data[0].embedding,
            match_count: 3,
        });
        const context = faqs
            .map((f) => `Q:${f.question}\nA:${f.answer}`)
            .join('\n\n');
        const res = await this.openai.chat([
            {
                role: 'system',
                content: `Bạn là chatbot hỗ trợ cho mạng xã hội Bondy.
          QUY TẮC:
          - CHỈ sử dụng thông tin trong FAQ.
          - KHÔNG suy đoán.
          - KHÔNG trả lời câu hỏi ngoài phạm vi Bondy.
          - Nếu không có câu trả lời phù hợp, hãy nói:
            "Xin lỗi, tôi chỉ hỗ trợ các câu hỏi liên quan đến Bondy."
          `,
            },
            { role: 'user', content: `${context}\n\nCâu hỏi: ${message}` },
        ]);
        return res.choices[0].message.content;
    }
};
exports.ChatService = ChatService;
exports.ChatService = ChatService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [openai_service_1.OpenaiService,
        supabase_service_1.SupabaseService])
], ChatService);
//# sourceMappingURL=chatbot.service.js.map