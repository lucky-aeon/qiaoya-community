package org.xhy.community.infrastructure.transcript;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.xhy.community.application.course.transcript.TranscriptProvider;
import org.xhy.community.application.course.transcript.TranscriptProviderTaskResult;
import org.xhy.community.application.course.transcript.TranscriptSubmitCommand;
import org.xhy.community.application.course.transcript.TranscriptSubmitResult;
import org.xhy.community.domain.course.valueobject.ChapterTranscriptStatus;

@Component
@ConditionalOnProperty(name = "transcript.provider", havingValue = "noop")
public class NoopTranscriptProvider implements TranscriptProvider {

    @Override
    public TranscriptSubmitResult submit(TranscriptSubmitCommand command) {
        throw new IllegalStateException("transcript.provider=noop 不生成转写结果，请配置 TRANSCRIPT_PROVIDER=dashscope-qwen-asr");
    }

    @Override
    public TranscriptProviderTaskResult query(String providerTaskId) {
        TranscriptProviderTaskResult result = new TranscriptProviderTaskResult();
        result.setStatus(ChapterTranscriptStatus.FAILED);
        result.setErrorCode("TRANSCRIPT_PROVIDER_NOOP");
        result.setErrorMessage("transcript.provider=noop 不生成转写结果，请配置 TRANSCRIPT_PROVIDER=dashscope-qwen-asr");
        return result;
    }
}
