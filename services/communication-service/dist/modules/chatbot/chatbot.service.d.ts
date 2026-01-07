import { OpenaiService } from 'src/common/openai/openai.service';
import { SupabaseService } from 'src/common/supabase/supabase.service';
export declare class ChatService {
    private openai;
    private supabase;
    constructor(openai: OpenaiService, supabase: SupabaseService);
    chat(message: string): Promise<string | null>;
}
