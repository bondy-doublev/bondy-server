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
            match_count: 5,
        });
        const context = faqs
            .map((f) => `Q:${f.question}\nA:${f.answer}`)
            .join('\n\n');
        const res = await this.openai.chat([
            {
                role: 'system',
                content: `Bạn là chatbot hỗ trợ cho mạng xã hội Bondy.  
          QUY TẮC:  
          1. Nếu người dùng hỏi về Bondy theo bất kỳ cách nào dưới đây, hãy hiểu rằng câu hỏi liên quan đến Bondy:  
            - Tên trực tiếp: bondy, Bondy, BONDY, bondy.com, web Bondy, website Bondy  
            - Gián tiếp: web này, website này, trang này, nền tảng này, app này, dịch vụ này, mạng xã hội này, đây, bạn  
            - Liên quan tính năng: nơi đăng bài, tạo tài khoản, kết nối bạn bè, nền tảng mạng xã hội thu nhỏ, ứng dụng này, trang web để kết nối và chia sẻ  
            - Lỗi chính tả phổ biến: bondi, bondie, bondey, b0ndy  
            - **Lưu ý:** tất cả kiểm tra phải **không phân biệt hoa thường**.  

          2. Chỉ sử dụng thông tin có trong FAQ của Bondy để trả lời.  
          3. Nếu câu hỏi của người dùng khác cách diễn đạt nhưng cùng ý nghĩa với FAQ, hãy map FAQ tương ứng.  
          4. KHÔNG suy đoán hay thêm thông tin ngoài FAQ.  
          5. KHÔNG trả lời câu hỏi ngoài phạm vi Bondy.  
          6. Nếu không có câu trả lời phù hợp trong FAQ, hãy trả lời chính xác:  
            "Xin lỗi, tôi hiện chưa có đủ khả năng để trả lời câu hỏi này."
          7. Nếu người dùng hỏi ngắn gọn kiểu "Cách nhắn tin?", hãy tự hiểu đó là cách nhắn tin trên trang Bondy và trả lời dựa trên FAQ. Nếu thấy thiếu thông tin, hãy hỏi thêm để làm rõ.  
          Hãy tuân thủ nghiêm ngặt các quy tắc trên khi trả lời.
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