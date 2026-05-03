package org.xhy.community.infrastructure.transcript;

public interface TranscriptProvider {

    TranscriptSubmitResult submit(TranscriptSubmitCommand command);

    TranscriptTaskResult query(String providerTaskId);
}
