import { Injectable } from '@nestjs/common';
import { OpenaiService } from 'src/common/openai/openai.service';
import { SupabaseService } from 'src/common/supabase/supabase.service';
import { CreateFaqDto } from './dto/create-faq.dto';

@Injectable()
export class FaqService {
  constructor(
    private supabase: SupabaseService,
    private openai: OpenaiService,
  ) {}

  async create(dto: CreateFaqDto) {
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

  delete(id: string) {
    return this.supabase.client.from('faqs').delete().eq('id', id);
  }
}
