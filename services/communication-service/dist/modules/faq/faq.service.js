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
exports.FaqService = void 0;
const common_1 = require("@nestjs/common");
const openai_service_1 = require("../../common/openai/openai.service");
const supabase_service_1 = require("../../common/supabase/supabase.service");
let FaqService = class FaqService {
    supabase;
    openai;
    constructor(supabase, openai) {
        this.supabase = supabase;
        this.openai = openai;
    }
    async create(dto) {
        const embedding = await this.openai.embed(dto.question);
        return this.supabase.client.from('faqs').insert({
            question: dto.question,
            answer: dto.answer,
            embedding: embedding.data[0].embedding,
        });
    }
    findAll() {
        return this.supabase.client.from('faqs').select('*');
    }
    delete(id) {
        return this.supabase.client.from('faqs').delete().eq('id', id);
    }
};
exports.FaqService = FaqService;
exports.FaqService = FaqService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [supabase_service_1.SupabaseService,
        openai_service_1.OpenaiService])
], FaqService);
//# sourceMappingURL=faq.service.js.map