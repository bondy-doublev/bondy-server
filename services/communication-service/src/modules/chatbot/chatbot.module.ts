import { Module } from '@nestjs/common';
import { ChatController } from './chatbot.controller';
import { ChatService } from './chatbot.service';
import { OpenaiModule } from 'src/common/openai/openai.module';
import { SupabaseModule } from 'src/common/supabase/supabase.module';

@Module({
  imports: [OpenaiModule, SupabaseModule],
  controllers: [ChatController],
  providers: [ChatService],
})
export class ChatBotModule {}
