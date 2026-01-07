import { Injectable } from '@nestjs/common';
import { OpenaiService } from 'src/common/openai/openai.service';
import { SupabaseService } from 'src/common/supabase/supabase.service';

@Injectable()
export class ChatService {
  constructor(
    private openai: OpenaiService,
    private supabase: SupabaseService,
  ) {}

  async chat(message: string) {
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
}
