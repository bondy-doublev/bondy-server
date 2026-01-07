import { Module } from '@nestjs/common';
import { FaqController } from './faq.controller';
import { FaqService } from './faq.service';
import { OpenaiModule } from 'src/common/openai/openai.module';
import { SupabaseModule } from 'src/common/supabase/supabase.module';

@Module({
  imports: [OpenaiModule, SupabaseModule],
  controllers: [FaqController],
  providers: [FaqService],
  exports: [FaqService],
})
export class FaqModule {}
